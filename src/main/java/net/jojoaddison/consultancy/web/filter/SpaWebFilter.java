package net.jojoaddison.consultancy.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class SpaWebFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SpaWebFilter.class);

    private static final String INDEX_HTML = "static/index.html";

    /**
     * The packaged shell, read once. Null when the client has not been built into the classpath, which
     * is the normal state of a backend-only dev run — the Angular dev server serves the SPA then, and
     * this filter falls back to forwarding.
     */
    private final String shellTemplate = loadShellTemplate();

    /**
     * Forwards any unmapped paths (except those containing a period) to the client {@code index.html}.
     *
     * <p>When the shell is on the classpath it is written directly rather than forwarded, so this
     * request's CSP nonce can be stamped into it (SEC-06). Angular reads {@code ngCspNonce} off the root
     * element and puts the same value on every {@code <style>} it injects at runtime, which is what
     * lets the policy drop {@code style-src 'unsafe-inline'}. A forward could not do this: the response
     * would be the static file, byte for byte, with the placeholder still in it.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // Request URI includes the contextPath if any, removed it.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (
            !path.startsWith("/api") &&
            !path.startsWith("/management") &&
            !path.startsWith("/v3/api-docs") &&
            !path.startsWith("/h2-console") &&
            !path.contains(".") &&
            path.matches("/(.*)")
        ) {
            if (shellTemplate != null) {
                writeShell(request, response);
            } else {
                request.getRequestDispatcher("/index.html").forward(request, response);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeShell(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nonce = CspNonceFilter.nonceOf(request);
        // No nonce means no CspNonceFilter ran; the policy writer drops the nonce source in that case,
        // so leaving the placeholder in the markup would be inert but confusing. Blank it either way.
        String html = shellTemplate.replace(CspNonceFilter.NONCE_PLACEHOLDER, nonce == null ? "" : nonce);

        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // The shell is now per-response — it carries a single-use nonce — so it must never be stored.
        // A cached copy would pair yesterday's nonce with today's header and block every runtime style.
        response.setHeader("Cache-Control", "no-store");
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

    private static String loadShellTemplate() {
        ClassPathResource resource = new ClassPathResource(INDEX_HTML);
        if (!resource.exists()) {
            LOG.info("No {} on the classpath; SPA routes will be forwarded rather than rendered with a CSP nonce", INDEX_HTML);
            return null;
        }
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("Could not read {}; falling back to forwarding SPA routes", INDEX_HTML, e);
            return null;
        }
    }
}

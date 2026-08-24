package net.jojoaddison.consultancy.security.jwt;

import static net.jojoaddison.consultancy.security.jwt.JwtAuthenticationTestUtils.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;
import net.jojoaddison.consultancy.security.AccessTokenCookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@AuthenticationIntegrationTest
class TokenAuthenticationIT {

    @Autowired
    private MockMvc mvc;

    @Value("${jhipster.security.authentication.jwt.base64-secret}")
    private String jwtKey;

    @Test
    void testLoginWithValidToken() throws Exception {
        expectOk(createValidToken(jwtKey));
    }

    @Test
    void testReturnFalseWhenJWThasInvalidSignature() throws Exception {
        expectUnauthorized(createTokenWithDifferentSignature());
    }

    @Test
    void testReturnFalseWhenJWTisMalformed() throws Exception {
        expectUnauthorized(createSignedInvalidJwt(jwtKey));
    }

    @Test
    void testReturnFalseWhenJWTisExpired() throws Exception {
        expectUnauthorized(createExpiredToken(jwtKey));
    }

    private void expectOk(String token) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").cookie(new Cookie(AccessTokenCookie.NAME, token))).andExpect(
            status().isNoContent()
        );
    }

    private void expectUnauthorized(String token) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/authenticate").cookie(new Cookie(AccessTokenCookie.NAME, token))).andExpect(
            status().isUnauthorized()
        );
    }
}

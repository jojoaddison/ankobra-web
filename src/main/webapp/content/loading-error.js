// Shows the "application failed to load" message if the bundle has not booted after 4 seconds.
//
// This lived inline in index.html, and it was the *only* inline script the built page contained —
// meaning `script-src 'unsafe-inline'` existed in the CSP purely to permit these fourteen lines. With
// 'unsafe-inline' present, CSP is not a meaningful XSS mitigation at all: injected script executes
// exactly as happily as this does. Moving it to a file bought `script-src 'self'`.
//
// Kept in content/ because angular.json copies that directory verbatim, so no build wiring is needed
// and the path is already permitAll in SecurityConfiguration.
//
// Note it is served under nginx's immutable one-year cache rule for *.js. That is fine for a file
// whose behaviour is this stable, but if it ever does change, the reference in index.html needs a
// cache-busting query string or returning visitors will keep the old copy.
window.onload = function () {
  setTimeout(showError, 4000);
};

// Toggles a class rather than writing errorElm.style.display. CSP's style-src-attr no longer allows
// inline style attributes (SEC-06); assigning to the CSSOM `style` property happens to remain outside
// what CSP checks, but relying on that distinction to keep a control working is how a control breaks
// later for reasons nobody remembers. The class is defined in content/css/loading.css.
function showError() {
  document.getElementById('jhipster-error')?.classList.remove('is-hidden');
}

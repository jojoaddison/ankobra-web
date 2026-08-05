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

function showError() {
  const errorElm = document.getElementById('jhipster-error');
  if (errorElm?.style) {
    errorElm.style.display = 'block';
  }
}

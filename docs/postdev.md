# CI - Release Workflow

## Step 1: Commit & Push

- commit and push branches

## Step 2: Pull Request

- push branches and open PRs

## Step 3: Merge CI

- merge when CI passes
- code-scanning alerts count: the `CodeQL` check fails on any new alert, so triage and dismiss with
  a reason rather than merging past it

## Step 4: Quality Deployment

- deploy to quality when release build completes — `deploy/quality-testing/startup.sh`, which runs
  the published image on jacserver at <http://jojoaddison.local>
- verify quality deployment in the browser
- the script's own checks cover what a browser would show and a test suite cannot: the CSP reaching
  `GET /`, the nonce, the `HttpOnly` session cookie, and CSRF with a live session

## Step 5: Production Deployment

- deploy to production only if quality is successfully verified
- `./deploy/deploy.sh --channel github --with-nginx` — `--with-nginx` matters whenever
  `deploy/prod-server/*.conf` has changed, or the site keeps its old configuration
- production **is** live at <https://jojoaddison.net> and serves real traffic. Deploying does not
  need express permission each time, but it is not a rehearsal: sessions drop when the auth
  transport changes, and an nginx mistake is public immediately
- rollback is `TAG=<previous-sha> ./deploy/deploy.sh --channel github --skip-build`

## Error handling

- fix errors that come up at any step and repeat step
- a check that fails on a healthy system is itself the defect — fix the check, and say so

---

*Corrected 2026-08-25, after the first release that followed this document end to end. Two premises
had stopped being true: step 5 said production was not operational, and step 4 assumed a quality
environment existed. Neither held — `jojoaddison.net` has been serving live, and
`deploy/quality-testing/` was another project's stack until it was converted. Both are now what the
steps describe.*

# Deployment

Deploys the Jojo Addison site to a server behind host nginx, at
`~/webroot/02-jojoaddison/ankobra-web/`, following the same conventions the health-connect apps on
that host use: one external Docker network per app, loopback-only published ports, host nginx owning
TLS, container names prefixed per app.

Everything in `prod-server/` is deployed **as-is** — the files in this repository are the source of
truth, not a starting point to be edited on the server. A change made only on the host is lost at the
next deploy and invisible to review.

## What gets deployed

**One image.** This is a JHipster monolith: `-Pprod` compiles the Angular bundle into the jar's
static resources, so Spring Boot serves the marketing site, the public enquiry endpoint and the
authenticated portal from a single process. There is no separate web container, and nginx has exactly
one upstream.

The image is built by **jib**, not a Dockerfile — JHipster generates no Dockerfile, and the
`jib-maven-plugin` configuration in `pom.xml` (base `eclipse-temurin:25-jre-noble`) is the image
definition.

```
deploy/
  build.sh                        build + push the image (jib, -Pprod, tests run)
  deploy.sh                       the whole deployment: preflight → build → ship → restart → verify
  prod-server/                    deployed verbatim to the server
    compose.yml                   app + postgres, loopback-only, hardened
    .env.example                  template; the real .env is generated on the server
    ankobra-web.conf              host nginx site (pre-certbot, HTTP only)
    infra.sh                      one-time: create the external network
    start                         pull the tagged image and recreate the stack
    backup.sh                     nightly pg_dump, 14-day retention
.github/workflows/
  ci.yml                          build + test gate for PRs and main (backend verify, frontend lint+test)
  publish.yml                     the github channel: same image, built on a GitHub runner
```

## CI vs. publish

Two workflows, deliberately separate:

- **`ci.yml`** runs on every pull request and on `main`: `./mvnw verify` (backend unit + integration
  tests, JDK 25) and `npm test` (ESLint + vitest). This is the gate a change passes before merge.
- **`publish.yml`** runs on `main`, on `v*` tags, and on demand. It builds the deployable image with
  jib after `-Pprod verify`, and pushes it to `ghcr.io/<owner>/ankobra-web`. Nothing in it touches a
  server — releasing stays a human-run `deploy.sh` step.

## Channels

`--channel` decides **where the image comes from**. It changes nothing else: the same `compose.yml`,
the same `.env`, the same verification.

| Channel | Image | Built by |
| --- | --- | --- |
| `private` (default) | `docker.jojoaddison.net/ankobra-web` | `build.sh`, on your machine |
| `github` | `ghcr.io/<owner>/ankobra-web` | `.github/workflows/publish.yml`, on a GitHub runner |

```bash
./deploy/deploy.sh                                   # private: build here, push, deploy
./deploy/deploy.sh --channel github                  # github: deploy what Actions already built
TAG=<sha> ./deploy/deploy.sh --channel github        # roll back to any published tag
```

The channel is stored on the server as `REGISTRY` in `.env`, and `deploy.sh` rewrites that line —
after showing you the change and asking — whenever the requested channel differs from what the host
points at. `--github` is shorthand for `--channel github`.

`--channel github` never builds. Falling through to `build.sh` would push a locally built image over
the one Actions published *under the same tag*, which is the drift the channel exists to prevent.
Instead the script checks the tag is really published (`docker manifest inspect`), warns if HEAD is
not on a remote branch, and after the restart confirms the running container's image actually came
from `$REGISTRY`.

### Before the github channel works

1. **A push to `main` must complete a `publish.yml` run.** `origin` is
   `github.com:jojoaddison/ankobra-web`, so the GHCR namespace is `ghcr.io/jojoaddison/ankobra-web`
   (also `deploy.sh`'s default). Override for a fork with `GITHUB_REGISTRY=ghcr.io/<owner>`.
2. **A GHCR package is private by default.** Either make it public (Packages → `ankobra-web` →
   Package settings → Change visibility) or give the server a credential:
   `ssh webserver 'docker login ghcr.io -u <user>'` with a PAT carrying `read:packages`.

## First-time install

DNS for the chosen host must already point at the server before the TLS step.

```bash
# 1. Everything except nginx and TLS. Generates all three secrets ON THE SERVER.
./deploy/deploy.sh --bootstrap

# 2. Once you are happy the app is healthy, publish it.
./deploy/deploy.sh --bootstrap --with-nginx --with-tls
```

`--bootstrap` refuses to run if `compose.yml` already exists on the server, and refuses to overwrite
an existing `.env`.

Read the generated admin password once, then store it in a password manager:

```bash
ssh webserver "grep ANKOBRA_ADMIN_PASSWORD ~/webroot/02-jojoaddison/ankobra-web/.env"
```

## Routine deploys

```bash
./deploy/deploy.sh                              # build, ship, restart, verify
./deploy/deploy.sh --verify-only                # mutate nothing, just run the checks
TAG=<previous-sha> ./deploy/deploy.sh --skip-build   # roll back to an already-pushed image
./deploy/deploy.sh --recover                    # compose.yml went missing but .env survived
```

Every mutating step is announced before it runs and prompts unless `--yes` is passed. The certbot
step additionally verifies every hostname resolves to the server's own IP before calling Let's
Encrypt, so it refuses rather than spending rate-limit quota on a name that could not pass.

## Secrets

All three are generated by `openssl rand` **running on the server** during bootstrap, so they never
exist on the deploying machine, in its shell history, or on the wire.

| Variable | Generate with | Notes |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | `openssl rand -base64 24` | No trust auth here, unlike the dev compose file. |
| `JWT_BASE64_SECRET` | `openssl rand -base64 64` | HS512 needs ≥ 64 bytes. Rotating it signs the operator out — the correct response to a suspected leak. |
| `ANKOBRA_ADMIN_PASSWORD` | `openssl rand -base64 24` | Applied to the seeded `admin` account at every boot by `AdminPasswordInitializer`. Unset, the account keeps generator-jhipster's committed `admin`/`admin`. |

`compose.yml` uses `${VAR:?message}` for all three, so a missing value fails at
`docker compose config` with a message naming the variable, rather than booting something insecure.

**There is no fallback if `JWT_BASE64_SECRET` is unset.** The sample key lives in a dev-only profile,
so `SPRING_PROFILES_ACTIVE=prod` does not activate it: production supplies its own key or fails to
start, and failing to start is the safe outcome.

## Production data

The dev-profile `DataSeeder` — the real demo fixtures plus the `kojo`/`ama` logins with a known
password — is `@Profile("dev")` and **does not run in production**. A fresh prod install starts with
only the `admin` account (password from `ANKOBRA_ADMIN_PASSWORD`) and no projects, clients or
tickets. Populate it through the portal, or decide separately whether this environment should carry
seed data.

## Backups

`backup.sh` runs `pg_dump --clean --if-exists` into `backups/`, keeps 14 days, and writes the dump
`0600` — it contains captured leads (names and email addresses). It refuses to rotate old backups
when the new dump is suspiciously small, because a `pg_dump` that fails mid-stream still exits 0
through a pipe.

Add to root's crontab on the server, alongside the existing cert-renewal entry:

```cron
15 3 * * * ~/webroot/02-jojoaddison/ankobra-web/backup.sh >> /var/log/ankobra-web-backup.log 2>&1
```

A backup nobody has restored is a hypothesis. Do one restore drill into a scratch database before
this carries real data.

## Verification

`deploy.sh` finishes by checking, on every deploy:

- `/management/health` is `UP` on the container's loopback port — internally first, so "the app is
  broken" and "the proxy is misconfigured" stay distinguishable
- `/` returns 200 (the monolith is serving its Angular bundle — proves it was built with `-Pprod`)
- `/api/**` answers **401** to an anonymous caller, and `/api/admin/**` likewise
- `/api/public/enquiries` answers **405** to a GET (routed and permitted, wrong method — proves the
  public contact endpoint kept its `permitAll`)
- the public URL responds, if DNS and TLS are in place

## Host conventions this follows

- **Loopback-only ports.** The app publishes `127.0.0.1:8090`; host nginx with Certbot-managed TLS is
  the only thing on a public interface. Change `APP_PORT` if 8090 collides on the target host.
- **Postgres publishes nothing.** `backup.sh` reaches it with `docker exec`.
- **Two external networks.** `ankobrawebnet` is created by `infra.sh`; `monitoring` belongs to
  `~/webroot/00-admin/monitoring` and must already exist, or the app container will not start.
  `infra.sh` warns rather than creating someone else's network.
- **`name:` is pinned** in `compose.yml` so `--remove-orphans` can only touch this project's
  containers.
- **Container hyphens, not underscores.** An underscore is illegal in a hostname, so a container
  named `ankobra_web_app` cannot be addressed over the compose network without Tomcat rejecting the
  request with a bare 400.
- **`/management` returns 404 at the proxy.** Health and metrics belong to the monitoring network,
  which reaches the container directly.

## Decisions to confirm before going live

- **The domain.** This app is the top-level site: `PUBLIC_URL` defaults to `https://jojoaddison.net`
  and the nginx block claims the apex `jojoaddison.net` and `www.jojoaddison.net` (`ALT_HOSTS`), both
  covered by one certificate. Confirm no other enabled nginx site on the host already serves these
  names before the first public deploy.
- **`SSH_HOST` / `REMOTE_DIR`.** Default to `webserver` and `~/webroot/02-jojoaddison/ankobra-web`.
  Override via env if the target host or path differs.
- **Seed data in production** (see above) — decide whether a fresh install should be empty or carry
  fixtures.
- **Mail** is optional and unset by default; configure `SMTP_HOST` if account emails are needed.

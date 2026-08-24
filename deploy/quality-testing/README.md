# ankobra-web — quality

Runs ankobra-web on **jacserver**, at a hostname, behind the same kind of nginx the production
server has — and then checks that it behaves.

```bash
./startup.sh
```

Then <http://jojoaddison.local> from any machine on the LAN. Sign in as `admin`; the password is
generated on jacserver and printed by the script.

The script ships `compose.yml` and a generated `.env` to `jacserver:~/webroot/01-jojoaddison/quality`
and drives compose there over ssh — the way `../deploy.sh` puts production's config on the production
server. Nothing runs on your laptop.

## What this is for

`./mvnw verify` and `npm test` answer "does the code do what it says". This answers a different
question: **does the thing we are about to deploy work when a browser talks to it through an edge.**

That gap is not theoretical here. Three defects shipped through a green suite in this repository:

- `'unsafe-inline'` removed from `script-src` left Angular's `inlineCritical` stylesheet stuck at
  `media="print"`, so every Bootstrap page rendered unstyled. Nothing failed; the built HTML looked
  correct.
- Security headers never reached `GET /` at all for the entire life of the deployment (SEC-14). They
  were present on API responses, which is where nothing needs them.
- Reading the session cookie in a `BearerTokenResolver` exempted **every authenticated request** from
  CSRF protection. 583 tests stayed green, because a MockMvc test that omits the CSRF token also
  omits the session cookie and so never enters the exempted state.

The last one is why the verification suite signs in for real and then makes a write with a live
session and no token. If you add a check, prefer one that needs a real request over a real edge.

## jacserver, not the production VPS

| ssh alias   | address           | `hostname` says | role       |
| ----------- | ----------------- | --------------- | ---------- |
| `jacserver` | 192.168.1.2 (LAN) | `jacserver`     | this stack |
| `webserver` | 199.247.5.252     | `jacserver`     | production |

**The production VPS has its hostname set to `jacserver`**, so `ssh webserver hostname` answers
`jacserver` and reads as proof the two are one machine. They are not; the ssh alias is the only thing
that distinguishes them.

Run here, this stack would bring up a *second* ankobra-web beside the live one — same image, same
health endpoint, competing for the same loopback port. An operator reading `docker ps` during an
incident would find two plausible stacks and no way to tell which one the public hostname reaches.

`--local` has no ssh alias to lean on, so it checks the one property the two do not share: jacserver
has a LAN address and the VPS has no private address at all. Docker bridges are excluded from that
check — every docker host carries `172.17.0.1`, including the VPS.

## One container, not four

ankobra-web is a JHipster **monolith**: Spring Boot serves the compiled Angular bundle from its own
classpath. So `compose.yml` is PostgreSQL and the app, and jacserver's nginx proxies straight to it —
one hop, exactly as in production.

If this file looks like it was adapted from a microservice stack, it was. The earlier version ran
mongodb, kafka, a gateway, an api and a separate web container, and mounted an OpenTelemetry
javaagent into two of them. None of that applies here: ankobra-web instruments itself with Micrometer
Tracing, so there is no agent to mount and no `JAVA_TOOL_OPTIONS` to be careful with.

## The edge is a host vhost

`host-site.conf` is jacserver's nginx site for `jojoaddison.local`. Installing it is one sudo, once:

```bash
sudo ln -sfn ~/work/JojoAddisonDev/workspace/ankobra-web/deploy/quality-testing/host-site.conf \
             /etc/nginx/sites-enabled/jojoaddison.local.conf
sudo nginx -t && sudo systemctl reload nginx
```

`startup.sh` prints that and runs none of it — `/etc` is root-owned and not this repository's to edit.

The container publishes on `127.0.0.1:18092`, so the vhost is the only way in. nginx cannot read the
environment, so that port is written in both `host-site.conf` and `compose.yml`; `startup.sh` checks
they still agree, because the failure mode is a 502 that reads like a dead application.

jacserver is shared, and the 18xxx range is busy: `18080`-`18086`, `18090`-`18091` and
`18100`-`18103` belong to the health-connect stacks, and `18090` in particular is
`hc-professional-quality-service`. A vhost pointed at a port another project owns does not fail —
it proxies `jojoaddison.local` into somebody else's application, which answers 200 and reads as a
deployment gone strange rather than a port clash. Check with `ss -lntH` before changing the port.

**The vhost deliberately sets no security headers**, which is the opposite of the sibling stacks on
this host. ankobra-web sets its own, including a CSP carrying a per-response nonce. Two CSP headers
*intersect* rather than merge, so a static `style-src 'self'` added at the edge would silently forbid
every runtime-injected Angular style and the app would render unstyled with nothing in any log. If
you copy a vhost from a sibling project, delete its `add_header` block rather than adapting it.

## It runs the `prod` profile

Same as production, and that is the point — including the parts that only exist there:

- Liquibase runs its **prod-context** changesets, so the SEC-16 demo-account removal and the SEC-04
  must-change-password migration are exercised here rather than meeting a real database for the
  first time on the production host.
- `DataSeeder` is `@Profile("dev")` and does **not** run. This stack starts with the `admin` account
  and no fixture data, exactly as production did on its first boot.
- `SampleSecretGuard` is active, so a stack misconfigured with the committed development JWT key
  fails to start instead of signing real sessions with a public key.

Do not add `dev` to get demo data. It swaps PostgreSQL for H2 and turns on the published `kojo`/`ama`
logins, which would make this a different application from the one being verified — and put known
credentials on a machine several people can reach.

## Secrets are generated on jacserver and kept

The three — `POSTGRES_PASSWORD`, `JWT_BASE64_SECRET`, `ANKOBRA_ADMIN_PASSWORD` — are generated with
`openssl rand` **on the target**, written to `~/webroot/01-jojoaddison/quality/.env`, and reused on
every later start. They never exist in this repository or on the deploying machine, which is the
property the production design was praised for in the audit and is worth keeping here.

Kept rather than rotated per start for two practical reasons: rotating the JWT key invalidates every
session, so a verification run would sign itself out halfway through; and rotating the admin password
would change the credential from under whoever is using it.

```bash
ssh jacserver "grep ANKOBRA_ADMIN_PASSWORD ~/webroot/01-jojoaddison/quality/.env"
```

## What it checks

Over the LAN, through the vhost, the way a person does:

| | |
| --- | --- |
| **It is ours** | 200 from `/`, and the `<title>` is ankobra-web's — a shared host is full of Angular apps that look the part |
| **CSP reaches `/`** | not just the API (SEC-14), with `script-src 'self'`, a nonce, and **no `unsafe-inline`, no `style-src-attr`, no `unsafe-eval`** anywhere in the policy |
| **The nonce is real** | present in both the header and the markup, equal within one response, different between two, and the shell is `no-store` so a cached page cannot pair yesterday's nonce with today's header |
| **Routing** | a deep client route serves the shell; an unknown `/api` path is *not* swallowed by the SPA fallback; `/management` is closed at the edge |
| **The session** | login answers 200 with the token in **no** readable place — not the body, not a header — and the cookie is `HttpOnly`, `SameSite=Strict`, and *not* `Secure` over plain http (it would be silently discarded) |
| **CSRF** | with a live session: no token → 403, wrong token → 403, matching pair → reaches the controller. **This is the check the whole stack exists for.** |
| **The honeypot** | a genuine enquiry and a honeypot submission both answer 201 — by design, they must be indistinguishable — so it counts the rows and asserts only one was written |
| **Logout** | 204, and the cleared cookie no longer authenticates |

## What it cannot check

Named here rather than left to be assumed, because a suite that looks exhaustive gets trusted for
things it does not do:

- **HSTS and `includeSubDomains`** — need TLS and the real domain. A local certificate would be
  self-signed, and the one thing it would exercise is precisely what cannot be rehearsed off the
  server.
- **Login throttling (SEC-04)** — would lock the `admin` account for the rest of the run.
- **The forced password change (SEC-04)** — `admin` is exempt by design and this stack seeds no
  other account.
- **Outbound mail (SEC-13)** — SMTP is deliberately unconfigured here.

## Commands

```bash
./startup.sh                  # ship, pull, start, wait, verify
./startup.sh --verify         # re-run the checks, touch nothing
./startup.sh --down           # stop, keep the database
./startup.sh --clean          # stop and drop the database volume
./startup.sh --images=local   # use an image built on jacserver (unreleased work)
./startup.sh --local          # you are already on jacserver
```

Logs:

```bash
ssh jacserver 'cd ~/webroot/01-jojoaddison/quality && docker compose -f compose.yml logs -f app'
```

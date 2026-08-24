#!/bin/bash
# Runs ankobra-web on jacserver at http://jojoaddison.local, and then checks that it behaves.
#
#   ./startup.sh                  ship the config, pull, start, wait, verify
#   ./startup.sh --verify         touch nothing, just re-run the checks
#   ./startup.sh --down           stop everything, keep the database
#   ./startup.sh --clean          stop everything and drop the database volume
#   ./startup.sh --host=X         a different ssh target (default: jacserver)
#   ./startup.sh --local          run compose here instead of over ssh (you are already on jacserver)
#   ./startup.sh --images=local   skip the pull and use an image already built there (unreleased work)
#
# --- Where this runs ---------------------------------------------------------------------------
#
# On jacserver — ssh alias `jacserver`, 192.168.1.2 on the LAN. Its config lives in
# ~/webroot/01-jojoaddison/quality, the way ../deploy.sh puts production's config on the production
# server. From a workstation this ships two files there and drives compose over ssh; --local runs
# compose here instead, for when you are already on the machine.
#
# NOT on `webserver`. That is the production VPS (199.247.5.252) — a different machine that reports
# `jacserver` as its own hostname, so asking a host its name cannot tell the two apart. Started
# there, this would bring up a second ankobra-web project beside the live one, competing for the
# same loopback port and answering the same health checks: an operator reading `docker ps` would see
# two plausible stacks and no way to tell which one the public hostname reaches.
#
# From a workstation the ssh alias is what distinguishes them. --local has no alias to lean on, so it
# checks the one property the two do not share: jacserver is on the LAN and the VPS has no private
# address at all. Docker bridges are excluded — every docker host has 172.17.0.1, including the VPS,
# so counting them would defeat the whole test.
#
# --- What this verifies ------------------------------------------------------------------------
#
# Not "did the containers start". They start with an empty database, behind a vhost serving somebody
# else's site, or with a Content-Security-Policy that forbids the styles the application injects —
# and all three look identical from the outside. The checks below go over the LAN, through the
# vhost, the way a person does.
#
# The security work of 2026-08-24 is the reason most of them exist. A CSP nonce, an HttpOnly session
# cookie and CSRF enforcement are all things the test suite cannot see: MockMvc has no browser, no
# edge and no cookie jar, and the CSRF bypass that shipped in that batch passed 583 green tests. If
# you add a check here, prefer the ones that need a real request over a real edge.
set -euo pipefail

cd "$(dirname "$0")"
QUALITY_DIR=$PWD
REPO_ROOT=$(cd ../.. && pwd)

HOSTNAME_LOCAL="${HOSTNAME_LOCAL:-jojoaddison.local}"
SSH_HOST="${SSH_HOST:-jacserver}"
REMOTE_DIR="${REMOTE_DIR:-~/webroot/01-jojoaddison/quality}" # expanded remotely
APP_PORT="${APP_PORT:-18092}"                                # what the vhost proxies to; see host-site.conf
REGISTRY="${REGISTRY:-ghcr.io/jojoaddison}"
BASE="http://${HOSTNAME_LOCAL}"

BOLD=$(tput bold 2>/dev/null || true)
RESET=$(tput sgr0 2>/dev/null || true)
GREEN=$(tput setaf 2 2>/dev/null || true)
RED=$(tput setaf 1 2>/dev/null || true)
YELLOW=$(tput setaf 3 2>/dev/null || true)

step() { printf '\n%s==> %s%s\n' "$BOLD" "$1" "$RESET"; }
ok() { printf '  %s✓%s %s\n' "$GREEN" "$RESET" "$1"; }
warn() { printf '  %s!%s %s\n' "$YELLOW" "$RESET" "$1"; }
die() {
    printf '  %s✗%s %s\n' "$RED" "$RESET" "$1"
    exit 1
}

ACTION=up
LOCAL="${LOCAL:-}"
PULL="${PULL:-yes}"
for arg in "$@"; do
    case "$arg" in
        --verify) ACTION=verify ;;
        --down) ACTION=down ;;
        --clean) ACTION=clean ;;
        --host=*) SSH_HOST="${arg#--host=}" ;;
        --local) LOCAL=yes ;;
        --images=local) PULL=no ;;
        --images=published) PULL=yes ;;
        -h | --help)
            sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *) die "unknown argument: $arg" ;;
    esac
done

# The one property jacserver has and the production VPS does not: an address on the LAN. Docker
# bridges are skipped — every docker host carries 172.17.0.1 and counting it would pass anywhere.
on_the_lan() {
    ip -4 -o addr show scope global 2>/dev/null |
        awk '$2 !~ /^(docker|br-|veth|virbr)/ {print $4}' |
        grep -qE '^(192\.168\.|10\.)'
}

if [[ -n "$LOCAL" ]]; then
    # An explicit bypass for CI, which starts this on a throwaway runner destroyed with the job.
    # Do not set it to get past the check on a real machine: the point is that there is one, and
    # somebody setting this has talked themselves into being the exception.
    if [[ "${ANKOBRA_QUALITY_ALLOW_ANY_HOST:-}" == "1" ]]; then
        warn "host check bypassed by ANKOBRA_QUALITY_ALLOW_ANY_HOST — this had better be CI"
    else
        on_the_lan || die "--local refuses to run here: this machine has no LAN address, so it is not jacserver.
     If this is the production VPS, starting a second ankobra-web here would compete with the live
     stack for its port. Use ssh from a workstation instead."
    fi
    REMOTE_DIR="${REMOTE_DIR/#\~/$HOME}"
    remote() { bash -c "$*"; }
    ship() { cp -f "$1" "$REMOTE_DIR/$2"; }
    WHERE="here"
else
    remote() { ssh -o BatchMode=yes -o ConnectTimeout=10 "$SSH_HOST" "$@"; }
    ship() { scp -q "$1" "$SSH_HOST:$REMOTE_DIR/$2"; }
    WHERE="$SSH_HOST"
fi
compose() { remote "cd $REMOTE_DIR && docker compose --env-file .env -f compose.yml $*"; }

# --- Stopping ----------------------------------------------------------------------------------

if [[ "$ACTION" == "down" ]]; then
    step "Stopping ($WHERE)"
    compose down
    ok "stopped; the database volume is kept — use --clean to drop it"
    exit 0
fi

if [[ "$ACTION" == "clean" ]]; then
    step "Stopping and dropping the database ($WHERE)"
    compose "down -v"
    ok "stopped, volume removed; the next start runs Liquibase against an empty database"
    exit 0
fi

# --- Start -------------------------------------------------------------------------------------

if [[ "$ACTION" == "up" ]]; then
    step "Preflight ($WHERE)"

    if [[ -z "$LOCAL" ]]; then
        remote true 2>/dev/null || die "cannot ssh to '$SSH_HOST' non-interactively — check ~/.ssh/config and your agent.
     If you are already ON that machine, use --local."
    fi
    remote 'command -v docker >/dev/null && docker compose version >/dev/null 2>&1' ||
        die "$WHERE needs docker and the compose plugin"
    ok "docker available ($WHERE)"

    # This repository's HEAD, which is the tag `deploy.sh --channel github` would deploy and the tag
    # publish.yml pushes on every merge to main. So this exercises the next production image rather
    # than something built here.
    TAG="${TAG:-$(git -C "$REPO_ROOT" rev-parse --short HEAD)}"
    ok "image: ${REGISTRY}/ankobra-web:${TAG}"

    # "Free" is not "nothing is listening": a port a container publishes is held by docker-proxy,
    # which ss does not report the way it reports an ordinary listener. A port held by our own app
    # container counts as free — that is a restart, not a conflict.
    if remote "ss -lntH 'sport = :${APP_PORT}' 2>/dev/null | grep -q ." &&
        ! remote "docker ps --format '{{.Names}}' | grep -qx ankobra-web-quality-app"; then
        die "port ${APP_PORT} is in use on $WHERE by something else — the vhost proxies to it, so it has to be ours"
    fi
    ok "port ${APP_PORT} is available ($WHERE)"

    # The vhost and compose.yml both name the port and nginx cannot read the environment, so the two
    # are written independently and can drift. Checked here because the failure is a 502 that reads
    # like a dead application.
    conf_port=$(sed -n 's|.*proxy_pass http://127\.0\.0\.1:\([0-9]*\).*|\1|p' host-site.conf | head -1)
    [[ "$conf_port" == "$APP_PORT" ]] ||
        die "host-site.conf proxies to ${conf_port:-nothing} but the container publishes ${APP_PORT} — change both"
    ok "host-site.conf and compose.yml agree on port ${APP_PORT}"

    step "Secrets ($WHERE)"
    # Generated ON THE TARGET, once, and kept — the property ../README.md calls out as the good part
    # of production's design: they never exist on the deploying machine or in this repository.
    #
    # Kept rather than rotated on every start for two reasons. Rotating the JWT key invalidates every
    # session, so a verification run would sign itself out halfway through. And rotating the admin
    # password would change the credential a person is in the middle of using.
    remote "mkdir -p $REMOTE_DIR"
    kept() { # name -> existing value in the target's .env, or empty
        remote "grep -m1 '^$1=' $REMOTE_DIR/.env 2>/dev/null | cut -d= -f2-" 2>/dev/null || true
    }
    gen() { remote "openssl rand -base64 $1 | tr -d '\n'"; }

    POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-$(kept POSTGRES_PASSWORD)}"
    JWT_BASE64_SECRET="${JWT_BASE64_SECRET:-$(kept JWT_BASE64_SECRET)}"
    ANKOBRA_ADMIN_PASSWORD="${ANKOBRA_ADMIN_PASSWORD:-$(kept ANKOBRA_ADMIN_PASSWORD)}"

    generated=""
    [[ -n "$POSTGRES_PASSWORD" ]] || { POSTGRES_PASSWORD=$(gen 24); generated="$generated POSTGRES_PASSWORD"; }
    # 64 bytes because HS512 rejects anything shorter, as a 500 at sign-in whose message names the
    # algorithm rather than the length.
    [[ -n "$JWT_BASE64_SECRET" ]] || { JWT_BASE64_SECRET=$(gen 64); generated="$generated JWT_BASE64_SECRET"; }
    [[ -n "$ANKOBRA_ADMIN_PASSWORD" ]] || { ANKOBRA_ADMIN_PASSWORD=$(gen 18); generated="$generated ANKOBRA_ADMIN_PASSWORD"; }

    if [[ -n "$generated" ]]; then
        ok "generated on $WHERE:$generated"
    else
        ok "reusing the secrets already in $REMOTE_DIR/.env"
    fi

    # Only if something is actually collecting. Enabled with nothing listening, the exporter retries
    # forever and fills the log with connection errors — see compose.yml.
    if remote "docker ps --format '{{.Names}}' | grep -qx otel-collector-quality"; then
        OTEL_TRACING_ENABLED=true
        ok "otel-collector-quality is running — tracing on"
    else
        OTEL_TRACING_ENABLED=false
    fi

    step "Installing the config ($WHERE)"
    ship compose.yml compose.yml
    # Generated, never hand-edited there: an edit on the server is invisible from here and would be
    # overwritten by the next start anyway. The secrets above are carried through unchanged.
    remote "umask 077 && cat > $REMOTE_DIR/.env" <<EOF
# Generated by quality-testing/startup.sh — overwritten on every start.
# The three secrets are generated ON THIS HOST and kept across restarts; nothing here is committed.
REGISTRY=$REGISTRY
TAG=$TAG
APP_PORT=$APP_PORT
PUBLIC_URL=$BASE
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
JWT_BASE64_SECRET=$JWT_BASE64_SECRET
ANKOBRA_ADMIN_PASSWORD=$ANKOBRA_ADMIN_PASSWORD
OTEL_TRACING_ENABLED=$OTEL_TRACING_ENABLED
EOF
    ok "compose.yml and .env installed in $REMOTE_DIR ($WHERE)"

    step "Starting the stack ($WHERE)"
    # The published image by default, which is the contract this file is built on: what runs here is
    # what production pulls, tag for tag, with no build context that can drift.
    #
    # --images=local is the deliberate exception and it is narrow. Unreleased work has no published
    # image, so there is no other way to exercise it behind the edge — which is the entire reason
    # this stack exists. Opt-in and announced, because an accidental skip would silently run
    # yesterday's image and pass.
    if [[ "$PULL" == "yes" ]]; then
        compose "pull --quiet" || die "could not pull ${REGISTRY}/ankobra-web:${TAG} — is it published?
     publish.yml pushes on every merge to main; a branch commit has no image unless you dispatched one.
     If you built it there for unreleased work, pass --images=local."
    else
        warn "--images=local: not pulling. This is NOT the published image."
        remote "docker image inspect ${REGISTRY}/ankobra-web:${TAG} >/dev/null 2>&1" ||
            die "${REGISTRY}/ankobra-web:${TAG} is not present on $WHERE — see ../build.sh"
        ok "the image is present locally"
    fi
    compose "up -d --remove-orphans"
    ok "containers up"

    step "Waiting for the app to report healthy (up to 5 minutes)"
    # Wide, and not a hang: Liquibase runs every entity changelog on first boot against an empty
    # database, which takes minutes on this hardware.
    deadline=$((SECONDS + 300))
    while ((SECONDS < deadline)); do
        unhealthy=$(compose "ps --format '{{.Name}} {{.Health}}'" 2>/dev/null |
            awk '$2 != "" && $2 != "healthy" {print $1}' || true)
        [[ -z "$unhealthy" ]] && break
        sleep 5
    done
    if [[ -n "${unhealthy:-}" ]]; then
        warn "still not healthy: $unhealthy"
        warn "logs:  ssh $SSH_HOST 'cd $REMOTE_DIR && docker compose -f compose.yml logs --tail 80 app'"
    else
        ok "every service with a healthcheck reports healthy"
    fi
fi

# --- Verify ------------------------------------------------------------------------------------

step "Verifying $BASE"

getent hosts "$HOSTNAME_LOCAL" >/dev/null 2>&1 ||
    die "$HOSTNAME_LOCAL does not resolve from here — it should point at $SSH_HOST on the LAN"

check() { # description, actual, expected-pattern
    if [[ "$2" =~ $3 ]]; then
        ok "$1 ($2)"
    else
        warn "$1 — got '$2', expected $3"
        FAILED=yes
    fi
}
refute() { # description, actual, pattern-that-must-not-match
    if [[ "$2" =~ $3 ]]; then
        warn "$1 — '$2' matched $3, which it must not"
        FAILED=yes
    else
        ok "$1"
    fi
}
code() {
    local out
    out=$(curl -s -o /dev/null -w '%{http_code}' --max-time 15 "$@") || out=000
    echo "${out:-000}"
}

JAR=$(mktemp)
LOGIN_BODY=$(mktemp)
trap 'rm -f "$JAR" "$LOGIN_BODY"' EXIT

# --- The edge is serving this application ------------------------------------------------------

home_code=$(code "$BASE/")
check "the marketing front is served" "$home_code" '^200$'
if [[ "$home_code" != "200" ]]; then
    warn "if the container is healthy this is the vhost, not the stack: the app answers directly on"
    warn "$WHERE at 127.0.0.1:${APP_PORT}. Install host-site.conf there — see the README."
fi

# A 200 says something answered, not that it is ours. jacserver runs several stacks and every one of
# them is an Angular application behind an nginx that looks the part; the title is the cheapest thing
# that tells them apart.
check "and it is ankobra-web, not another site on this host" \
    "$(curl -s --max-time 15 "$BASE/" | tr -d '\n' | sed -n 's/.*<title>\([^<]*\)<\/title>.*/\1/p')" \
    '^Jojo Addison Consultancy'

# SEC-14: the headers have to reach GET /, not just the API. They did not, for the entire life of
# the deployment, because SpaWebFilter forwarded and the security chain never ran on that dispatch.
headers=$(curl -sI --max-time 15 "$BASE/" | tr -d '\r')
csp=$(sed -n 's/^[Cc]ontent-[Ss]ecurity-[Pp]olicy: //p' <<<"$headers")
check "the CSP reaches the SPA entry point, not just the API" "${csp:-absent}" "^default-src 'self'"
check "script-src is 'self'" "$(grep -c "script-src 'self';" <<<"$csp")" '^1$'
check "style-src carries a per-response nonce" "$(grep -c "nonce-" <<<"$csp")" '^1$'
# SEC-06, the whole point of the 2026-08-24 CSP work: no directive may allow inline anything.
refute "no 'unsafe-inline' survives under any directive" "$csp" "unsafe-inline"
refute "and style-src-attr is gone entirely" "$csp" "style-src-attr"
refute "and 'unsafe-eval' is still gone" "$csp" "unsafe-eval"

# The nonce is only a control if it is per-response and if the HTML agrees with the header. A static
# nonce is decoration, and a header that disagrees with the markup blocks every Angular style.
nonce_hdr=$(sed -n "s/.*'nonce-\([^']*\)'.*/\1/p" <<<"$csp")
nonce_html=$(curl -s --max-time 15 "$BASE/" | grep -oi 'ngcspnonce="[^"]*"' | head -1 | sed 's/.*="\(.*\)"/\1/')
second=$(curl -sI --max-time 15 "$BASE/" | tr -d '\r' | sed -n "s/.*'nonce-\([^']*\)'.*/\1/p")
check "a nonce is stamped into the served HTML" "${nonce_html:-none}" '^.{8,}$'
check "the header's nonce and the markup's agree within one response" \
    "$([[ -n "$nonce_html" && "$nonce_html" == "$nonce_hdr" ]] && echo same || echo "differ")" '^same$'
# Vacuously true if the second reading came back empty, so require it to exist first.
check "a second request gets a nonce of its own" "${second:-none}" '^.{8,}$'
refute "and it is not the same one" "$nonce_hdr" "^${second:-__absent__}$"
check "the shell is not cached, so a nonce cannot outlive its header" \
    "$(sed -n 's/^[Cc]ache-[Cc]ontrol: //p' <<<"$headers")" 'no-store'

check "a deep client route still serves the shell" "$(code "$BASE/portal/overview")" '^200$'
check "an unknown API path is not swallowed by the SPA fallback" "$(code "$BASE/api/nope")" '^(401|403|404)$'
check "/management stays closed at the edge" "$(code "$BASE/management/prometheus")" '^404$'

# --- Signing in, and where the token ends up ---------------------------------------------------

# Two statements, not `curl && awk` in one substitution: under `set -e` an assignment whose command
# substitution exits non-zero kills the script, so a single unreachable request would abort the run
# instead of reporting a failed check like every other line here.
curl -s -c "$JAR" --max-time 15 -o /dev/null "$BASE/" || true
xsrf=$(awk '/XSRF-TOKEN/ {print $7}' "$JAR" 2>/dev/null || true)
check "the CSRF cookie is issued eagerly, so a client has one before its first POST" \
    "${xsrf:-none}" '^.{8,}$'

login_headers=$(curl -s -b "$JAR" -c "$JAR" --max-time 15 -D- -o "$LOGIN_BODY" \
    -X POST "$BASE/api/authenticate" \
    -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $xsrf" \
    -d "{\"username\":\"admin\",\"password\":\"${ANKOBRA_ADMIN_PASSWORD:-$(remote "grep -m1 '^ANKOBRA_ADMIN_PASSWORD=' $REMOTE_DIR/.env | cut -d= -f2-")}\",\"rememberMe\":false}" |
    tr -d '\r')
login_body=$(cat "$LOGIN_BODY" 2>/dev/null || true)

check "admin can sign in" "$(head -1 <<<"$login_headers" | awk '{print $2}')" '^200$'
set_cookie=$(sed -n 's/^[Ss]et-[Cc]ookie: //p' <<<"$login_headers" | grep ANKOBRA-ACCESS-TOKEN || true)

# SEC-06. The three places the token must NOT be, and one attribute it must carry. Any of these
# failing hands the session back to script and undoes the migration.
refute "the token is not in the login response body" "$login_body" "id_token"
refute "nor in an Authorization header" "$login_headers" "[Aa]uthorization:"
check "the session cookie is HttpOnly" "$(grep -ci httponly <<<"$set_cookie")" '^1$'
check "and SameSite=Strict" "$(grep -ci 'samesite=strict' <<<"$set_cookie")" '^1$'
# Over plain HTTP it must NOT be Secure, or the browser drops it and login fails with nothing in any
# log to say why. This is the one place quality and production legitimately differ, and the reason
# AccessTokenCookie keys `secure` off the request rather than hard-coding it.
refute "and not Secure over plain http, or the browser would silently discard it" "$set_cookie" "[Ss]ecure"

check "the cookie authenticates an ordinary read" "$(code -b "$JAR" "$BASE/api/account")" '^200$'

# --- CSRF, with a live session -----------------------------------------------------------------
#
# THE check this stack exists for. Reading the cookie in a BearerTokenResolver exempts every
# authenticated request from CSRF, because OAuth2ResourceServerConfigurer ignores anything its
# resolver can read a token from. That shipped, and 583 tests stayed green: a MockMvc test that omits
# the CSRF token also omits the session cookie, so the suite only ever exercised forgery from a
# browser with no session — which is not the threat.
#
# The two rows below are the ones that were 400 before the fix.
check "a write with a session and NO csrf token is refused" \
    "$(code -b "$JAR" -X POST "$BASE/api/tickets" -H 'Content-Type: application/json' -d '{}')" '^403$'
check "a write with a session and a WRONG csrf token is refused" \
    "$(code -b "$JAR" -X POST "$BASE/api/tickets" -H 'Content-Type: application/json' \
        -H 'X-XSRF-TOKEN: not-the-right-token' -d '{}')" '^403$'
# 400 is the pass: bean validation rejected an empty body, which means the request reached the
# controller rather than being stopped at the edge of the filter chain.
check "and a matching token pair reaches the controller" \
    "$(code -b "$JAR" -X POST "$BASE/api/tickets" -H 'Content-Type: application/json' \
        -H "X-XSRF-TOKEN: $xsrf" -d '{}')" '^400$'

# --- The public contact form -------------------------------------------------------------------
#
# SEC-08's honeypot, checked on its effect rather than its status code, because the whole design is
# that a caught submission is INDISTINGUISHABLE from a real one to the sender: both answer 201. The
# only way to tell them apart is to count what was written.
leads_before=$(curl -s -b "$JAR" --max-time 15 "$BASE/api/leads/count" | tr -dc '0-9')
enquiry='{"name":"Quality Check","email":"quality@example.com","need":"Consultancy","message":"Automated verification"}'
honeypot='{"name":"Spam Bot","email":"bot@example.com","need":"Consultancy","message":"Automated verification","website":"http://spam.example"}'
check "the public contact form accepts a genuine enquiry" \
    "$(code -b "$JAR" -X POST "$BASE/api/public/enquiries" -H 'Content-Type: application/json' \
        -H "X-XSRF-TOKEN: $xsrf" -d "$enquiry")" '^201$'
check "and answers a honeypot submission identically" \
    "$(code -b "$JAR" -X POST "$BASE/api/public/enquiries" -H 'Content-Type: application/json' \
        -H "X-XSRF-TOKEN: $xsrf" -d "$honeypot")" '^201$'
leads_after=$(curl -s -b "$JAR" --max-time 15 "$BASE/api/leads/count" | tr -dc '0-9')
check "but only one of the two was written" \
    "$((${leads_after:-0} - ${leads_before:-0}))" '^1$'

# --- Signing out -------------------------------------------------------------------------------
#
# A server call now, because script cannot delete an HttpOnly cookie — which is the same property
# that makes the cookie worth having.
check "logout clears the session" \
    "$(code -b "$JAR" -c "$JAR" -X POST "$BASE/api/logout" -H "X-XSRF-TOKEN: $xsrf")" '^204$'
check "and the cleared cookie no longer authenticates" "$(code -b "$JAR" "$BASE/api/account")" '^401$'

# --- What this stack cannot check ---------------------------------------------------------------
#
# Stated rather than silently missing, because a suite that looks exhaustive is trusted for the
# things it does not do:
#
#   HSTS and includeSubDomains   need TLS and the real domain; there is no certificate here
#   login throttling (SEC-04)    would lock the admin account for the rest of the run
#   the forced password change   admin is exempt by design, and this stack seeds no other account
#   outbound mail (SEC-13)       SMTP is deliberately unconfigured here

step "Ready"
cat <<EOF
  ${BOLD}${BASE}${RESET}

  Sign in as ${BOLD}admin${RESET} — the password was generated on $WHERE and is not in this repository:

    ssh $SSH_HOST "grep ANKOBRA_ADMIN_PASSWORD $REMOTE_DIR/.env"

  Image     ${REGISTRY}/ankobra-web:${TAG:-$(git -C "$REPO_ROOT" rev-parse --short HEAD)}
  Edge      $SSH_HOST's nginx -> 127.0.0.1:${APP_PORT}   (host-site.conf)
  Direct    127.0.0.1:${APP_PORT} on $SSH_HOST — for poking; normal traffic goes through the hostname

  Logs      ssh $SSH_HOST 'cd $REMOTE_DIR && docker compose -f compose.yml logs -f app'
  Stop      ./startup.sh --down
  Reset     ./startup.sh --clean && ./startup.sh
EOF

if [[ -n "${FAILED:-}" ]]; then
    printf '\n  %sSome checks did not pass — the stack is up but not behaving as expected.%s\n' "$YELLOW" "$RESET"
    exit 1
fi

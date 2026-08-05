# Security runbook — ankobra-web

Written 2026-08-05 for G-07 in [`security-20260805-0936.md`](security-20260805-0936.md). Four
procedures you will want under pressure, written before they are needed rather than during.

Everything here assumes `ssh webserver` works and you can `sudo` there. Commands are copy-pasteable.

**Before anything else, in any incident:** write down the time you started and what you saw. The audit
trail (below) tells you what happened to the system; nothing records what *you* did to it.

---

## 1. Suspected account compromise

**Signals.** Unexpected `event=login.success` in the audit log — an unfamiliar `source_ip`, an odd
hour, or a success immediately following a burst of `event=login.failure` for the same account.

```bash
# Everything that account has done, newest last.
ssh webserver 'docker logs ankobra-web-app 2>&1 | grep SECURITY_AUDIT | grep "login=\"<login>\""'

# Who else has been trying, and from where.
ssh webserver 'docker logs ankobra-web-app 2>&1 | grep "event=login.failure" | tail -50'

# Privilege changes — the thing an attacker wants and the thing you most need to spot.
ssh webserver 'docker logs ankobra-web-app 2>&1 | grep -E "event=account\.(created|updated|deleted)"'
```

Logs also reach Grafana via Alloy → Loki, where `{app="ankobra-web"} |= "SECURITY_AUDIT"` is the same
query with history beyond the container's lifetime. Prefer Loki if the container has been restarted.

**Containment.** Start here — this signs one account out everywhere, immediately, without touching
anyone else (SEC-09):

```bash
curl -X POST https://jojoaddison.net/api/admin/users/<login>/revoke-sessions \
  -H "Authorization: Bearer <your admin token>"        # 204 = every token they hold is now dead
```

Then, in order:

1. **Revoke their sessions** (above). Kills every token already issued to them. Does not stop them
   logging in again, so pair it with 2 or 3.
2. **Deactivate the account** — admin → user management → deactivate. Blocks new logins, and also
   revokes their existing tokens on the active→inactive transition.
3. **Change their password** — also revokes their sessions, including the one doing the changing.
4. **Rotate the signing key** (§2) — now only needed for a *key* compromise, not an account one. It
   signs out everybody.

**Judgement call:** if the account is an admin, do 1 and 2 immediately and do not wait to investigate
first. An admin token can create more admin accounts, and every minute it stays valid is a minute they
can re-establish access through a route your containment did not cover.

> **Do not bump `token_version` with SQL.** `findOneByLogin` is `@Cacheable`, and the validator reads
> the user through it — a direct `UPDATE` is silently ineffective for up to the cache TTL (an hour in
> production) and needs an app restart to take hold. Use the endpoint or the admin UI, both of which
> evict. This was found by a test, not in production, but it would have failed exactly when it mattered.

---

## 2. Rotating the JWT signing key

Invalidates every issued token immediately. Everyone is signed out and logs in again. There is no
partial version of this.

```bash
ssh webserver
cd ~/webroot/01-jojoaddison/ankobra-web
cp .env .env.bak-$(date +%F-%H%M) && chmod 600 .env.bak-*   # so a typo is recoverable

NEW_SECRET=$(openssl rand -base64 64 | tr -d '\n')
sudo sed -i "s|^JWT_BASE64_SECRET=.*|JWT_BASE64_SECRET=${NEW_SECRET}|" .env
unset NEW_SECRET                                            # keep it out of the shell history

./start                                                     # recreates the app container
```

Confirm it took: a token minted before the rotation must now be rejected.

```bash
curl -s -o /dev/null -w '%{http_code}\n' https://jojoaddison.net/api/account \
  -H "Authorization: Bearer <a token from before>"          # expect 401
```

The app refuses to start if the key is missing, and `SampleSecretGuard` refuses if it is the committed
development key — so a container that comes up healthy is itself evidence the value is a real one.

---

## 3. Rotating the database password

```bash
ssh webserver
cd ~/webroot/01-jojoaddison/ankobra-web
cp .env .env.bak-$(date +%F-%H%M) && chmod 600 .env.bak-*

NEW_PW=$(openssl rand -base64 24)
# Change it inside PostgreSQL FIRST. Doing .env first leaves the app unable to connect if this fails.
docker exec ankobra-web-postgres psql -U jojoaddison -d jojoaddison \
  -c "ALTER USER jojoaddison WITH PASSWORD '${NEW_PW}'"
sudo sed -i "s|^POSTGRES_PASSWORD=.*|POSTGRES_PASSWORD=${NEW_PW}|" .env
unset NEW_PW

./start
curl -sf http://127.0.0.1:8090/management/health | grep -q '"status":"UP"' && echo OK
```

The Postgres container reads `POSTGRES_PASSWORD` only when it initialises an empty data directory, so
changing it in `.env` alone changes nothing about the database — hence the `ALTER USER`.

---

## 4. Restoring from backup

Backups are nightly at 03:15 into `~/webroot/01-jojoaddison/ankobra-web/backups/`, 14-day retention.
`.sql.gz.gpg` is encrypted; `.sql.gz` is not (see the deploy README).

**Decrypting** needs the private key, which is deliberately *not* on the server. Copy the archive to
wherever that key lives:

```bash
scp webserver:~/webroot/01-jojoaddison/ankobra-web/backups/ankobra_web_YYYY-MM-DD.sql.gz.gpg .
gpg --decrypt ankobra_web_YYYY-MM-DD.sql.gz.gpg | gunzip > restore.sql
```

**Restoring into production** — destructive, and there is no undo. Take a fresh dump first even if the
database looks broken; a broken database is still evidence about what happened to it.

```bash
ssh webserver
cd ~/webroot/01-jojoaddison/ankobra-web
sudo ./backup.sh                                            # snapshot the current state first
docker compose stop app                                     # stop writes during the restore
gunzip -c backups/<file>.sql.gz | docker exec -i ankobra-web-postgres \
  psql --username=jojoaddison --dbname=jojoaddison
docker compose start app
sudo ./verify-restore.sh                                    # confirm the result is coherent
```

The dumps use `--clean --if-exists`, so they replay into a populated database without erroring on
every existing object.

**Verifying a backup without restoring over anything** — this is the drill worth doing when nothing is
wrong, and it runs weekly at 03:40 Sundays:

```bash
ssh webserver 'sudo ~/webroot/01-jojoaddison/ankobra-web/verify-restore.sh'
```

Note what it does not cover: it dumps the live database and restores *that*, which proves `pg_dump`
output is restorable. It cannot prove a stored archive decrypts, because the key is off-host. Do that
one by hand, on the machine holding the private key, at least when the key changes.

---

## 5. Offboarding a consultant or admin

1. Deactivate the account (`/admin` → user management). Do not delete it — the audit trail references
   the login, and deleting the row makes past events harder to attribute.
2. Check what they touched: `grep 'actor="<login>"'` over the audit log.
3. Revoking sessions is now covered by step 1 — deactivation revokes on the active→inactive
   transition. Confirm it took: `grep 'event=account.sessions_revoked' ` over the audit log, or check
   that a token they held is refused. Rotating the signing key (§2) is no longer necessary for
   offboarding; keep it for a suspected *key* leak.
4. Rotate any shared credential they knew: `.env` secrets, the private registry login, SSH access.

---

## What is still missing

Honest gaps, so nobody discovers them mid-incident:

- **No self-service "sign out my other devices".** Per-user revocation exists (SEC-09) but only an
  admin can trigger it; a user who suspects their own session is compromised has to ask.
- **No alerting.** Every query here is something a human has to think to run. Nothing pages on a burst
  of `event=login.failure` or on `event=account.created`.
- **No off-host backup copy.** The dumps live on the same host as the database. A host loss takes both.
- **Mail is unconfigured** (SEC-13), so password-reset and account-creation emails are not delivered.
  Account recovery is currently an out-of-band, manual process.

#!/bin/bash
# Nightly PostgreSQL backup, invoked from root's crontab alongside the existing cert-renewal entry
# (see ../README.md). Follows the convention the sibling apps on this host already use.
#
# The database is the whole of the durable state: this app stores no uploads. It does hold captured
# leads (people's names, email addresses and free-text messages submitted by third parties through a
# public form), so the dump is treated accordingly — 0600, owned by the invoking user, never written
# anywhere world-readable, and encrypted at rest when a recipient key is configured.
#
# ENCRYPTION (SEC-10 in ../../docs/security-20260805-0936.md)
# ----------------------------------------------------------
# If `backup-pubkey.asc` is present next to this script, the dump is piped straight into gpg and only
# the ciphertext ever touches disk — the plaintext exists solely in the pipe. Generate the pair on a
# machine that is NOT this server, keep the private half there, and copy only the public half up:
#
#     gpg --quick-generate-key "ankobra-web backup" default default never
#     gpg --armor --export "ankobra-web backup" > backup-pubkey.asc
#     scp backup-pubkey.asc webserver:~/webroot/01-jojoaddison/ankobra-web/
#
# A private key stored on the same host as the backups it protects is decoration, not encryption.
#
# Without that file the dump is written UNENCRYPTED and the script says so, loudly, on every run.
# That is deliberate and is the opposite of the usual fail-closed instinct: the risk being managed
# here is total data loss, an unencrypted 0600 dump sits on the same host as the live database it
# came from (so it adds little exposure the database does not already carry), and a backup that
# refuses to run because a key is missing is how a host ends up with no backups at all — which is
# exactly the state this app was in until 2026-08-05.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

PUBKEY="${ANKOBRA_BACKUP_PUBKEY:-backup-pubkey.asc}"

mkdir -p backups
chmod 700 backups
stamp="$(date +%F)"

# --clean --if-exists so the dump can be replayed straight into a populated database during a restore
# drill, rather than erroring on every existing object.
dump_command=(docker exec ankobra-web-postgres
  pg_dump --username=jojoaddison --dbname=jojoaddison --clean --if-exists)

if [[ -f "$PUBKEY" ]]; then
  out="backups/ankobra_web_${stamp}.sql.gz.gpg"
  # One pipeline: plaintext never lands on disk, so there is nothing to shred afterwards.
  # --trust-model always because this is a bare public key with no web of trust behind it; refusing
  # to encrypt to an untrusted key is the wrong failure for an unattended backup.
  "${dump_command[@]}" | gzip | gpg --batch --yes --quiet --trust-model always \
    --encrypt --recipient-file "$PUBKEY" --output "$out"
  encrypted=true
else
  out="backups/ankobra_web_${stamp}.sql.gz"
  "${dump_command[@]}" | gzip > "$out"
  encrypted=false
fi

chmod 600 "$out"

# Refuse to keep an empty dump and silently rotate the good ones away behind it. A pg_dump that fails
# mid-stream still exits 0 through the pipe, so size is the check that actually catches it.
size="$(stat -c%s "$out")"
if [[ "$size" -lt 1024 ]]; then
  echo "ERROR: dump is suspiciously small (${size} bytes) — not rotating old backups" >&2
  exit 1
fi

# 14-day retention, matching the sibling apps. Both extensions, so a switch to encryption does not
# strand the older plaintext dumps outside the rotation and keep them forever.
find backups \( -name '*.sql.gz' -o -name '*.sql.gz.gpg' \) -mtime +14 -delete

if [[ "$encrypted" == true ]]; then
  echo "backup complete (encrypted to $(basename "$PUBKEY")):"
else
  echo "WARNING: ${PUBKEY} not found — this dump is UNENCRYPTED and contains third-party personal" >&2
  echo "         data. See the header of this script to generate a key and enable encryption." >&2
  echo "backup complete (UNENCRYPTED):"
fi
ls -la "$out"

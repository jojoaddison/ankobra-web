#!/bin/bash
# Nightly PostgreSQL backup, invoked from root's crontab alongside the existing cert-renewal entry
# (see ../README.md). Follows the convention the sibling apps on this host already use.
#
# The database is the whole of the durable state: this app stores no uploads. It does hold captured
# leads (people's names and email addresses), so the dump is treated accordingly — 0600, owned by the
# invoking user, never written anywhere world-readable.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

mkdir -p backups
chmod 700 backups
stamp="$(date +%F)"
out="backups/ankobra_web_${stamp}.sql.gz"

# --clean --if-exists so the dump can be replayed straight into a populated database during a restore
# drill, rather than erroring on every existing object.
docker exec ankobra-web-postgres \
  pg_dump --username=jojoaddison --dbname=jojoaddison --clean --if-exists \
  | gzip > "$out"

chmod 600 "$out"

# Refuse to keep an empty dump and silently rotate the good ones away behind it. A pg_dump that fails
# mid-stream still exits 0 through the pipe, so size is the check that actually catches it.
if [[ "$(stat -c%s "$out")" -lt 1024 ]]; then
  echo "ERROR: dump is suspiciously small ($(stat -c%s "$out") bytes) — not rotating old backups" >&2
  exit 1
fi

# 14-day retention, matching the sibling apps.
find backups -name '*.sql.gz' -mtime +14 -delete

echo "backup complete:"
ls -la "$out"

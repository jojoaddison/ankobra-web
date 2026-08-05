#!/bin/bash
# Restore drill (G-05 in ../../docs/security-20260805-0936.md). A backup nobody has restored is a
# hypothesis; this turns it into a check that can run unattended.
#
# WHAT IT PROVES, AND WHAT IT DOES NOT
# ------------------------------------
# It takes a FRESH pg_dump, replays it into a throwaway PostgreSQL container, and asserts the schema
# and row counts arrive intact. That proves the thing most likely to rot: that `pg_dump --clean
# --if-exists` against this database produces output that actually restores, and that the tables a
# restore would be judged on are populated.
#
# It deliberately does NOT decrypt a stored archive. It cannot: the private key lives off this host,
# which is the entire point of encrypting the backups (see backup.sh). Verifying the stored ciphertext
# end-to-end therefore has to happen wherever that key is — that drill is in the runbook, and this
# script is not a substitute for it. What this catches is a dump that never restores; what it cannot
# catch is a backup file corrupted after it was written.
#
# Safe to run against production: pg_dump takes no locks that block writes, and everything else
# happens inside a container that is removed on exit.
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

SCRATCH_CONTAINER="ankobra-web-restore-drill"
PG_IMAGE="postgres:18.4"
SCRATCH_PASSWORD="restore-drill-$$"

# Every table whose contents a restore is judged on. Each is compared SOURCE vs RESTORED rather than
# against a fixed expectation: production currently has 2 users and 3 authorities but zero clients,
# projects, tickets and leads, so "must be non-empty" would fail every drill today and "must exist"
# would pass a dump that restored the schema and none of the rows. Comparing counts is right whether a
# table holds nothing or a million rows, and needs no editing as the data grows.
COMPARED_TABLES=(jhi_user jhi_authority jhi_user_authority client project ticket quote quote_line milestone lead
  course service_item team_member)

# One absolute floor: the seeded admin always exists, so an empty jhi_user means the dump captured
# nothing at all and every "0 == 0" comparison below would agree with itself and pass.
FLOOR_TABLE=jhi_user

cleanup() {
  docker rm -f "$SCRATCH_CONTAINER" >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "==> Dumping the live database"
dump="$(mktemp)"
chmod 600 "$dump"
trap 'rm -f "$dump"; cleanup' EXIT
docker exec ankobra-web-postgres \
  pg_dump --username=jojoaddison --dbname=jojoaddison --clean --if-exists > "$dump"
echo "    $(stat -c%s "$dump") bytes"

echo "==> Starting a throwaway PostgreSQL (${PG_IMAGE})"
docker run -d --name "$SCRATCH_CONTAINER" \
  -e POSTGRES_DB=jojoaddison -e POSTGRES_USER=jojoaddison -e POSTGRES_PASSWORD="$SCRATCH_PASSWORD" \
  "$PG_IMAGE" >/dev/null
# No published port: the drill talks to it through `docker exec` only, so it is never reachable off
# the host even for the seconds it exists.

# Poll with a real query rather than pg_isready. During initialisation the postgres image runs a
# temporary server that pg_isready happily reports as ready, before POSTGRES_DB has been created and
# before the real server restarts — so the obvious readiness check races and the restore then fails
# with `database "jojoaddison" does not exist`. A SELECT against the target database only succeeds
# once the database actually exists on the final server.
deadline=$((SECONDS + 90))
until docker exec "$SCRATCH_CONTAINER" psql --username=jojoaddison --dbname=jojoaddison -tAc 'SELECT 1' >/dev/null 2>&1; do
  if [[ "$SECONDS" -ge "$deadline" ]]; then
    echo "    FAIL  throwaway database never became ready" >&2
    docker logs --tail 20 "$SCRATCH_CONTAINER" >&2 || true
    exit 1
  fi
  sleep 1
done

echo "==> Restoring into it"
# --clean --if-exists emits DROPs for objects that do not exist yet in an empty database; those are
# notices, not failures, so ON_ERROR_STOP would reject a perfectly good dump. Real breakage shows up
# as missing tables in the assertions below, which is the check that matters.
docker exec -i "$SCRATCH_CONTAINER" psql --username=jojoaddison --dbname=jojoaddison --quiet \
  > /dev/null < "$dump"

echo "==> Verifying (source vs restored)"
failed=0

count_in() { # container, table -> row count, or MISSING
  docker exec "$1" psql --username=jojoaddison --dbname=jojoaddison -tAc "SELECT count(*) FROM $2" 2>/dev/null || echo "MISSING"
}

for table in "${COMPARED_TABLES[@]}"; do
  source_count="$(count_in ankobra-web-postgres "$table")"
  restored_count="$(count_in "$SCRATCH_CONTAINER" "$table")"

  if [[ "$source_count" == "MISSING" ]]; then
    echo "    FAIL  ${table}: absent from the LIVE database — this list is stale, fix the script" >&2
    failed=1
  elif [[ "$restored_count" == "MISSING" ]]; then
    echo "    FAIL  ${table}: absent after restore (schema did not come back)" >&2
    failed=1
  elif [[ "$source_count" != "$restored_count" ]]; then
    echo "    FAIL  ${table}: ${source_count} rows live, ${restored_count} restored" >&2
    failed=1
  else
    printf "    ok    %-20s %s rows\n" "$table" "$restored_count"
  fi
done

floor="$(count_in "$SCRATCH_CONTAINER" "$FLOOR_TABLE")"
if [[ "$floor" == "MISSING" || "$floor" -eq 0 ]]; then
  echo "    FAIL  ${FLOOR_TABLE} is empty after restore — the seeded admin always exists, so this dump" >&2
  echo "          captured no data at all and every count-comparison above agreed with itself" >&2
  failed=1
fi

if [[ "$failed" -ne 0 ]]; then
  echo "RESTORE DRILL FAILED — the backups are not currently trustworthy" >&2
  exit 1
fi

echo "==> Restore drill passed"

#!/usr/bin/env bash
# Roda as migrações e os testes do banco num Postgres comum (sem Docker).
# O que faz: recria o banco "ludius_test", aplica a imitação do Supabase,
# todas as migrações e depois cada arquivo de teste pgTAP.
# Uso: PGHOST=... PGUSER=... ./scripts/banco-local/testar.sh
# O jeito oficial (com Docker) é: supabase start && supabase test db
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DB="${TEST_DB:-ludius_test}"

psql -v ON_ERROR_STOP=1 -q -d postgres -c "drop database if exists $DB" -c "create database $DB"
psql -v ON_ERROR_STOP=1 -q -d "$DB" -f "$ROOT/scripts/banco-local/supabase_stub.sql"
psql -v ON_ERROR_STOP=1 -q -d "$DB" -c "create extension if not exists pgtap"

for f in "$ROOT"/supabase/migrations/*.sql; do
  echo "migração: $(basename "$f")"
  psql -v ON_ERROR_STOP=1 -q -d "$DB" -f "$f"
done

failed=0
for f in "$ROOT"/supabase/tests/database/*.test.sql; do
  echo "teste: $(basename "$f")"
  out="$(psql -v ON_ERROR_STOP=1 -X -q -t -A -d "$DB" -f "$f" 2>&1)" || failed=1
  echo "$out" | grep -E '^(not )?ok|^#|ERROR' || true
  if echo "$out" | grep -qE '^not ok|Looks like|ERROR'; then failed=1; fi
done

if [ "$failed" -ne 0 ]; then
  echo "FALHOU"
  exit 1
fi
echo "TUDO CERTO"

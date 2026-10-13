-- One-time hardening for the existing backend-only Supabase project.
-- The application uses the postgres database role through the session pooler;
-- it does not use Supabase's anon/authenticated Data API roles.
-- Run with psql -v ON_ERROR_STOP=1 against the production project.

BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '60s';

DO $preflight$
DECLARE
  table_count integer;
  sequence_count integer;
  function_count integer;
BEGIN
  IF current_user <> 'postgres' THEN
    RAISE EXCEPTION 'Expected postgres role, got %', current_user;
  END IF;

  SELECT count(*) INTO table_count
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public' AND c.relkind IN ('r', 'p')
    AND c.relowner = 'postgres'::regrole;
  SELECT count(*) INTO sequence_count
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public' AND c.relkind = 'S'
    AND c.relowner = 'postgres'::regrole;
  SELECT count(*) INTO function_count
  FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace
  WHERE n.nspname = 'public' AND p.proowner = 'postgres'::regrole;

  IF table_count <> 13 OR sequence_count <> 14 OR function_count <> 1 THEN
    RAISE EXCEPTION 'Unexpected public schema: % tables, % sequences, % functions',
      table_count, sequence_count, function_count;
  END IF;
END
$preflight$;

DO $enable_rls$
DECLARE
  table_record record;
BEGIN
  FOR table_record IN
    SELECT c.relname
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'public' AND c.relkind IN ('r', 'p')
  LOOP
    EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', table_record.relname);
  END LOOP;
END
$enable_rls$;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC, anon, authenticated;

-- Supabase currently grants future objects created by postgres in public to
-- both client roles. Remove those per-schema defaults as well.
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
  REVOKE ALL PRIVILEGES ON TABLES FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
  REVOKE ALL PRIVILEGES ON SEQUENCES FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
  REVOKE ALL PRIVILEGES ON FUNCTIONS FROM anon, authenticated;

DO $verify$
DECLARE
  failures integer;
BEGIN
  SELECT count(*) INTO failures
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public' AND c.relkind IN ('r', 'p')
    AND (
      NOT c.relrowsecurity
      OR has_table_privilege('anon', c.oid, 'SELECT')
      OR has_table_privilege('anon', c.oid, 'INSERT')
      OR has_table_privilege('anon', c.oid, 'UPDATE')
      OR has_table_privilege('anon', c.oid, 'DELETE')
      OR has_table_privilege('authenticated', c.oid, 'SELECT')
      OR has_table_privilege('authenticated', c.oid, 'INSERT')
      OR has_table_privilege('authenticated', c.oid, 'UPDATE')
      OR has_table_privilege('authenticated', c.oid, 'DELETE')
    );
  IF failures <> 0 THEN
    RAISE EXCEPTION '% public tables still allow client-role access or lack RLS', failures;
  END IF;

  SELECT count(*) INTO failures
  FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public' AND c.relkind = 'S'
    AND (has_sequence_privilege('anon', c.oid, 'USAGE')
      OR has_sequence_privilege('authenticated', c.oid, 'USAGE'));
  IF failures <> 0 THEN
    RAISE EXCEPTION '% public sequences still allow client-role use', failures;
  END IF;

  SELECT count(*) INTO failures
  FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace
  WHERE n.nspname = 'public'
    AND (has_function_privilege('anon', p.oid, 'EXECUTE')
      OR has_function_privilege('authenticated', p.oid, 'EXECUTE'));
  IF failures <> 0 THEN
    RAISE EXCEPTION '% public functions still allow client-role execution', failures;
  END IF;

  SELECT count(*) INTO failures
  FROM pg_default_acl d CROSS JOIN LATERAL aclexplode(d.defaclacl) acl
  WHERE d.defaclrole = 'postgres'::regrole
    AND d.defaclnamespace = 'public'::regnamespace
    AND d.defaclobjtype IN ('r', 'S', 'f')
    AND acl.grantee IN ('anon'::regrole, 'authenticated'::regrole);
  IF failures <> 0 THEN
    RAISE EXCEPTION '% postgres public default grants still target client roles', failures;
  END IF;
END
$verify$;

COMMIT;

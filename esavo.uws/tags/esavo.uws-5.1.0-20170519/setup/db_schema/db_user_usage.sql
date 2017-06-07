-- Function: uws2_schema.db_user_usage(text)

-- DROP FUNCTION uws2_schema.db_user_usage(text);

CREATE OR REPLACE FUNCTION uws2_schema.db_user_usage(text)
  RETURNS bigint AS
$BODY$
SELECT COALESCE( (SELECT sum(pg_total_relation_size(quote_ident(schemaname) || '.' || quote_ident(tablename)))::bigint 
		       FROM pg_tables WHERE schemaname = 'user_' || $1 )
		,0)

$BODY$
  LANGUAGE sql VOLATILE
  COST 100;

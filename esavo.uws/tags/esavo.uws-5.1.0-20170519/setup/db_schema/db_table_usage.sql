-- Function: uws2_schema.db_table_usage(text, text)

-- DROP FUNCTION uws2_schema.db_table_usage(text, text);

CREATE OR REPLACE FUNCTION uws2_schema.db_table_usage(
    text,
    text)
  RETURNS bigint AS
$BODY$
SELECT COALESCE( (SELECT pg_total_relation_size(quote_ident($1) || '.' || quote_ident($2)))
		,0)
			
$BODY$
  LANGUAGE sql VOLATILE
  COST 100;

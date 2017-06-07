-- Function: tap_schema.extract_table_metadata_to_tap_schema(character varying, character varying)

-- DROP FUNCTION tap_schema.extract_table_metadata_to_tap_schema(character varying, character varying);

CREATE OR REPLACE FUNCTION tap_schema.extract_table_metadata_to_tap_schema(
    table_schema character varying,
    table_name character varying)
  RETURNS void AS
$BODY$

INSERT INTO tap_schema.all_columns (
SELECT column_name, NULL, NULL, NULL, 

CASE WHEN data_type LIKE '%SMALLINT%' THEN 'SMALLINT'

	WHEN data_type ILIKE '%INTEGER%' THEN 'INTEGER'
	WHEN data_type ILIKE '%BIGINT%' THEN 'BIGINT'
	WHEN data_type ILIKE '%SMALLINT%' THEN 'SMALLINT'
	WHEN data_type ILIKE '%REAL%' THEN 'REAL'
	WHEN data_type ILIKE '%DOUBLE PRECISION%' THEN 'DOUBLE'
	WHEN data_type ILIKE '%BINARY%' THEN 'BINARY'
	WHEN data_type ILIKE '%VARBINARY%' THEN 'VARBINARY'
	WHEN data_type ILIKE '%CHAR%' THEN 'CHAR'
	WHEN data_type ILIKE '%VARCHAR%' THEN 'VARCHAR'
	WHEN data_type ILIKE '%BLOB%' THEN 'BLOB'
	WHEN data_type ILIKE '%CLOB%' THEN 'CLOB'
	WHEN data_type ILIKE '%TIMESTAMP%' THEN 'TIMESTAMP'
	WHEN data_type ILIKE '%POINT%' THEN 'POINT'
	WHEN data_type ILIKE '%REGION%' THEN 'REGION'
	WHEN data_type ILIKE '%BOOLEAN%' THEN 'BOOLEAN'
END,
'', table_name, table_schema, null, 0, 0, 0

FROM information_schema.columns 
WHERE table_schema like $1
  AND table_name   = $2
) $BODY$
  LANGUAGE sql VOLATILE
  COST 100;

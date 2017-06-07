DROP VIEW tap_schema.columns;
DROP VIEW tap_schema.tables;
DROP VIEW tap_schema.schemas;
DROP VIEW tap_schema.keys;

DROP VIEW tap_schema.functions;
DROP VIEW tap_schema.functions_arguments;


DROP INDEX tap_schema.all_functions_arguments_schema_name_function_name_idx;
DROP INDEX tap_schema.all_functions_schema_name_idx;
DROP INDEX tap_schema.all_columns_schema_name_idx;
DROP INDEX tap_schema.all_columns_table_name_idx;
DROP INDEX tap_schema.all_tables_schema_name_idx;


DROP TABLE tap_schema.all_functions_arguments;
DROP TABLE tap_schema.all_functions;

DROP TABLE tap_schema.all_columns;
DROP TABLE tap_schema.all_tables;
DROP TABLE tap_schema.all_schemas;

DROP TABLE tap_schema.key_columns;
DROP TABLE tap_schema.all_keys;

DROP FUNCTION tap_schema.extract_table_metadata_to_tap_schema(character varying, character varying);

DROP SCHEMA tap_schema;


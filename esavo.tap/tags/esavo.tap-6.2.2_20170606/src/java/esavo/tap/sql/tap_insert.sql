-- Initial inserts: make tap_schema all_tables info available
INSERT INTO tap_schema.all_schemas (schema_name, public) values ('public', true);
INSERT INTO tap_schema.all_schemas (schema_name, public) values ('tap_upload', true);
INSERT INTO tap_schema.all_schemas (schema_name, public) values ('tap_schema', true);

INSERT INTO tap_schema.all_tables (table_name, schema_name, table_type, description, public) values ('schemas','tap_schema','table', 'TAP SCHEMA schemas', true);
INSERT INTO tap_schema.all_tables (table_name, schema_name, table_type, description, public) values ('tables','tap_schema','table', 'TAP SCHEMA tables', true);
INSERT INTO tap_schema.all_tables (table_name, schema_name, table_type, description, public) values ('columns','tap_schema','table', 'TAP SCHEMA columns', true);
INSERT INTO tap_schema.all_tables (table_name, schema_name, table_type, description, public) values ('keys','tap_schema','table', 'TAP SCHEMA keys', true);
INSERT INTO tap_schema.all_tables (table_name, schema_name, table_type, description, public) values ('key_columns','tap_schema','table', 'TAP SCHEMA key columns', true);

INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('schema_name', 'schemas', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('db_schema_name', 'schemas', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('description', 'schemas', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('utype', 'schemas', 'tap_schema', 'VARCHAR', true);

INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('table_name', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('db_table_name', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('schema_name', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('table_type', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('description', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('utype', 'tables', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('size', 'tables', 'tap_schema', 'INTEGER', true);

INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('column_name', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('db_column_name', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('description', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('ucd', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('utype', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('datatype', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('unit', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('table_name', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('schema_name', 'columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('size', 'columns', 'tap_schema', 'INTEGER', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('principal', 'columns', 'tap_schema', 'INTEGER', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('std', 'columns', 'tap_schema', 'INTEGER', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('indexed', 'columns', 'tap_schema', 'INTEGER', true);

INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('key_id', 'keys', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('from_table', 'keys', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('target_table', 'keys', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('description', 'keys', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('utype', 'keys', 'tap_schema', 'VARCHAR', true);

INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('key_id', 'key_columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('from_column', 'key_columns', 'tap_schema', 'VARCHAR', true);
INSERT INTO tap_schema.all_columns (column_name, table_name, schema_name, datatype, public) values ('target_column', 'key_columns', 'tap_schema', 'VARCHAR', true);




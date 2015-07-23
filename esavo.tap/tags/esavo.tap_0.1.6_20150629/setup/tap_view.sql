-- DROP VIEW tap_schema.tables;
-- DROP VIEW tap_schema.schemas;
-- DROP VIEW tap_schema.columns;
-- DROP VIEW tap_schema.keys;

-- DROP VIEW tap_schema.functions;
-- DROP VIEW tap_schema.functions_arguments;


-- DROP TABLE tap_schema.all_functions_arguments;
-- DROP TABLE tap_schema.all_functions;

-- DROP TABLE tap_schema.all_columns;
-- DROP TABLE tap_schema.all_tables;
-- DROP TABLE tap_schema.all_schemas;

-- DROP TABLE tap_schema.key_columns;
-- DROP TABLE tap_schema.all_keys;



CREATE SCHEMA tap_upload;
CREATE SCHEMA tap_schema;


-- Table: tap_schema.all_schemas

CREATE TABLE tap_schema.all_schemas
(
  schema_name varchar NOT NULL,
  description varchar,
  utype varchar,
  CONSTRAINT all_schemas_pk PRIMARY KEY (schema_name)
);


-- Table: tap_schema.all_tables

CREATE TABLE tap_schema.all_tables
(
  table_name varchar NOT NULL,
  schema_name varchar NOT NULL,
  table_type varchar,
  description varchar,
  utype varchar,
  size integer,
  flags integer,
  CONSTRAINT all_tables_pk PRIMARY KEY (schema_name, table_name),
  CONSTRAINT table_all_schemas_fk FOREIGN KEY (schema_name)
      REFERENCES tap_schema.all_schemas (schema_name)
);


-- Table: tap_schema.all_columns

CREATE TABLE tap_schema.all_columns
(
  column_name varchar NOT NULL,
  description varchar,
  ucd varchar,
  utype varchar,
  datatype varchar,
  unit varchar,
  table_name varchar NOT NULL,
  schema_name varchar NOT NULL,
  size integer,
  principal integer NOT NULL DEFAULT 0,
  std integer NOT NULL DEFAULT 0,
  indexed integer NOT NULL DEFAULT 0,
  flags integer,
  CONSTRAINT all_columns_pk PRIMARY KEY (schema_name, table_name, column_name),
  CONSTRAINT all_columns_all_tables_fk FOREIGN KEY (schema_name, table_name)
      REFERENCES tap_schema.all_tables (schema_name, table_name)
);


-- Table: tap_schema.all_keys

CREATE TABLE tap_schema.all_keys
(
  key_id varchar NOT NULL,
  from_schema varchar NOT NULL,
  from_table varchar NOT NULL,
  target_table varchar NOT NULL,
  target_schema varchar NOT NULL,
  description varchar,
  utype varchar,
  CONSTRAINT all_keys_pk PRIMARY KEY (key_id)
);


-- Table: tap_schema.key_columns

CREATE TABLE tap_schema.key_columns
(
  key_id varchar NOT NULL,
  from_column varchar NOT NULL,
  target_column varchar NOT NULL,
  CONSTRAINT key_columns_pk PRIMARY KEY (key_id),
  CONSTRAINT key_columns_all_keys_fk FOREIGN KEY (key_id) REFERENCES tap_schema.all_keys (key_id)
);



-- Table: tap_schema.all_functions;

CREATE TABLE tap_schema.all_functions
(
  function_name varchar NOT NULL,
  schema_name varchar NOT NULL,
  return_type varchar NOT NULL,
  description varchar,
  CONSTRAINT all_functions_pk PRIMARY KEY (schema_name, function_name),
  CONSTRAINT all_functions_all_schemas_fk FOREIGN KEY (schema_name)
      REFERENCES tap_schema.all_schemas (schema_name)
);

-- Table: tap_schema.all_functions_arguments;

CREATE TABLE tap_schema.all_functions_arguments
(
  argument_name varchar NOT NULL,
  description varchar,
  arg_type varchar NOT NULL,
  function_name varchar NOT NULL,
  schema_name varchar NOT NULL,
  default_value varchar,
  max_value varchar,
  min_value varchar,
  CONSTRAINT all_functions_arguments_pk PRIMARY KEY (schema_name, function_name, argument_name),
  CONSTRAINT all_functions_arguments_all_functions_fk FOREIGN KEY (schema_name, function_name)
      REFERENCES tap_schema.all_functions (schema_name, function_name)
);


-- tables view: only 'public' and 'tap_schema' schemas are available

CREATE VIEW tap_schema.schemas AS SELECT * FROM tap_schema.all_schemas WHERE schema_name IN ('public','tap_schema');
CREATE VIEW tap_schema.tables AS SELECT * FROM tap_schema.all_tables WHERE schema_name IN ('public','tap_schema');
CREATE VIEW tap_schema.columns AS SELECT * FROM tap_schema.all_columns WHERE schema_name IN ('public','tap_schema');
CREATE VIEW tap_schema.keys AS SELECT * FROM tap_schema.all_keys WHERE from_schema IN ('public','tap_schema') AND target_schema IN ('public','tap_schema');

CREATE VIEW tap_schema.functions AS SELECT * FROM tap_schema.all_functions WHERE schema_name IN ('public','tap_schema');
CREATE VIEW tap_schema.functions_arguments AS SELECT * FROM tap_schema.all_functions_arguments WHERE schema_name IN ('public','tap_schema');



-- Initial inserts: make tap_schema all_tables info available

insert into tap_schema.all_schemas (schema_name) values ('tap_schema');

insert into tap_schema.all_tables (table_name, schema_name, table_type, description) values ('schemas','tap_schema','table', 'TAP SCHEMA schemas');
insert into tap_schema.all_tables (table_name, schema_name, table_type, description) values ('tables','tap_schema','table', 'TAP SCHEMA tables');
insert into tap_schema.all_tables (table_name, schema_name, table_type, description) values ('columns','tap_schema','table', 'TAP SCHEMA columns');
insert into tap_schema.all_tables (table_name, schema_name, table_type, description) values ('keys','tap_schema','table', 'TAP SCHEMA keys');
insert into tap_schema.all_tables (table_name, schema_name, table_type, description) values ('key_columns','tap_schema','table', 'TAP SCHEMA key columns');

insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('schema_name', 'schemas', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('description', 'schemas', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('utype', 'schemas', 'tap_schema', 'VARCHAR');

insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('table_name', 'tables', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('schema_name', 'tables', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('table_type', 'tables', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('description', 'tables', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('utype', 'tables', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('size', 'tables', 'tap_schema', 'INTEGER');

insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('column_name', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('description', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('ucd', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('utype', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('datatype', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('unit', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('table_name', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('schema_name', 'columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('size', 'columns', 'tap_schema', 'INTEGER');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('principal', 'columns', 'tap_schema', 'INTEGER');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('std', 'columns', 'tap_schema', 'INTEGER');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('indexed', 'columns', 'tap_schema', 'INTEGER');

insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('key_id', 'keys', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('from_table', 'keys', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('target_table', 'keys', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('description', 'keys', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('utype', 'keys', 'tap_schema', 'VARCHAR');

insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('key_id', 'key_columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('from_column', 'key_columns', 'tap_schema', 'VARCHAR');
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype) values ('target_column', 'key_columns', 'tap_schema', 'VARCHAR');


-- DROP VIEW public.dual;
-- View: dual
CREATE SCHEMA public;
--CREATE OR REPLACE VIEW public.dual AS SELECT 1;
CREATE OR REPLACE VIEW public.dual AS SELECT 1 AS dummy;

insert into tap_schema.all_schemas (schema_name) values ('public');
insert into tap_schema.all_tables (table_name, schema_name, table_type, description,size) values ('dual','public','table', 'auxiliary table to be used as dummy "from"',1);
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype, principal, std) values ('dummy', 'dual', 'public', 'INTEGER', 0, 0);



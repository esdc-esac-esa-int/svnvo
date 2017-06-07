
CREATE SCHEMA tap_schema;

-- Table: tap_schema.all_schemas

CREATE TABLE tap_schema.all_schemas
(
  schema_name varchar NOT NULL,
  db_schema_name varchar,
  description varchar,
  utype varchar,
  title varchar,
  public boolean DEFAULT FALSE,
  CONSTRAINT all_schemas_pk PRIMARY KEY (schema_name)
);


-- Table: tap_schema.all_tables

CREATE TABLE tap_schema.all_tables
(
  table_name varchar NOT NULL,
  db_table_name varchar,
  schema_name varchar NOT NULL,
  table_type varchar,
  description varchar,
  utype varchar,
  size bigint,
  flags integer,
  public boolean DEFAULT FALSE,
  hierarchy varchar,
  CONSTRAINT all_tables_pk PRIMARY KEY (schema_name, table_name),
  CONSTRAINT table_all_schemas_fk FOREIGN KEY (schema_name)
      REFERENCES tap_schema.all_schemas (schema_name)
);

--ADD TABLESPACE IF NEEDED
CREATE INDEX all_tables_schema_name_idx ON tap_schema.all_tables (schema_name);

-- Table: tap_schema.all_columns

CREATE TABLE tap_schema.all_columns
(
  column_name varchar NOT NULL,
  db_column_name varchar,
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
  pos integer DEFAULT -1,
  public boolean DEFAULT FALSE,
  array_type varchar,
  array_dims varchar,
  CONSTRAINT all_columns_pk PRIMARY KEY (schema_name, table_name, column_name),
  CONSTRAINT all_columns_all_tables_fk FOREIGN KEY (schema_name, table_name)
      REFERENCES tap_schema.all_tables (schema_name, table_name)
);

--ADD TABLESPACE IF NEEDED
CREATE INDEX all_columns_schema_name_idx ON tap_schema.all_columns (schema_name);
CREATE INDEX all_columns_table_name_idx ON tap_schema.all_columns (table_name);


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
  public boolean DEFAULT FALSE,
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
  public boolean DEFAULT FALSE,
  CONSTRAINT all_functions_pk PRIMARY KEY (schema_name, function_name),
  CONSTRAINT all_functions_all_schemas_fk FOREIGN KEY (schema_name)
      REFERENCES tap_schema.all_schemas (schema_name)
);

--ADD TABLESPACE IF NEEDED
CREATE INDEX all_functions_schema_name_idx ON tap_schema.all_functions (schema_name);

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
  public boolean DEFAULT FALSE,
  CONSTRAINT all_functions_arguments_pk PRIMARY KEY (schema_name, function_name, argument_name),
  CONSTRAINT all_functions_arguments_all_functions_fk FOREIGN KEY (schema_name, function_name)
      REFERENCES tap_schema.all_functions (schema_name, function_name)
);

--ADD TABLESPACE IF NEEDED
CREATE INDEX all_functions_arguments_schema_name_function_name_idx ON tap_schema.all_functions_arguments (schema_name, function_name);


-- tables view: only 'public' and 'tap_schema' schemas are available

CREATE VIEW tap_schema.schemas AS SELECT * FROM tap_schema.all_schemas WHERE public=true;

-- Filter all tables with no public schema and public flag false.
CREATE VIEW tap_schema.tables AS SELECT all_tables.* 
	FROM tap_schema.all_tables, tap_schema.schemas
	WHERE all_tables.schema_name=schemas.schema_name
	AND all_tables.public = true;

-- Filter all tables with no public table and public flag false.
CREATE VIEW tap_schema.columns AS SELECT all_columns.*
	FROM tap_schema.all_columns, tap_schema.tables, tap_schema.schemas
	WHERE all_columns.table_name=tables.table_name
	AND all_columns.schema_name=schemas.schema_name
	AND all_columns.public = true
	ORDER BY pos, column_name;



CREATE VIEW tap_schema.keys AS SELECT * FROM tap_schema.all_keys WHERE public=true;

CREATE VIEW tap_schema.functions AS SELECT * FROM tap_schema.all_functions WHERE public=true;
CREATE VIEW tap_schema.functions_arguments AS SELECT * FROM tap_schema.all_functions_arguments WHERE public=true;


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




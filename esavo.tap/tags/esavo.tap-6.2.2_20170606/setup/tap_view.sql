
-- ---------------------------------------------
-- EXECUTE THE FOLLOWING SCRIPTS BEFORE THIS ONE
-- execute src/java/esavo/tap/sql/tap_create.sql
-- execute src/java/esavo/tap/sql/tap_insert.sql
-- ---------------------------------------------


-- View: dual
-- CREATE SCHEMA public;
--CREATE OR REPLACE VIEW public.dual AS SELECT 1;
CREATE OR REPLACE VIEW public.dual AS SELECT 1 AS dummy;

insert into tap_schema.all_tables (table_name, schema_name, table_type, description,size, public) values ('dual','public','table', 'auxiliary table to be used as dummy "from"',1, true);
insert into tap_schema.all_columns (column_name, table_name, schema_name, datatype, principal, std, public) values ('dummy', 'dual', 'public', 'INTEGER', 0, 0, true);

-- tap upload
CREATE SCHEMA tap_upload;

-- tap config: multiple schemas
CREATE SCHEMA tap_config;

CREATE TABLE tap_config.user_schema
(
  user_id character varying NOT NULL,
  schema_name character varying NOT NULL,
  CONSTRAINT user_schema_pk PRIMARY KEY (user_id)
);
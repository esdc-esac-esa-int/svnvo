-- DROP TABLE uws2_schema.job_parameters;
-- DROP TABLE uws2_schema.results_meta;
-- DROP TABLE uws2_schema.error_summary_meta;
-- DROP TABLE uws2_schema.jobs_meta;
-- DROP TABLE uws2_schema.owner_parameters;
-- DROP TABLE uws2_schema.owners;

-- DROP SCHEMA uws2_schema;


CREATE SCHEMA uws2_schema;


-- Table uws2_schema.owners

CREATE TABLE uws2_schema.owners
(
  owner_id varchar NOT NULL,
  auth_name varchar,
  pseudo varchar,
  roles integer,
  CONSTRAINT owners_pk PRIMARY KEY (owner_id)
);

-- Table uws2_schema.owner_parameters

CREATE TABLE uws2_schema.owner_parameters
(
  owner_id character varying NOT NULL,
  parameter_id character varying NOT NULL,
  description character varying,
  data_type varchar NOT NULL,
  string_representation character varying,
  CONSTRAINT owner_parameters_pk PRIMARY KEY (owner_id, parameter_id),
  CONSTRAINT owner_parameters_owner_id_fk FOREIGN KEY (owner_id)
      REFERENCES uws2_schema.owners (owner_id)
);

CREATE INDEX ON uws2_schema.owner_parameters (owner_id); --ADD TABLESPACE IF NEEDED

-- Table uws2_schema.jobs_meta

CREATE TABLE uws2_schema.jobs_meta
(
  job_id varchar NOT NULL,
  list_id varchar NOT NULL,
  owner_id varchar NOT NULL,
  session_id varchar,
  phase_id varchar,
  job_name varchar,
  quote bigint,
  start_time bigint,
  end_time bigint,
  destruction_time bigint,
  execution_duration bigint,
  relative_path varchar,
  priority integer,
  creation_time bigint NOT NULL,
  CONSTRAINT jobs_meta_pk PRIMARY KEY (job_id),
  CONSTRAINT owners_owner_id_fk FOREIGN KEY (owner_id)
      REFERENCES uws2_schema.owners (owner_id)
);

CREATE INDEX ON uws2_schema.jobs_meta (owner_id); --ADD TABLESPACE IF NEEDED


-- Table uws2_schema.error_summary_meta

CREATE TABLE uws2_schema.error_summary_meta
(
  job_id varchar NOT NULL,
  message varchar,
  type varchar,
  details boolean,
  details_size varchar,
  details_mime_type varchar,
  CONSTRAINT error_summary_meta_pk PRIMARY KEY (job_id),
  CONSTRAINT error_summary_meta_job_meta_id_fk FOREIGN KEY (job_id)
      REFERENCES uws2_schema.jobs_meta (job_id)
);


-- Table uws2_schema.results_meta

CREATE TABLE uws2_schema.results_meta
(
  result_id varchar NOT NULL,
  job_id varchar NOT NULL,
  type varchar,
  mime_type varchar,
  size bigint,
  rows bigint,
  content_encoding varchar,
  CONSTRAINT results_meta_pk PRIMARY KEY (result_id, job_id),
  CONSTRAINT results_meta_job_meta_id_fk FOREIGN KEY (job_id)
      REFERENCES uws2_schema.jobs_meta (job_id)
);
CREATE INDEX ON uws2_schema.results_meta (job_id); --ADD TABLESPACE IF NEEDED


-- Table uws2_schema.job_parameters

CREATE TABLE uws2_schema.job_parameters
(
  parameter_id varchar NOT NULL,
  job_id varchar NOT NULL,
  parameter_type varchar NOT NULL,
  data_type varchar NOT NULL,
  string_representation varchar,
  CONSTRAINT job_parameters_pk PRIMARY KEY (parameter_id, job_id, parameter_type),
  CONSTRAINT job_parameters_job_meta_id_fk FOREIGN KEY (job_id)
      REFERENCES uws2_schema.jobs_meta (job_id)
);

CREATE INDEX ON uws2_schema.job_parameters (job_id); --ADD TABLESPACE IF NEEDED



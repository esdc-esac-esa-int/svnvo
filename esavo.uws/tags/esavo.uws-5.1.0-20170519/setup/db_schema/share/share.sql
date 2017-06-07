

-- DROP TABLE share.resource_visibility;
-- DROP TABLE share.share_relation;
-- DROP TABLE share.resource;
-- DROP TABLE share.user_group;
-- DROP TABLE share.group;
-- DROP TABLE share.resource_type;
-- DROP TABLE share.user;
-- DROP TABLE share.share_mode;

CREATE SCHEMA share;

CREATE TABLE share.share_mode (
  mode_id INTEGER   NOT NULL ,
  description VARCHAR      ,
CONSTRAINT share_mode_pk PRIMARY KEY(mode_id));



CREATE TABLE share.user (
  user_id VARCHAR   NOT NULL   ,
CONSTRAINT user_pk PRIMARY KEY(user_id));



CREATE TABLE share.resource_type (
  type_id INTEGER   NOT NULL ,
  description VARCHAR      ,
CONSTRAINT resource_type_pk PRIMARY KEY(type_id));



CREATE TABLE share.group (
  group_id VARCHAR   NOT NULL ,
  user_id VARCHAR   NOT NULL ,
  title VARCHAR    ,
  description VARCHAR      ,
  CONSTRAINT group_pk PRIMARY KEY(group_id),
  CONSTRAINT group_owner_fk FOREIGN KEY (user_id)
    REFERENCES share.user(user_id));



CREATE TABLE share.user_group (
  user_id VARCHAR   NOT NULL ,
  group_id VARCHAR   NOT NULL   ,
  CONSTRAINT user_group_pk PRIMARY KEY(user_id, group_id),
  CONSTRAINT user_group_user_fk FOREIGN KEY (user_id)
    REFERENCES share.user(user_id),
  CONSTRAINT user_group_group_fk FOREIGN KEY (group_id)
    REFERENCES share.group(group_id));


CREATE TABLE share.resource (
  resource_id VARCHAR  NOT NULL ,
  user_id VARCHAR   NOT NULL ,
  type_id INTEGER   NOT NULL   ,
  title VARCHAR NOT NULL,
  description VARCHAR,
  CONSTRAINT resource_pk PRIMARY KEY(resource_id),
  CONSTRAINT resource_resource_type_fk FOREIGN KEY (type_id)
    REFERENCES share.resource_type(type_id),
  CONSTRAINT resource_owner_fk FOREIGN KEY (user_id)
    REFERENCES share.user(user_id));



CREATE TABLE share.share_relation (
  resource_id VARCHAR   NOT NULL ,
  group_id VARCHAR   NOT NULL ,
  mode_id INTEGER      ,
  CONSTRAINT share_relation_pk PRIMARY KEY(resource_id, group_id),
  CONSTRAINT share_relation_resource_fk FOREIGN KEY (resource_id)
    REFERENCES share.resource(resource_id),
  CONSTRAINT share_relation_group_fk FOREIGN KEY (group_id)
    REFERENCES share.group(group_id),
  CONSTRAINT share_relation_share_mode FOREIGN KEY (mode_id)
    REFERENCES share.share_mode(mode_id));



CREATE TABLE share.resource_visibility (
  user_id VARCHAR   NOT NULL ,
  resource_id VARCHAR   NOT NULL   ,
  CONSTRAINT resource_visibility_pk PRIMARY KEY(user_id, resource_id),
  CONSTRAINT resource_visibility_user_fk FOREIGN KEY (user_id)
    REFERENCES share.user(user_id),
  CONSTRAINT resource_visibility_resource_fk FOREIGN KEY (resource_id)
    REFERENCES share.resource(resource_id));



INSERT INTO share.share_mode (mode_id, description) VALUES (0, 'Read Only');
INSERT INTO share.share_mode (mode_id, description) VALUES (1, 'Read/Write');

INSERT INTO share.resource_type (type_id, description) VALUES (0, 'Table');
INSERT INTO share.resource_type (type_id, description) VALUES (1, 'Job');

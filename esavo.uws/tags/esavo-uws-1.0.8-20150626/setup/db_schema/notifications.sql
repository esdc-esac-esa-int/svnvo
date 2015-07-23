-- DROP TABLE notifications_schema.notifications_users;
-- DROP TABLE notifications_schema.notifications;

-- DROP SCHEMA notifications_schema;

CREATE SCHEMA notifications_schema;

-- Tables

CREATE TABLE notifications_schema.notifications
(
	notification_id varchar,
	type integer,
	subtype integer,
	description varchar,
	creation_time bigint,
	CONSTRAINT notifications_pk PRIMARY KEY (notification_id)
);

CREATE TABLE notifications_schema.notifications_users
(
	notification_id varchar,
	user_id varchar,
	CONSTRAINT notifications_users_pk PRIMARY KEY (notification_id, user_id),
	CONSTRAINT notifications_users_notification_fk FOREIGN KEY (notification_id)
		REFERENCES notifications_schema.notifications (notification_id)
);


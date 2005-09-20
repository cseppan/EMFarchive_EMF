CREATE TABLE dataset_access_logs
(
  access_log_id serial NOT NULL,
  dataset_id int4 NOT NULL,
  username varchar(255) NOT NULL,
  date timestamp NOT NULL,
  version varchar(128) NOT NULL,
  description text NOT NULL,
  "location" varchar(255) NOT NULL,
  CONSTRAINT dataset_access_logs_access_log_id_key UNIQUE (access_log_id)
) 
WITHOUT OIDS;
ALTER TABLE dataset_access_logs OWNER TO emf;


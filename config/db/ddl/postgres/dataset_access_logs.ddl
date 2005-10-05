CREATE TABLE dataset_access_logs
(
  access_log_id serial NOT NULL,
  dataset_id int4 REFERENCES datasets (dataset_id) NOT NULL,
  username varchar(255) REFERENCES users (user_name) NOT NULL,
  date timestamp NOT NULL,
  version varchar(128) NOT NULL,
  description text NOT NULL,
  "location" varchar(255) NOT NULL,
  CONSTRAINT dataset_access_logs_access_log_id_key UNIQUE (access_log_id),
  CONSTRAINT dataset_access_logs_pkey PRIMARY KEY (access_log_id),
  CONSTRAINT dataset_access_logs_to_datasets FOREIGN KEY (dataset_id)
      REFERENCES datasets (dataset_id)
) 
WITHOUT OIDS;
ALTER TABLE dataset_access_logs OWNER TO emf;


CREATE TABLE statusmessages
(
  status_id serial NOT NULL,
  user_name varchar(255) NOT NULL,
  msg_type varchar(255) NOT NULL,
  message text NOT NULL,
  msg_read bool NOT NULL DEFAULT false,
  date timestamp NOT NULL,
  CONSTRAINT statusmessages_status_id_key UNIQUE (status_id)
) 
WITHOUT OIDS;
ALTER TABLE statusmessages OWNER TO emf;

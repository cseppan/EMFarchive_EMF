CREATE TABLE statusmessages
(
  user_name varchar(255) NOT NULL,
  msg_type varchar(255) NOT NULL,
  message text NOT NULL,
  id varchar(32) NOT NULL,
  msg_read bool NOT NULL DEFAULT false,
  date timestamp NOT NULL
) 
WITHOUT OIDS;
ALTER TABLE statusmessages OWNER TO emf;

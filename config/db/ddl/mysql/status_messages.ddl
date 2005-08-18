CREATE TABLE emf.statusmessages
(
  user_name varchar(15) NOT NULL,
  msg_type varchar(255) NOT NULL,
  message varchar(255) NOT NULL,
  id varchar(32) NOT NULL,
  msg_read bool NOT NULL DEFAULT false,
  date timestamp NOT NULL
);
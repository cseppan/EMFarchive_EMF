CREATE TABLE emf.users
(
  user_name varchar(255) NOT NULL,
  user_pass varchar(15) NOT NULL,
  fullname varchar(255) NOT NULL,
  affiliation varchar(255) NOT NULL,
  workphone varchar(15) NOT NULL,
  emailaddr varchar(128) NOT NULL,
  inadmingrp bool NOT NULL DEFAULT false,
  acctdisabled bool NOT NULL DEFAULT false,
  CONSTRAINT pk_users PRIMARY KEY (user_name)
) 
WITH OIDS;
ALTER TABLE emf.users OWNER TO emf;
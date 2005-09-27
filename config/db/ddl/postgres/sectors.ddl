DROP TABLE sectors;

CREATE SEQUENCE sectors_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 199
  CACHE 1;
ALTER TABLE sectors_id_seq OWNER TO emf;

CREATE TABLE sectors
(
  id int4 NOT NULL DEFAULT nextval('emf.sectors_id_seq'::text),
  name varchar(255) NOT NULL,
  description text,
  CONSTRAINT sectors_id_key UNIQUE (id)
) 
WITHOUT OIDS;
ALTER TABLE sectors OWNER TO emf;
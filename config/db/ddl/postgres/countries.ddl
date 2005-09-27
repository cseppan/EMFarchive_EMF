DROP TABLE countries;

CREATE SEQUENCE countries_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 199
  CACHE 1;
ALTER TABLE countries_id_seq OWNER TO emf;

CREATE TABLE countries
(
  id int4 NOT NULL DEFAULT nextval('emf.countries_id_seq'::text),
  name varchar(255) NOT NULL,
  CONSTRAINT countries_id_key UNIQUE (id)
) 
WITHOUT OIDS;
ALTER TABLE countries OWNER TO emf;



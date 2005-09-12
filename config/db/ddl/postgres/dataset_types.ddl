CREATE TABLE emf.datasettypes
(
  name varchar(128) NOT NULL,
  id int4 NOT NULL DEFAULT 0,
  description text NOT NULL,
  minfiles int2 NOT NULL DEFAULT 0,
  maxfiles int2 NOT NULL DEFAULT 0,
  uid varchar(32)
) 
WITHOUT OIDS;
ALTER TABLE emf.datasettypes OWNER TO emf;
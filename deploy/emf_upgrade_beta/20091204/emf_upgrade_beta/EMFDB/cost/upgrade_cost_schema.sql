--added (10/16/09) for use with control measures
CREATE TABLE emf.references
(
   id serial NOT NULL,
   description text,
   lock_owner character varying(255),
   lock_date timestamp without time zone,
   CONSTRAINT references_pkey PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE emf.references OWNER TO emf;

--added (10/16/09) for use with control measures
-- emf control_measure_references
CREATE TABLE emf.control_measure_references
(
   id SERIAL PRIMARY KEY,
   control_measure_id int4 NOT NULL REFERENCES emf.control_measures(id) ,
   list_index int4,
   reference_id int4 REFERENCES emf.references (id),
   UNIQUE (control_measure_id, reference_id)
)
WITHOUT OIDS;
ALTER TABLE emf.control_measure_references OWNER TO emf;
CREATE INDEX control_measure_references_id
   ON emf.control_measure_references
   USING btree
   (id);
CREATE INDEX control_measure_references_measure_id
   ON emf.control_measure_references
   USING btree
   (control_measure_id);
CREATE INDEX control_measure_references_reference_id
   ON emf.control_measure_references
   USING btree
   (reference_id);
-- emf control_measure_references

-- 11/10/09 - changed the filter to be text instead of varchar(255)
ALTER TABLE emf.control_strategies ALTER filter TYPE text;

-- 11/12/09 - added unique constraint to the reference description
ALTER TABLE emf."references" ADD CONSTRAINT uq_reference_description UNIQUE (description);

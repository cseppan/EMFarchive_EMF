--added (10/1/09) to allow search datasets on KeyVal
ALTER TABLE emf.datasets_keywords ADD COLUMN kwname varchar(255) NOT NULL DEFAULT '';
UPDATE emf.datasets_keywords AS kw SET kwname = (SELECT name FROM emf.keywords WHERE id = kw.keyword_id);

--added (10/7/09) to add descriptions to dataset versions
ALTER TABLE emf.versions ADD COLUMN description text NOT NULL DEFAULT '';

--added (10/13/09) to add intended use to dataset versions
ALTER TABLE emf.versions ADD COLUMN intended_use int4 REFERENCES emf.intended_uses (id) DEFAULT 1;

--added (10/16/09) for use with control measures
CREATE TABLE "references"
(
  id serial NOT NULL,
  description text,
  lock_owner character varying(255),
  lock_date timestamp without time zone,
  CONSTRAINT references_pkey PRIMARY KEY (id)
)
WITH (OIDS=FALSE);
ALTER TABLE "references" OWNER TO emf;

--added (10/21/09) to allow search datasets on dataset types' KeyVal
ALTER TABLE emf.dataset_types_keywords ADD COLUMN kwname varchar(255) NOT NULL DEFAULT '';
UPDATE emf.dataset_types_keywords AS kw SET kwname = (SELECT name FROM emf.keywords WHERE id = kw.keyword_id);

-- added (11/9/2009) upon request from Alison
ALTER TABLE emf.dataset_types_keywords ALTER value TYPE text;
ALTER TABLE emf.datasets_keywords ALTER value TYPE text;

-- Function: emf.create_orl_table_indexes(table_name character varying)

-- DROP FUNCTION emf.create_orl_table_indexes(table_name character varying);

CREATE OR REPLACE FUNCTION public.create_orl_table_indexes(table_name character varying)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(63) := '';
	is_point_table boolean := false;
BEGIN

	-- see if there are point specific columns to be indexed
	SELECT count(1) = 4
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = lower(table_name)
		and a.attname in ('plantid','pointid','stackid','segment')
		AND a.attnum > 0
	into is_point_table;

	-- Create Indexes....

	-- create fips btree index
	IF length('fips_' || table_name) >= 63 - 5 THEN
		index_name := 'fips_' || substr(table_name, 6, 63);
	ELSE
		index_name := 'fips_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(fips)';

	-- create poll btree index
	IF length('poll_' || table_name) >= 63 - 5 THEN
		index_name := 'poll_' || substr(table_name, 6, 63);
	ELSE
		index_name := 'poll_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(poll)';

	-- create scc btree index
	IF length('scc_' || table_name) >= 63 - 4 THEN
		index_name := 'scc_' || substr(table_name, 5, 63);
	ELSE
		index_name := 'scc_' || table_name;
	END IF;
	execute 'CREATE INDEX ' || index_name || '
			ON emissions.' || table_name || '
			USING btree
			(scc)';

	-- add point specific indexes--plantid, pointid, stackid, segment
	IF is_point_table THEN
		-- create plantid btree index
		IF length('plantid_' || table_name) >= 63 - 8 THEN
			index_name := 'plantid_' || substr(table_name, 9, 63);
		ELSE
			index_name := 'plantid_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(plantid)';

		-- create pointid btree index
		IF length('pointid_' || table_name) >= 63 - 8 THEN
			index_name := 'pointid_' || substr(table_name, 9, 63);
		ELSE
			index_name := 'pointid_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(pointid)';

		-- create stackid btree index
		IF length('stackid_' || table_name) >= 63 - 8 THEN
			index_name := 'stackid_' || substr(table_name, 9, 63);
		ELSE
			index_name := 'stackid_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(stackid)';

		-- create segment btree index
		IF length('segment_' || table_name) >= 63 - 8 THEN
			index_name := 'segment_' || substr(table_name, 9, 63);
		ELSE
			index_name := 'segment_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(segment)';
	END IF;

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.create_orl_table_indexes(table_name character varying) OWNER TO emf;

--select public.create_orl_table_indexes('csdr_1081_v0_20071107011535p2020_annualsum_25oct2007_v0_orl_txt');


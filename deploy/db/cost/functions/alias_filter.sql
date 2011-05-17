CREATE OR REPLACE FUNCTION public.alias_filter(filter text, table_name character varying(64), table_alias character varying(64))
  RETURNS text AS
$BODY$
DECLARE
	table_column record;
	aliased_filter text := filter;
BEGIN

		
		FOR table_column IN EXECUTE 
			'SELECT a.attname as column_name
			FROM pg_class c
				inner join pg_attribute a
				on a.attrelid = c.oid
				inner join pg_type t
				on t.oid = a.atttypid
			WHERE c.relname = ' || lower(quote_literal(table_name)) || '
				AND a.attnum > 0
			order by a.attname desc'	
-- NOTE keep sort order this make sure we don't want to double alias columns with similar names (i.e., scc and scc_code)
		LOOP
			aliased_filter := regexp_replace(aliased_filter, table_column.column_name, table_alias || '.' || table_column.column_name, 'gi');
		END LOOP;


	return aliased_filter;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE STRICT
  COST 100;
ALTER FUNCTION public.alias_filter(text, character varying(64), character varying(64)) OWNER TO emf;

--select public.alias_filter('compliance_date = ''''', 'ds_control_ptnonipm_2020ce_test_14422549', 'cp');



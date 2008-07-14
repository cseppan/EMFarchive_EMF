CREATE OR REPLACE FUNCTION public.alias_inventory_filter(filter text, table_alias varchar)
  RETURNS text AS
$BODY$
DECLARE
BEGIN

	RETURN 
		replace(
		replace(
			replace(
			replace(
				replace(
				replace(
					replace(
					replace(
						replace(
						replace(
							replace(
							replace(
								filter, 
							'scc', table_alias || '.scc'), 
							'SCC', table_alias || '.scc'), 
						'fips', table_alias || '.fips'), 
						'FIPS', table_alias || '.fips'), 
					'plantid', table_alias || '.plantid'), 
					'PLANTID', table_alias || '.PLANTID'), 
				'pointid', table_alias || '.pointid'), 
				'POINTID', table_alias || '.pointid'), 
			'stackid', table_alias || '.stackid'), 
			'STACKID', table_alias || '.stackid'), 
		'segment', table_alias || '.segment'), 
		'SEGMENT', table_alias || '.segment');

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE
  COST 100;
ALTER FUNCTION public.alias_inventory_filter(text, varchar) OWNER TO emf;

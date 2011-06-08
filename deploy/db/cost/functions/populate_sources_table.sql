drop FUNCTION public.populate_sources_table(
	inv_table_name varchar(64),
	inv_filter text);

CREATE OR REPLACE FUNCTION public.populate_sources_table(
	inv_table_name varchar(64),
	inv_filter text
) RETURNS void AS $$
DECLARE
	is_point_table boolean := false;
BEGIN
	-- see if there are point specific columns to be indexed
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	execute 
	'insert into emf.sources (scc, fips ' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ', source)
	select distinct on (inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ')
		inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ',
		inv.scc || inv.fips || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
	FROM emissions.' || inv_table_name || ' inv
	where not exists (select 1 
		from emf.sources 
		where sources.source = inv.scc || inv.fips || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
		)
/*
		left outer join emf.sources
		on sources.source = inv.scc || inv.fips || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
		sources.scc = inv.scc
		and sources.fips = inv.fips
		' || case when is_point_table = false then '
		and sources.plantid is null
		and sources.pointid is null
		and sources.stackid is null
		and sources.segment is null
		' else '
		and sources.plantid = inv.plantid
		and sources.pointid = inv.pointid
		and sources.stackid = inv.stackid
		and sources.segment = inv.segment
		' end || '

		 where sources.id is null 
*/			' || case when length(coalesce(inv_filter, '')) > 0 then ' and (' || public.alias_inventory_filter(inv_filter, 'inv') || ')' else '' end || '
		';

	return;
END;
$$ LANGUAGE plpgsql;


--select public.populate_sources_table('DS_point_cap2005_epa_1697358408','version IN (0) AND dataset_id=4441');
--select public.populate_sources_table('ds_test_lc_error_at_epa_mergedorl_010217189_20110601010217206','(substring(fips,1,2)=''37'') ');analyze emf.sources;

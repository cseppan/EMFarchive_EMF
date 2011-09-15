/*
select public.build_project_future_year_inventory_matching_hierarchy_sql(
	5315, --inv_dataset_id integer, 
	1, --inv_dataset_version integer, 
	4120, --control_program_dataset_id integer, 
	0, --control_program_dataset_version integer, 
	'fips,plantid,pointid,stackid,segment,scc,poll', --select_columns varchar, 
	'substring(fips,1,2) = ''37''',--null'substring(fips,1,2) = ''37''', --inv_filter text,
	null, --1279 county_dataset_id integer,
	null, --county_dataset_version integer,
	'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''07/01/2020''::timestamp without time zone	', --control_program_dataset_filter text,
	1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
	);

	' || case when control_program.type <> 'Projection' then '
							-- make the compliance date has been met
							and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
							' else '' end || '
*/
CREATE OR REPLACE FUNCTION public.build_project_future_year_inventory_matching_hierarchy_sql(
	inv_dataset_id integer, 
	inv_dataset_version integer, 
	control_program_dataset_id integer, 
	control_program_dataset_version integer, 
	select_columns varchar, 
	inv_filter text,
	county_dataset_id integer,
	county_dataset_version integer,
	control_program_dataset_filter text,
	match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = include only Matched Sources, quick version (just return one of each matching type)
	) RETURNS text AS
$BODY$
DECLARE
	inv_is_point_table boolean := false;
	inv_has_sic_column boolean := false; 
	inv_has_naics_column boolean := false;
	inv_has_mact_column boolean := false;
	control_packet_has_mact_column boolean := false;
	control_packet_has_naics_column boolean := false;
	sql text := '';

	control_program_table_name varchar(64) = ''; 
	inv_table_name varchar(64) = '';
	join_type varchar(10) := 'inner';
	control_program_dataset_version_filter_sql text := '';
	inv_dataset_filter_sql text := '';
	aliased_select_columns text := '';
	
	county_dataset_filter_sql text := '';
	
BEGIN
	join_type := case when coalesce(match_type, 1) = 2 then 'left outer' else 'inner' end;

	-- get the contol program dataset table name
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = control_program_dataset_id
	into control_program_table_name;

	-- get the input dataset table name
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = inv_dataset_id
	into inv_table_name;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;

	--store control dataset version filter in variable
	select public.build_version_where_filter(control_program_dataset_id, control_program_dataset_version, 'cp') || coalesce(' and ' || '(' ||  public.alias_filter(control_program_dataset_filter, control_program_table_name, 'cp') || ')', '')
	into control_program_dataset_version_filter_sql;

	--store control dataset version filter in variable
	select public.build_version_where_filter(inv_dataset_id, inv_dataset_version, 'inv') || coalesce(' and ' || '(' || public.alias_filter(inv_filter, inv_table_name, 'inv') || ')', '') || coalesce(county_dataset_filter_sql, '')
	into inv_dataset_filter_sql;

	select public.alias_filter(select_columns, control_program_table_name, 'cp')
	into select_columns;

	-- see if there are point specific columns in the inventory
	inv_is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a mact column in the inventory
	inv_has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',');

	-- see if there is a mact column in the control packet
	control_packet_has_mact_column := public.check_table_for_columns(control_program_table_name, 'mact', ',');

	-- see if there is a naics column in the control packet
	control_packet_has_naics_column := public.check_table_for_columns(control_program_table_name, 'naics', ',');

	-- see if there is a sic column in the inventory
	inv_has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	inv_has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	sql :=
		'
		WITH inv AS (
		select * 
		from emissions.' || inv_table_name || ' inv
		where ' || inv_dataset_filter_sql || '
		)
		--NA - NEEDED To help with not having to worry about when a union might end up at the top of the sql statement
		select 
			null::integer as record_id,' || coalesce(select_columns || ',','') || '
			null::double precision as ranking
		FROM emissions.' || control_program_table_name || ' cp

		where 1 = 0

		' || case when inv_is_point_table then '
		--1 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) and (cp.segment is not null) and (cp.scc is not null) and (cp.poll is not null) then 1::double precision --1
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			and cp.segment = inv.segment
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is not null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '

			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--2 - Country/State/County code, plant ID, point ID, stack ID, segment, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) and (cp.segment is not null) and (cp.poll is not null) then 2::double precision --2
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			and cp.segment = inv.segment
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is not null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--3 - Country/State/County code, plant ID, point ID, stack ID, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) and (cp.poll is not null) then 3::double precision --3
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--4 - Country/State/County code, plant ID, point ID, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.poll is not null) then 4::double precision --4
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--5 - Country/State/County code, plant ID, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.scc is not null) and (cp.poll is not null) then 5::double precision --5
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--5.5 - Country/State/County code, plant ID, MACT code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.mact is not null) and (cp.poll is not null) then 5.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.mact = inv.mact
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--6 - Country/State/County code, plant ID, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.poll is not null) then 6::double precision --6
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--7 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) and (cp.segment is not null) and (cp.scc is not null) then 7::double precision --7
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			and cp.segment = inv.segment
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is not null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--8- Country/State/County code, plant ID, point ID, stack ID, segment
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) and (cp.segment is not null) then 8::double precision --8
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			and cp.segment = inv.segment
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is not null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--9 - Country/State/County code, plant ID, point ID, stack ID
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) and (cp.stackid is not null) then 9::double precision --9
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			and cp.stackid = inv.stackid
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is not null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--10 - Country/State/County code, plant ID, point id
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.pointid is not null) then 10::double precision --10
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.pointid = inv.pointid
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is not null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--11 - Country/State/County code, plant ID, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.scc is not null) then 11::double precision --11
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--12 - Country/State/County code, plant ID, MACT code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) and (cp.mact is not null) then 12::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			and cp.mact = inv.mact
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || ' ' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--13 - Country/State/County code, plant ID
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.plantid is not null) then 13::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.fips = inv.fips
			
			and cp.plantid = inv.plantid
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is not null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--14,16 - Country/State/County code or Country/State code, MACT code, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.mact is not null) and (cp.scc is not null) and (cp.poll is not null) then 14::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.mact is not null) and (cp.scc is not null) and (cp.poll is not null) then 16::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and cp.mact = inv.mact
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--15,17 - Country/State/County code or Country/State code, MACT code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.mact is not null) and (cp.poll is not null) then 15::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.mact is not null) and (cp.poll is not null) then 17::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and cp.mact = inv.mact
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--18 - MACT code, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.mact is not null) and (cp.scc is not null) and (cp.poll is not null) then 18::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.mact = inv.mact
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--19 - MACT code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.mact is not null) and (cp.poll is not null) then 19::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.mact = inv.mact
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--20,22 - Country/State/County code or Country/State code, 8-digit SCC code, MACT code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.mact is not null) and (cp.scc is not null) then 20::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.mact is not null) and (cp.scc is not null) then 22::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and cp.mact = inv.mact
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--21,23 - Country/State/County code or Country/State code, MACT code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.mact is not null) then 21::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.mact is not null) then 23::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and cp.mact = inv.mact
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--24 - MACT code, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.mact is not null) and (cp.scc is not null) then 24::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.mact = inv.mact
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25 - MACT code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.mact is not null) then 25::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.mact = inv.mact
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			and cp.mact is not null
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end



/*
25.01	Country/State/County code, NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.02	Country/State/County code, NAICS code, pollutant	point, nonpoint	control, cpection
25.03	Country/State code, NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.04	Country/State code, NAICS code, pollutant	point, nonpoint	control, cpection
25.05	NAICS code, 8-digit SCC code, pollutant	point, nonpoint	control, cpection
25.06	NAICS code, pollutant	point, nonpoint	control, cpection
25.07	Country/State/County code, NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.08	Country/State/County code, NAICS code	point, nonpoint	control, cpection
25.09	Country/State code, NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.10	Country/State code, NAICS code	point, nonpoint	control, cpection
25.11	NAICS code, 8-digit SCC code	point, nonpoint	control, cpection
25.12	NAICS code	point, nonpoint	control, cpection
*/



		|| case when inv_has_naics_column and control_packet_has_naics_column then '
		--25.01,25.03 - Country/State/County code or Country/State code, NAICS code, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.01::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.03::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.naics = inv.naics
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.02,25.04 - Country/State/County code or Country/State code, NAICS code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.naics is not null) and (cp.poll is not null) then 25.02::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.naics is not null) and (cp.poll is not null) then 25.04::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.naics = inv.naics
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.05 - NAICS code, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.05::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.06 - NAICS code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.poll is not null) then 25.06::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.07,25.09 - Country/State/County code or Country/State code, NAICS code, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.naics is not null) and (cp.scc is not null) then 25.07::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.naics is not null) and (cp.scc is not null) then 25.09::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.naics = inv.naics
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.08,25.10 - Country/State/County code or Country/State code, NAICS code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.naics is not null) then 25.08::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.naics is not null) then 25.10::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.naics = inv.naics
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.11 - NAICS code, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) and (cp.scc is not null) then 25.11::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--25.12 - NAICS code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.naics is not null) then 25.12::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.naics = inv.naics
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			and cp.naics is not null
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '






		' || case when inv_has_sic_column then '
		--25.5,27.5 - Country/State/County code or Country/State code, 8-digit SCC code, 4-digit SIC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 25.5::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 27.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.sic = inv.sic
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--26,28 - Country/State/County code or Country/State code, 4-digit SIC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.sic is not null) and (cp.poll is not null) then 26::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.sic is not null) and (cp.poll is not null) then 28::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.sic = inv.sic
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--29.5 - 4-digit SIC code, SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.scc is not null) and (cp.poll is not null) then 29.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--30 - 4-digit SIC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.poll is not null) then 30::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--31.5,33.5 - Country/State/County code or Country/State code, 4-digit SIC code, SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.sic is not null) and (cp.scc is not null) then 31.5::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.sic is not null) and (cp.scc is not null) then 33.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.sic = inv.sic
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--32,34 - Country/State/County code or Country/State code, 4-digit SIC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.sic is not null) then 32::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.sic is not null) then 34::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.sic = inv.sic
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--35.5 - 4-digit SIC code, SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) and (cp.scc is not null) then 35.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--36 - 4-digit SIC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.sic is not null) then 36::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.sic = inv.sic
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is not null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 

		' else '' end || '

		--38,42 - Country/State/County code or Country/State code, 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.scc is not null) and (cp.poll is not null) then 38::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.scc is not null) and (cp.poll is not null) then 42::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--46 - 8-digit SCC code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.scc is not null) and (cp.poll is not null) then 46::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.scc = inv.scc
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--50,54 - Country/State/County code or Country/State code, 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.scc is not null) then 50::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.scc is not null) then 54::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--58 - 8-digit SCC code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.scc is not null) then 58::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.scc = inv.scc
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is not null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--62,64 - Country/State/County code or Country/State code, pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) and (cp.poll is not null) then 62::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) and (cp.poll is not null) then 64::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--63,65 - Country/State/County code or Country/State code
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.fips is not null) and (length(cp.fips) = 5 or length(cp.fips) = 6) then 63::double precision
				when (cp.fips is not null) and (length(cp.fips) = 2 or length(cp.fips) = 3) then 65::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on (cp.fips = inv.fips or cp.fips = substr(inv.fips, 1, 2) or (substr(cp.fips,3,3) = ''000'' and substr(cp.fips, 1, 2) = substr(inv.fips, 1, 2)))
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is not null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' 


		--66 - Pollutant
		union all
		' || (case when match_type = 3 then '(' else '' end) || 'select 
			inv.record_id,' || coalesce(select_columns || ',','') || '
			case 
				when (cp.poll is not null) then 66::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' cp
			' || join_type || ' join inv
					
			on cp.poll = inv.poll
			' || case when join_type <> 'left outer' then '' else ' and inv.record_id is null' end || '

		where cp.fips is null 
			and cp.plantid is null 
			and cp.pointid is null 
			and cp.stackid is null 
			and cp.segment is null 
			and cp.scc is null 
			and cp.poll is not null
			and cp.sic is null
			' || case when control_packet_has_naics_column then 'and cp.naics is null' else '' end || '
			' || case when control_packet_has_mact_column then 'and cp.mact is null' else '' end || '
			and ' || control_program_dataset_version_filter_sql || (case when match_type = 3 then ' limit 1)' else '' end) || ' ';
	return sql;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;

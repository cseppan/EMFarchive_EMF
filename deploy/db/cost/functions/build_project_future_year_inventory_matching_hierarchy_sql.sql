CREATE OR REPLACE FUNCTION public.build_project_future_year_inventory_matching_hierarchy_sql(
	control_program_table_name varchar(64), 
	inv_table_name varchar(64), 
	select_columns varchar, 
	where_filter varchar
	) RETURNS text AS
$BODY$
DECLARE
	inv_is_point_table boolean := false;
	inv_has_sic_column boolean := false; 
	inv_has_naics_column boolean := false;
	inv_has_mact_column boolean := false;
	control_packet_has_mact_column boolean := false;
	sql character varying := '';
BEGIN

	-- see if there are point specific columns in the inventory
	inv_is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a mact column in the inventory
	inv_has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',');

	-- see if there is a mact column in the control packet
	control_packet_has_mact_column := public.check_table_for_columns(control_program_table_name, 'mact', ',');

	-- see if there is a sic column in the inventory
	inv_has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	inv_has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	sql :=
		'' || case when inv_is_point_table then '
		--1 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) and (proj.poll is not null) then 1::double precision --1
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid
			and proj.segment = inv.segment
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is not null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '

			and ' || where_filter || '


		--2 - Country/State/County code, plant ID, point ID, stack ID, segment, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.poll is not null) then 2::double precision --2
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid
			and proj.segment = inv.segment
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is not null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			
			and ' || where_filter || '


		--3 - Country/State/County code, plant ID, point ID, stack ID, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.poll is not null) then 3::double precision --3
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--4 - Country/State/County code, plant ID, point ID, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.poll is not null) then 4::double precision --4
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--5 - Country/State/County code, plant ID, 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) and (proj.poll is not null) then 5::double precision --5
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--5.5 - Country/State/County code, plant ID, MACT code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.mact is not null) and (proj.poll is not null) then 5.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.mact = inv.mact
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '

		' else '' end || '

		--6 - Country/State/County code, plant ID, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.poll is not null) then 6::double precision --6
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--7 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) then 7::double precision --7
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid
			and proj.segment = inv.segment
			and proj.scc = inv.scc

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is not null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--8- Country/State/County code, plant ID, point ID, stack ID, segment
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) then 8::double precision --8
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid
			and proj.segment = inv.segment

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is not null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--9 - Country/State/County code, plant ID, point ID, stack ID
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) then 9::double precision --9
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid
			and proj.stackid = inv.stackid

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is not null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--10 - Country/State/County code, plant ID, point id
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) then 10::double precision --10
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.pointid = inv.pointid

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is not null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--11 - Country/State/County code, plant ID, 8-digit SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) then 11::double precision --11
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.scc = inv.scc

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--12 - Country/State/County code, plant ID, MACT code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.mact is not null) then 12::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid
			and proj.mact = inv.mact

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '

		' else '' end || '

		--13 - Country/State/County code, plant ID
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) then 13::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.fips = inv.fips
			
			and proj.plantid = inv.plantid

		where proj.fips is not null 
			and proj.plantid is not null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '

		' else '' end || '

		' || case when inv_has_mact_column and control_packet_has_mact_column then '
		--14,16 - Country/State/County code or Country/State code, MACT code, 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 14::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 16::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and proj.mact = inv.mact
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--15,17 - Country/State/County code or Country/State code, MACT code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.poll is not null) then 15::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.poll is not null) then 17::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and proj.mact = inv.mact
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--18 - MACT code, 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 18::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.mact = inv.mact
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--19 - MACT code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.mact is not null) and (proj.poll is not null) then 19::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.mact = inv.mact
			and proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--20,22 - Country/State/County code or Country/State code, 8-digit SCC code, MACT code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.scc is not null) then 20::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.scc is not null) then 22::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and proj.mact = inv.mact
			and proj.scc = inv.scc

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--21,23 - Country/State/County code or Country/State code, MACT code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) then 21::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) then 23::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			
			and proj.mact = inv.mact

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--24 - MACT code, 8-digit SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.mact is not null) and (proj.scc is not null) then 24::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.mact = inv.mact
			and proj.scc = inv.scc

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '


		--25 - MACT code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.mact is not null) then 25::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.mact = inv.mact

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			and proj.mact is not null
			and ' || where_filter || '

		' else '' end || '

		' || case when inv_has_sic_column then '
		--25.5,27.5 - Country/State/County code or Country/State code, 8-digit SCC code, 4-digit SIC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) and (proj.scc is not null) and (proj.poll is not null) then 25.5::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) and (proj.scc is not null) and (proj.poll is not null) then 27.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.sic = inv.sic
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null
			and proj.poll is not null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--26,28 - Country/State/County code or Country/State code, 4-digit SIC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) and (proj.poll is not null) then 26::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) and (proj.poll is not null) then 28::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.sic = inv.sic
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--29.5 - 4-digit SIC code, SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.sic is not null) and (proj.scc is not null) and (proj.poll is not null) then 29.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.sic = inv.sic
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--30 - 4-digit SIC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.sic is not null) and (proj.poll is not null) then 30::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.sic = inv.sic
			and proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--31.5,33.5 - Country/State/County code or Country/State code, 4-digit SIC code, SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) and (proj.scc is not null) then 31.5::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) and (proj.scc is not null) then 33.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.sic = inv.sic
			and proj.scc = inv.scc

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--32,34 - Country/State/County code or Country/State code, 4-digit SIC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) then 32::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) then 34::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.sic = inv.sic

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--35.5 - 4-digit SIC code, SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.sic is not null) and (proj.scc is not null) then 35.5::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.sic = inv.sic
			and proj.scc = inv.scc

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--36 - 4-digit SIC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.sic is not null) then 36::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.sic = inv.sic

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is not null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '

		' else '' end || '

		--38,42 - Country/State/County code or Country/State code, 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) and (proj.poll is not null) then 38::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) and (proj.poll is not null) then 42::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--46 - 8-digit SCC code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.scc is not null) and (proj.poll is not null) then 46::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.scc = inv.scc
			and proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--50,54 - Country/State/County code or Country/State code, 8-digit SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) then 50::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) then 54::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.scc = inv.scc

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--58 - 8-digit SCC code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.scc is not null) then 58::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.scc = inv.scc

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is not null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--62,64 - Country/State/County code or Country/State code, pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.poll is not null) then 62::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.poll is not null) then 64::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))
			and proj.poll = inv.poll

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--63,65 - Country/State/County code or Country/State code
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) then 63::double precision
				when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) then 65::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2) or (substr(proj.fips,3,3) = ''000'' and substr(proj.fips, 1, 2) = substr(inv.fips, 1, 2)))

		where proj.fips is not null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '


		--66 - Pollutant
		union all
		select 
			inv.record_id,' || select_columns || '
			case 
				when (proj.poll is not null) then 66::double precision
			end as ranking
		FROM emissions.' || control_program_table_name || ' proj
			inner join emissions.' || inv_table_name || ' inv
					
			on proj.poll = inv.poll

		where proj.fips is null 
			and proj.plantid is null 
			and proj.pointid is null 
			and proj.stackid is null 
			and proj.segment is null 
			and proj.scc is null 
			and proj.poll is not null
			and proj.sic is null
			' || case when control_packet_has_mact_column then 'and proj.mact is null' else '' end || '
			and ' || where_filter || '';
	return sql;
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;

#!/bin/sh

## Upgrades EMF schemas 
##
## Should be run from the same directory as this file 
##
##------------------------------------------------------------##

## set environtment variables for script
export POSTGRESBINDIR=/usr/bin
export EMFDATABASE=EMF

$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < upgrade_versions.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/upgrade_cost_schema.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/delete_measure.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/create_projected_future_year_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_project_future_year_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_max_emis_red_strategy_messages.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_apply_measures_in_series_strategy.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_sources_table.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/validate_project_future_year_inventory_control_programs.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_project_future_year_inventory_strategy_messages.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_annotate_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/build_project_future_year_inventory_matching_hierarchy_sql.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/cost_equations.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_max_emis_red_strategy.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_worksheet.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_detailed_result.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < data/upgrade_data_schema.sql

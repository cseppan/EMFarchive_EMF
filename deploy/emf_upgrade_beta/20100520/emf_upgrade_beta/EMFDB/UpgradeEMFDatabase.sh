#!/bin/sh

## Upgrades EMF schemas 
##
## Should be run from the same directory as this file 
##
##------------------------------------------------------------##

## set environtment variables for script
export POSTGRESBINDIR=/usr/bin
export POSTGRESSCRIPTDIR=/usr/bin/scripts
export EMFDATABASE=EMF

$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < upgrade_versions.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < $POSTGRESSCRIPTDIR/ltree.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < case/upgrade_case_schema.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/upgrade_cost_schema.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/alias_inventory_filter.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/build_project_future_year_inventory_matching_hierarchy_sql.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/clean_project_future_year_inventory_control_programs.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/create_projected_future_year_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_detailed_result.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_worksheet.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_max_emis_red_strategy.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_project_future_year_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < data/upgrade_data_schema.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < sms/upgrade_sms_schema.sql

package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class AbstractControlStrategyInventoryOutput implements ControlStrategyInventoryOutput {

    protected ControlStrategy controlStrategy;

    protected DatasetCreator creator;

    protected TableFormat tableFormat;

    protected StatusDAO statusServices;

    protected User user;

    protected HibernateSessionFactory sessionFactory;

//    private DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    protected EmfDataset inputDataset;
    
    protected ControlStrategyResult controlStrategyResult;

    protected Datasource datasource;
    
    public AbstractControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.controlStrategyResult = controlStrategyResult;
        this.inputDataset = controlStrategyResult.getInputDataset();
        this.user = user;
        this.sessionFactory = sessionFactory;
//        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.tableFormat = new FileFormatFactory(dbServer).tableFormat(inputDataset.getDatasetType());
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                dbServer.getEmissionsDatasource(), new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords()));
        this.statusServices = new StatusDAO(sessionFactory);
    }

    public void create() throws Exception {
        doCreateInventory(inputDataset, getDatasetTableName(inputDataset));
    }

    protected void doCreateInventory(EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {
            EmfDataset dataset = creator.addDataset(creator.createDatasetName(inputDataset + "_CntlInv"), 
                    inputDataset, inputDataset.getDatasetType(), 
                    tableFormat, description(inputDataset));
            
            String outputInventoryTableName = getDatasetTableName(dataset);
            
            ControlStrategyResult result = getControlStrategyResult(controlStrategyResult.getId());
            try {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource);
            } catch (Exception e) {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource, true);
            }        

            setControlStrategyResultContolledInventory(result, dataset);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }

//    private void setandRunQASteps() throws EmfException {
//        try {
//            ControlStrategyResult result = getControlStrategyResult();
//            EmfDataset controlledDataset = (EmfDataset) result.getControlledInventoryDataset();
//            QAStepTask qaTask = new QAStepTask(controlledDataset, controlledDataset.getDefaultVersion(), user,
//                    sessionFactory, dbServerFactory);
//            qaTask.runSummaryQASteps(qaTask.getDefaultSummaryQANames());
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        }
//    }

    protected String description(EmfDataset inputDataset) {
        String startingDesc = inputDataset.getDescription() + "";
        if ((startingDesc.indexOf("FIPS,SCC") > 0) || (startingDesc.indexOf("\"FIPS\",") > 0))
        {
           return startingDesc;
        }
        return inputDataset.getDescription() + "#" + "Implements control strategy: " + controlStrategy.getName() + "\n" +
          "#DESC FIPS,SCC,SIC,MACT,SRCTYPE,POLL,ANN_EMIS,AVD_EMIS,CEFF,REFF,RPEN,PRI_DEV,SEC_DEV,DATA_SOURCE,YEAR,TRIBAL_CODE,"+
          "MACT_FLAG,COMPLIANCE_STATUS,START_DATE,END_DATE,WINTER_PCT,SPRING_PCT,SUMMER_PCT,FALL_PCT,DAYS_PER_WEEK,WEEKS_PER_YEAR,HOURS_PER_DAY,"+
          "HOURS_PER_YEAR,PERIOD_DAYS_PER_WEEK,PERIOD_WEEKS_PER_YEAR,PERIOD_HOURS_OF_DAY,PERIOD_HOURS_PER_PERIOD\n";
          // TODO: need to make it so that exporters automatically output the column descs instead of putting it here
    }

    protected ControlStrategyResult getControlStrategyResult(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            ControlStrategyResult result = dao.getControlStrategyResult(id, session);
            if (result == null)
                throw new EmfException("You have to run the control strategy to create control inventory output");
            return result;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    protected void addControlStrategyResult(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            int id = dao.add(result, session);
            result.setId(id);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveControlStrategyResult(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(result, session);
        } catch (Exception e) {
            throw new EmfException("Could not update control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void setControlStrategyResultContolledInventory(ControlStrategyResult result, EmfDataset controlledInventory) throws EmfException {
        result.setControlledInventoryDataset(controlledInventory);
        saveControlStrategyResult(result);
    }

    protected Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    protected String getDatasetTableName(Dataset dataset) throws EmfException {
        if (dataset == null) return "";
        InternalSource[] sources = dataset.getInternalSources();
        if (sources.length > 1) {
            throw new EmfException(
                    "At this moment datasets with multiple tables are not supported for creating a inventory output");
        }
        String tableName = sources[0].getTable();
        return tableName;
    }

    protected int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInMonth(controlStrategy.getInventoryYear(), month) : 31;
    }
    
    protected String detailDatasetTable(ControlStrategyResult result) throws EmfException {
        return getDatasetTableName(result.getDetailedResultDataset());
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource) throws EmfException {
        createControlledInventory(datasetId, inputTable, 
                detailResultTable, outputTable, 
                version, dataset, 
                datasource, false);
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) throws EmfException {
        String query = populateInventory(datasetId, inputTable, 
                detailResultTable, outputTable,
                version(inputDataset, controlStrategyResult.getInputDatasetVersion()), inputDataset, 
                datasource, missingColumns);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }

    private String populateInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) {
        VersionedQuery versionedQuery = new VersionedQuery(version);
        int month = inputDataset.applicableMonth();
        int noOfDaysInMonth = 31;
        if (month != -1) {
            noOfDaysInMonth = getDaysInMonth(month);
        }
        String sql = "select ";
        String columnList = "";
        Column[] columns = tableFormat.cols();
        //flag indicating if we are doing replacement vs
        //add on controls., currently we only support replacement controls.
        boolean isReplacementControl = !controlStrategy.getStrategyType().getName().equals(StrategyType.applyMeasuresInSeries);
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
//                sql += "record_id";
//                columnList += "record_id";
            } else if (columnName.equalsIgnoreCase("dataset_id")) {
//              sql += "," + datasetId + " as dataset_id";
//              columnList += ",dataset_id";
                sql += datasetId + " as dataset_id";
                columnList += "dataset_id";
            } else if (columnName.equalsIgnoreCase("delete_versions")) {
                sql += ", '' as delete_versions";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("version")) {
                sql += ", 0 as version";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ceff")) {
                sql += ", case when b.source_id is not null then case when coalesce(b.starting_emissions, 0.0) <> 0.0 then TO_CHAR((1- b.final_emissions / b.starting_emissions) * 100, 'FM990.099')::double precision else null::double precision end else ceff end as ceff";
//              sql += ", case when b.source_id is not null then case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then TO_CHAR((1 - " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + ") * 100, 'FM990.099')::double precision else 0.0 end else ceff end as ceff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("avd_emis")) {
                sql += ", case when b.source_id is not null then b.final_emissions / " + (month != -1 ? noOfDaysInMonth : "365") + " else avd_emis end as avd_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ann_emis")) {
                sql += ", case when b.source_id is not null then b.final_emissions else ann_emis end as ann_emis";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("reff")) {
                sql += ", case when b.source_id is not null then 100 else reff end as reff";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("rpen")) {
                sql += ", case when b.source_id is not null then 100 else rpen end as rpen";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CONTROL_MEASURES")) {
                if (isReplacementControl)
                    sql += ", cm_abbrev_list as CONTROL_MEASURES";
                else
                    sql += ", case when " + (!missingColumns ? "control_measures" : "null") + " is null or length(" + (!missingColumns ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (!missingColumns ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                if (isReplacementControl)
                    sql += ", percent_reduction_list as PCT_REDUCTION";
                else
                    sql += ", case when " + (!missingColumns ? "pct_reduction" : "null") + " is null or length(" + (!missingColumns ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (!missingColumns ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CURRENT_COST")) {
                sql += ", annual_cost as CURRENT_COST";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CUMULATIVE_COST")) {
                if (isReplacementControl)
                    sql += ", case when b.source_id is not null then " + (!missingColumns ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!missingColumns ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
                else
                    sql += ", case when b.source_id is not null then " + (!missingColumns ? "case when cumulative_cost is null then annual_cost when cumulative_cost is not null then cumulative_cost + coalesce(annual_cost, 0.0) end" : "annual_cost") + " else " + (!missingColumns ? "cumulative_cost" : "null::double precision") + " end as CUMULATIVE_COST";
//                sql += ", case when " + (!missingColumns ? "cumulative_cost" : "null") + " is null and annual_cost is null then null::double precision else coalesce(" + (!missingColumns ? "cumulative_cost" : "null::double precision") + ", 0.0) + coalesce(annual_cost, 0) end as CUMULATIVE_COST";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(inputTable, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT source_id, "
        + "min(final_emissions) as final_emissions, "
        + "max(input_emis) as starting_emissions, "
        + "sum(annual_cost) as annual_cost, "
        + "public.concatenate_with_ampersand(cm_abbrev) as cm_abbrev_list, "
        + "public.concatenate_with_ampersand(TO_CHAR(percent_reduction, 'FM990.099')) as percent_reduction_list "
        + "FROM (select source_id, input_emis, final_emissions, annual_cost, cm_abbrev, percent_reduction "
        + "        FROM " + qualifiedTable(detailResultTable, datasource)
        + "        order by source_id, apply_order "
        + "        ) tbl "
        + "    group by source_id ) as b "
        + "on inv.record_id = b.source_id"
        + " WHERE " + versionedQuery.query();
        sql = "INSERT INTO " + qualifiedTable(outputTable, datasource) + " (" + columnList + ") " + sql;
        System.out.println(sql);
        return sql;
    }

    protected String qualifiedTable(String table, Datasource datasource) {
        return datasource.getName() + "." + table;
    }

    protected void failStatus(StatusDAO statusServices, String message) {
        String end = "Failed to create a controlled inventory for strategy "+controlStrategy.getName()+
           ": " + message;
        Status status = status(user, end);
        statusServices.add(status);
    }

    protected void startStatus(StatusDAO statusServices) {
        String start = "Creating controlled inventory of type '" + inputDataset.getDatasetType()
                + "' using control strategy '" + controlStrategy.getName() + "' for dataset '" + inputDataset.getName() + "'";
        Status status = status(user, start);
        statusServices.add(status);
    }

    protected Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Controlled Inventory");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    protected void createDetailedResultTableIndexes(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.create_strategy_detailed_result_table_indexes('" + getDatasetTableName(controlStrategyResult.getDetailedResultDataset()) + "');analyze emissions." + getDatasetTableName(controlStrategyResult.getDetailedResultDataset()) + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //
        } finally {
            //
        }
    }
}

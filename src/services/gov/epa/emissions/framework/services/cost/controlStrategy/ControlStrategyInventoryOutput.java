package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
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
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class ControlStrategyInventoryOutput {

    private ControlStrategy controlStrategy;

    private DatasetCreator creator;

    private TableFormat tableFormat;

    private StatusDAO statusServices;

    private User user;

    private HibernateSessionFactory sessionFactory;

//    private DbServerFactory dbServerFactory;

    private DbServer dbServer;

    private EmfDataset inputDataset;
    
    private ControlStrategyResult controlStrategyResult;
    
    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.controlStrategyResult = controlStrategyResult;
        this.inputDataset = controlStrategyResult.getInputDataset();
        this.user = user;
        this.sessionFactory = sessionFactory;
//        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.tableFormat = new FileFormatFactory(dbServer).tableFormat(inputDataset.getDatasetType());
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                dbServer.getEmissionsDatasource(), new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords()));
        this.statusServices = new StatusDAO(sessionFactory);
    }

    public void create() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableCreator tableCreator = new TableCreator(datasource);
        doCreateInventory(dbServer, datasource, tableCreator, inputDataset, inputTable(inputDataset));
    }

    private void doCreateInventory(DbServer server, Datasource datasource, TableCreator tableCreator,
            EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
//        createTable(outputInventoryTableName, tableCreator);
        startStatus(statusServices);
        try {
            copyAndUpdateData(server, datasource, inputDataset, inputTable);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
//            tableCreator.drop(outputInventoryTableName);
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }

//    private void copyAndUpdateData(EmfDbServer server, Datasource datasource, EmfDataset inputDataset,
    private void copyAndUpdateData(DbServer server, Datasource datasource, EmfDataset inputDataset,
            String inputTable) throws EmfException {
        EmfDataset dataset = creator.addDataset("ControlledInventory_", "CSINVEN_", 
                inputDataset, inputDataset.getDatasetType(), 
                tableFormat, description(inputDataset));
        
        String outputInventoryTableName = dataset.getInternalSources()[0].getTable();
        
        ControlStrategyResult result = getControlStrategyResult();
        try {
            createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                    outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                    inputDataset, datasource);
        } catch (Exception e) {
            createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                    outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                    inputDataset, datasource, true);
        }        

        updateControlStrategyResults(result, dataset);
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

    private String description(EmfDataset inputDataset) {
        String startingDesc = inputDataset.getDescription();
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

    private ControlStrategyResult getControlStrategyResult() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            ControlStrategyResult result = dao.getControlStrategyResult(controlStrategyResult.getId(), session);
            if (result == null)
                throw new EmfException("You have to run the control strategy to create control inventory output");
            return result;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    private void updateControlStrategyResults(ControlStrategyResult result, EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            result.setControlledInventoryDataset(dataset);
            dao.updateControlStrategyResult(result, session);
        } catch (Exception e) {
            throw new EmfException("Could not update control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }

    }

    private Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    private String inputTable(EmfDataset inputDataset) throws EmfException {
        InternalSource[] sources = inputDataset.getInternalSources();
        if (sources.length > 1) {
            throw new EmfException(
                    "At this moment datasets with multiple tables are not supported for creating a inventory output");
        }
        String inputTable = sources[0].getTable();
        return inputTable;
    }

    private int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInMonth(controlStrategy.getInventoryYear(), month) : 31;
    }
    
    private String detailDatasetTable(ControlStrategyResult result) {
        Dataset detailedResultDataset = result.getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
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
        String query = copyDataFromOriginalTableQuery2(datasetId, inputTable, 
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

    private String copyDataFromOriginalTableQuery2(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
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
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
                sql += "record_id";
                columnList += "record_id";
            } else if (columnName.equalsIgnoreCase("dataset_id")) {
                sql += "," + datasetId + " as dataset_id";
                columnList += ",dataset_id";
            } else if (columnName.equalsIgnoreCase("delete_versions")) {
                sql += ", '' as delete_versions";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("version")) {
                sql += ", 0 as version";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("ceff")) {
                sql += ", case when b.source_id is not null then case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then TO_CHAR((1 - " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + ") * 100, 'FM990.099')::double precision else 0.0 end else ceff end as ceff";
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
                sql += ", case when " + (!missingColumns ? "control_measures" : "null") + " is null or length(" + (!missingColumns ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (!missingColumns ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                sql += ", case when " + (!missingColumns ? "pct_reduction" : "null") + " is null or length(" + (!missingColumns ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (!missingColumns ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CURRENT_COST")) {
                sql += ", annual_cost as CURRENT_COST";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CUMULATIVE_COST")) {
                sql += ", case when " + (!missingColumns ? "cumulative_cost" : "null") + " is null and annual_cost is null then null else coalesce(" + (!missingColumns ? "cumulative_cost" : "null") + ", 0) + coalesce(annual_cost, 0) end as CUMULATIVE_COST";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(inputTable, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT source_id, "
        + "max(final_emissions) as final_emissions, "
        + "sum(annual_cost) as annual_cost, "
        + "public.concatenate_with_ampersand(cm_abbrev) as cm_abbrev_list, "
        + "public.concatenate_with_ampersand(TO_CHAR(percent_reduction, 'FM990.099')) as percent_reduction_list "
        + "FROM (select source_id, final_emissions, annual_cost, cm_abbrev, percent_reduction "
        + "        FROM " + qualifiedTable(detailResultTable, datasource)
        + "        order by source_id, apply_order "
        + "        ) tbl "
        + "    group by source_id ) as b "
        + "on inv.record_id = b.source_id"
        + " WHERE " + versionedQuery.query();
        sql = "SET work_mem TO '256MB';INSERT INTO " + qualifiedTable(outputTable, datasource) + " (" + columnList + ") " + sql;
        System.out.println(sql);
        return sql;
    }

    private String qualifiedTable(String table, Datasource datasource) {
        return datasource.getName() + "." + table;
    }

    private void failStatus(StatusDAO statusServices, String message) {
        String end = "Failed to create a controlled inventory for strategy "+controlStrategy.getName()+
           ": " + message;
        Status status = status(user, end);
        statusServices.add(status);
    }

    private void startStatus(StatusDAO statusServices) {
        String start = "Creating controlled inventory of type '" + inputDataset.getDatasetType()
                + "' using control strategy '" + controlStrategy.getName() + "' for dataset '" + inputDataset.getName() + "'";
        Status status = status(user, start);
        statusServices.add(status);
    }

    private Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Controlled Inventory");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }
}

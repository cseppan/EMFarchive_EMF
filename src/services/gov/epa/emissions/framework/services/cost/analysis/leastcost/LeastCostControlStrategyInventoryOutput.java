package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.AbstractControlStrategyInventoryOutput;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class LeastCostControlStrategyInventoryOutput extends AbstractControlStrategyInventoryOutput {
   
    public LeastCostControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        super(user, controlStrategy,
                controlStrategyResult, sessionFactory, 
                dbServerFactory);
    }

    public void create() throws Exception {
        createInventories();
    }

    protected void createInventories() throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {

            ControlStrategyInputDataset[] inventories = controlStrategy.getControlStrategyInputDatasets();
            ControlStrategyResult detailedResult = getControlStrategyResult(controlStrategyResult.getId());
            //we need to create a controlled inventory for each invnentory, except the merged inventory
            for (int i = 0; i < inventories.length; i++) {
//                EmfDataset inventory = inventories[i].getInputDataset();
                this.inputDataset = inventories[i].getInputDataset();
                if (!inputDataset.getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    tableFormat = new FileFormatFactory(dbServer).tableFormat(inputDataset.getDatasetType());
                    //create controlled inventory dataset
                    EmfDataset dataset = creator.addDataset("ControlledInventory_", "CSINVEN_", 
                            inputDataset, inputDataset.getDatasetType(), 
                            tableFormat, description(inputDataset));
                    //get table name
                    String outputInventoryTableName = getDatasetTableName(dataset);

                    //create strategy result for each controlled inventory
                    ControlStrategyResult result = createControlStrategyResult(inventories[i], dataset, 
                            getControlledInventoryStrategyResultType());

                    createControlledInventory(dataset.getId(), getDatasetTableName(inputDataset), 
                            detailDatasetTable(detailedResult), outputInventoryTableName, 
                            version(inputDataset, inventories[i].getVersion()), datasource, 
                            tableFormat);

                    result.setCompletionTime(new Date());
                    result.setRunStatus("Completed.");
                    saveControlStrategyResult(result);
                }
            }
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }

    private void createControlledInventory(int datasetId, String inputTable, 
            String detailResultTable, String outputTable, 
            Version version, Datasource datasource,
            TableFormat tableFormat) throws EmfException {
        String query = copyDataFromOriginalTableQuery2(datasetId, inputTable, 
                detailResultTable, outputTable,
                version(inputDataset, controlStrategyResult.getInputDatasetVersion()), inputDataset, 
                datasource, tableFormat);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }

    private String copyDataFromOriginalTableQuery2(int contInvDatasetId, String invTableName, 
            String detailResultTableName, String contInvTableName, 
            Version invVersion, Dataset invDataset, 
            Datasource datasource, TableFormat invTableFormat) throws EmfException {
        VersionedQuery invVersionedQuery = new VersionedQuery(invVersion);
        int month = inputDataset.applicableMonth();
        int noOfDaysInMonth = 31;
        if (month != -1) {
            noOfDaysInMonth = getDaysInMonth(month);
        }
        String sql = "select ";
        String columnList = "";
        Column[] columns = invTableFormat.cols();
        ResultSetMetaData md = getResultSetMetaData(qualifiedTable(invTableName, datasource));
        boolean hasControlMeasuresColumn = false;
        boolean hasPctReductionColumn = false;
        boolean hasCumulativeCostColumn = false;
        try {
            for (int i = 1; i < md.getColumnCount(); i++) {
                if (md.getColumnName(i).equalsIgnoreCase("CONTROL_MEASURES")) 
                    hasControlMeasuresColumn = true;
                else if (md.getColumnName(i).equalsIgnoreCase("PCT_REDUCTION")) 
                    hasPctReductionColumn = true;
                else if (md.getColumnName(i).equalsIgnoreCase("CUMULATIVE_COST")) 
                    hasCumulativeCostColumn = true;
            }
        } catch (SQLException e) {
            //
        }
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
                sql += "record_id";
                columnList += "record_id";
            } else if (columnName.equalsIgnoreCase("dataset_id")) {
                sql += "," + contInvDatasetId + " as dataset_id";
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
                sql += ", case when " + (hasControlMeasuresColumn ? "control_measures" : "null") + " is null or length(" + (hasControlMeasuresColumn ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (hasControlMeasuresColumn ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                sql += ", case when " + (hasPctReductionColumn ? "pct_reduction" : "null") + " is null or length(" + (hasPctReductionColumn ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (hasPctReductionColumn ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CURRENT_COST")) {
                sql += ", annual_cost as CURRENT_COST";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CUMULATIVE_COST")) {
                sql += ", case when " + (hasCumulativeCostColumn ? "cumulative_cost" : "null") + " is null and annual_cost is null then null else coalesce(" + (hasCumulativeCostColumn ? "cumulative_cost" : "null") + ", 0) + coalesce(annual_cost, 0) end as CUMULATIVE_COST";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(invTableName, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT source_id, "
        + "max(final_emissions) as final_emissions, "
        + "sum(annual_cost) as annual_cost, "
        + "public.concatenate_with_ampersand(cm_abbrev) as cm_abbrev_list, "
        + "public.concatenate_with_ampersand(TO_CHAR(percent_reduction, 'FM990.099')) as percent_reduction_list "
        + "FROM (select source_id, final_emissions, annual_cost, cm_abbrev, percent_reduction "
        + "        FROM " + qualifiedTable(detailResultTableName, datasource)
        + "        WHERE ORIGINAL_DATASET_ID = " + invDataset.getId()
        + "        order by source_id, apply_order "
        + "        ) tbl "
        + "    group by source_id ) as b "
        + "on inv.record_id = b.source_id"
        + " WHERE " + invVersionedQuery.query();
        sql = "SET work_mem TO '256MB';INSERT INTO " + qualifiedTable(contInvTableName, datasource) + " (" + columnList + ") " + sql;
        System.out.println(sql);
        return sql;
    }

    protected ControlStrategyResult createControlStrategyResult(ControlStrategyInputDataset inventory, EmfDataset controlledInventory,
            StrategyResultType strategyResultType) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        
        result.setInputDataset(inventory.getInputDataset());
        result.setInputDatasetVersion(inventory.getVersion());
        result.setControlledInventoryDataset(controlledInventory);
        result.setStrategyResultType(strategyResultType);
        result.setStartTime(new Date());
        result.setRunStatus("Start processing controlled inventory");

        //persist result
        addControlStrategyResult(result);
        return result;
    }

    protected StrategyResultType getControlledInventoryStrategyResultType() throws EmfException {
        StrategyResultType strategyResultType = null;
        Session session = sessionFactory.getSession();
        try {
            strategyResultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.controlledInventoryResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get strategy result type");
        } finally {
            session.close();
        }
        return strategyResultType;
    }
    
    private ResultSetMetaData getResultSetMetaData(String qualifiedTableName) throws EmfException {
        ResultSet rs;
        ResultSetMetaData md;
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTableName);
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
        return md;
    }
}
package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.orl.ORLMergedFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class StrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
    private ControlStrategyResult leastCostCMWorksheetResult;

    private ControlStrategyResult leastCostCurveSummaryResult;

    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory, Boolean useSQLApproach) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
        //get rid of strategy results
        deleteStrategyResults();

        //run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {
            //process/load each input dataset
            ControlStrategyInputDataset controlStrategyInputDataset = getInventory();
            try {
                loader.loadStrategyResult(controlStrategyInputDataset);
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". " + e.getMessage();
                setStatus(status);
            } finally {
                addStatus(controlStrategyInputDataset);
            }
            
        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                loader.disconnectDbServer();
                disconnectDbServer();
            }
        }
    }

    public void afterRun() throws EmfException {
        //finalize the result, update completion time and run status...
        leastCostCMWorksheetResult.setCompletionTime(new Date());
        leastCostCMWorksheetResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCMWorksheetResult);
        saveControlStrategySummaryResult(leastCostCMWorksheetResult);
        runSummaryQASteps((EmfDataset)leastCostCMWorksheetResult.getDetailedResultDataset(), 0);

        leastCostCurveSummaryResult.setCompletionTime(new Date());
        leastCostCurveSummaryResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCurveSummaryResult);
        saveControlStrategySummaryResult(leastCostCurveSummaryResult);
        runSummaryQASteps((EmfDataset)leastCostCurveSummaryResult.getDetailedResultDataset(), 0);
}

    public void beforeRun() throws EmfException {
        //create the worksheet (strat result)
        leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
        leastCostCurveSummaryResult = loader.loadLeastCostCurveSummaryResult();
        
        //if there is more than one input inventory, then merge these into one dataset, 
        //then we use that as the input to the strategy run
        if (controlStrategyInputDatasetCount >= 1) {
            //check to see if exists already, if so, then truncate its data and start over...
            ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
            boolean hasMergedDataset = false;
            EmfDataset mergedDataset = null;
            for (ControlStrategyInputDataset inputDataset : inputDatasets) 
                if (inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    hasMergedDataset = true;
                    mergedDataset = inputDataset.getInputDataset();
                }
            EmfDataset inputDataset = inputDatasets[0].getInputDataset();
            String country = inputDataset.getCountry() != null ? inputDataset.getCountry().getName() : "US";
            if (!hasMergedDataset) {
                mergedDataset = createMergedInventoryDataset(country);
                //add to strategy...
                ControlStrategyInputDataset controlStrategyInputDataset = new ControlStrategyInputDataset(mergedDataset);
                loader.makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset);
                controlStrategy.addControlStrategyInputDatasets(controlStrategyInputDataset);
                saveControlStrategy(controlStrategy);
            } else {
                truncateORLMergedDataset(mergedDataset);
            }
            populateORLMergedDataset(mergedDataset);
            
        }
    }

    private ControlStrategyInputDataset getInventory() {
        ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
        ControlStrategyInputDataset dataset = null;
        if (inputDatasets.length == 1) {
            dataset = inputDatasets[0];
        } else {
            for (ControlStrategyInputDataset inputDataset : controlStrategy.getControlStrategyInputDatasets()) 
                if (inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    dataset = inputDataset;
                }
        }
        return dataset;
    }
    
    private DatasetType getORLMergedInventoryDatasetType() {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(DatasetType.orlMergedInventory, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    protected EmfDataset createMergedInventoryDataset(String country) throws EmfException {
//        TableFormat tableFormat = new ORLMergedFileFormat(dbServer.getSqlDataTypes());
        TableFormat tableFormat = new VersionedTableFormat(new ORLMergedFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes());
        //"MergedORL_", 
        return creator.addDataset("DS", 
                DatasetCreator.createDatasetName(controlStrategy.getName() + "_MergedORL"), getORLMergedInventoryDatasetType(), 
                tableFormat, getORLMergedInventoryDatasetDescription(tableFormat, country));
    }

    private String getORLMergedInventoryDatasetDescription(TableFormat tableFormat, String country) {
        return "#" + tableFormat.identify() + "\n#COUNTRY " + country + "\n#YEAR " + controlStrategy.getInventoryYear();
    }
    
    private void populateORLMergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            ORLMergedFileFormat fileFormat = new ORLMergedFileFormat(dbServer.getSqlDataTypes());
            
            String columnDelimitedList = fileFormat.columnDelimitedList();
            
            String sql = "INSERT INTO " + qualifiedEmissionTableName(mergedDataset) + " (dataset_id, " + columnDelimitedList + ") ";
            ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                EmfDataset inputDataset = controlStrategyInputDatasets[i].getInputDataset();
                if (!inputDataset.getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    String tableName = qualifiedEmissionTableName(controlStrategyInputDatasets[i].getInputDataset());
                    boolean isPointDataset = inputDataset.getDatasetType().getName().equals(DatasetType.orlPointInventory);
                    String inventoryColumnDelimitedList = columnDelimitedList;
                    ResultSetMetaData md = getDatasetResultSetMetaData(tableName);
                    //we need to figure out what dataset columns we have to work with.
                    //for example, nonpoint inv won't have plantid, and point inv has no rpen
                    //alias these columns and use null as the value
                    byte designCapacityColumnCount = 0;
                    boolean hasDesignCapacityColumns = false;
                    boolean hasSICColumn = false;
                    boolean hasNAICSColumn = false;
                    for (int j = 1; j <= md.getColumnCount(); j++) {
                        if (md.getColumnName(j).equalsIgnoreCase("naics")) {
                            hasNAICSColumn = true;
                        } else if (md.getColumnName(j).equalsIgnoreCase("sic")) {
                            hasSICColumn = true;
                        } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity")) {
                            ++designCapacityColumnCount;
                        } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity_unit_numerator")) {
                            ++designCapacityColumnCount;
                        } else if (md.getColumnName(j).equalsIgnoreCase("design_capacity_unit_denominator")) {
                            ++designCapacityColumnCount;
                        }
                    }
                    if (isPointDataset) {
                        if (designCapacityColumnCount == 3) hasDesignCapacityColumns = true;
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("RPEN", "100::double precision as RPEN");
                        if (!hasDesignCapacityColumns) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, null::text as DESIGN_CAPACITY_UNIT_NUMERATOR, null::text as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                        if (!hasNAICSColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("NAICS", "null::text as NAICS");
                        if (!hasSICColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SIC", "null::text as SIC");
                    } else {
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("PLANTID, POINTID, STACKID, SEGMENT, PLANT", "''::text as PLANTID, ''::text as POINTID, ''::text as STACKID, ''::text as SEGMENT, ''::text as PLANT");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("DESIGN_CAPACITY, DESIGN_CAPACITY_UNIT_NUMERATOR, DESIGN_CAPACITY_UNIT_DENOMINATOR", "null::double precision as DESIGN_CAPACITY, null::text as DESIGN_CAPACITY_UNIT_NUMERATOR, null::text as DESIGN_CAPACITY_UNIT_DENOMINATOR");
                        inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("STKFLOW", "null::double precision as STKFLOW");
                        if (!hasNAICSColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("NAICS", "null::text as NAICS");
                        if (!hasSICColumn) inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SIC", "null::text as SIC");
                    }
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("SECTOR", "'" + getDatasetSector(inputDataset) +  "' as SECTOR");
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_DATASET_ID", inputDataset.getId() +  "::integer as ORIGINAL_DATASET_ID");
                    inventoryColumnDelimitedList = inventoryColumnDelimitedList.replaceAll("ORIGINAL_RECORD_ID", "RECORD_ID as ORIGINAL_RECORD_ID");
                    VersionedQuery versionedQuery = new VersionedQuery(version(inputDataset, controlStrategyInputDatasets[i].getVersion()));
                    sql += (i > 0 ? " union all " : "") 
                        + "select " + mergedDataset.getId() + " as dataset_id, " + inventoryColumnDelimitedList + " "
                        + "from " + tableName + " e "
                        + "where " + versionedQuery.query()
                        + loader.getFilterForSourceQuery();
                }
            }
//System.out.println(sql);
            System.out.println(System.currentTimeMillis() + " " + sql);
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data into the merged inventory dataset table" + "\n" + e.getMessage());
        }
    }
    
    private String getDatasetSector(EmfDataset dataset) {
        String sector = "";
        Sector[] sectors = dataset.getSectors();
        if (sectors != null) {
            if (sectors.length > 0) {
                sector = sectors[0].getName();
            }
        }
        return sector;
    }
    
    private void truncateORLMergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            datasource.query().execute("TRUNCATE " + qualifiedEmissionTableName(mergedDataset));
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data into the merged inventory dataset table" + "\n" + e.getMessage());
        }
    }
    
    private ResultSetMetaData getDatasetResultSetMetaData(String qualifiedTableName) throws EmfException {
        ResultSet rs = null;
        ResultSetMetaData md = null;
        System.out.println(System.currentTimeMillis() + " get ResultSetMetaData");
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTableName + " where 1 = 0");
            md = rs.getMetaData();
        } catch (SQLException e) {
            throw new EmfException("Error occured when getting metadata for the inventory dataset table" + "\n" + e.getMessage());
        }
        return md;
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
}
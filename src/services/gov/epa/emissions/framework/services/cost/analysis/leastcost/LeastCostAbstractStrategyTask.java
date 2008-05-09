package gov.epa.emissions.framework.services.cost.analysis.leastcost;

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

public abstract class LeastCostAbstractStrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
    protected ControlStrategyResult leastCostCMWorksheetResult;

    protected ControlStrategyResult leastCostCurveSummaryResult;
    
    public LeastCostAbstractStrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    protected void compareInventoriesTemporalResolution() {
        ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();
        if (inputDatasets.length > 1) {
            Date startDate1 = null;
            Date stopDate1 = null;
            Date startDate2 = null;
            Date stopDate2 = null;
            int count = 0;
            for (ControlStrategyInputDataset inputDataset : controlStrategy.getControlStrategyInputDatasets()) 
                if (!inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    startDate1 = inputDatasets[0].getInputDataset().getStartDateTime();
                    stopDate1 = inputDatasets[0].getInputDataset().getStopDateTime();
                    if (startDate1 == null || stopDate1 == null) 
                        setStatus("The dataset: " + inputDatasets[0].getInputDataset() + ", is missing a start or stop date time.");
                    if (count > 0) {
                        startDate2 = inputDataset.getInputDataset().getStartDateTime();
                        stopDate2 = inputDataset.getInputDataset().getStopDateTime();
                        
                        if (startDate1 != null && stopDate1 != null 
                                && startDate2 != null && stopDate2 != null
                                && (!startDate1.equals(startDate2) 
                                        || !stopDate1.equals(stopDate2))
                                        ) {
                            setStatus("The datasets have different start or stop date times.");
                            break;
                        }
                        startDate2 = startDate1;
                        stopDate2 = stopDate1;
                    }
                    ++count;
                }
        }
    }

    protected void finalizeCMWorksheetResult() throws EmfException {
        //finalize the result, update completion time and run status...
        leastCostCMWorksheetResult.setCompletionTime(new Date());
        leastCostCMWorksheetResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCMWorksheetResult);
        saveControlStrategySummaryResult(leastCostCMWorksheetResult);
        runSummaryQASteps((EmfDataset)leastCostCMWorksheetResult.getDetailedResultDataset(), 0);
    }
        
    protected void finalizeCostCuveSummaryResult() throws EmfException {
        //finalize the result, update completion time and run status...
        leastCostCurveSummaryResult.setCompletionTime(new Date());
        leastCostCurveSummaryResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCurveSummaryResult);
        saveControlStrategySummaryResult(leastCostCurveSummaryResult);
        runSummaryQASteps((EmfDataset)leastCostCurveSummaryResult.getDetailedResultDataset(), 0);
    }
        
    protected ControlStrategyInputDataset getInventory() {
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
    
    protected DatasetType getORLMergedInventoryDatasetType() {
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

    protected String getORLMergedInventoryDatasetDescription(TableFormat tableFormat, String country) {
        return "#" + tableFormat.identify() + "\n#COUNTRY " + country + "\n#YEAR " + controlStrategy.getInventoryYear();
    }
    
    protected void populateORLMergedDataset(EmfDataset mergedDataset) throws EmfException {
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
    
    protected String getDatasetSector(EmfDataset dataset) {
        String sector = "";
        Sector[] sectors = dataset.getSectors();
        if (sectors != null) {
            if (sectors.length > 0) {
                sector = sectors[0].getName();
            }
        }
        return sector;
    }
    
    protected void truncateORLMergedDataset(EmfDataset mergedDataset) throws EmfException {
        try {
            datasource.query().execute("TRUNCATE " + qualifiedEmissionTableName(mergedDataset));
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data into the merged inventory dataset table" + "\n" + e.getMessage());
        }
    }
    
    protected ResultSetMetaData getDatasetResultSetMetaData(String qualifiedTableName) throws EmfException {
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

    protected Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    protected void mergeInventoryDatasets() throws EmfException {
        //if there is more than one input inventory, then merge these into one dataset, 
        //then we use that as the input to the strategy run
        if (controlStrategy.getControlStrategyInputDatasets().length > 1) {
            ControlStrategyResult[] results = loader.getControlStrategyResults();
//        if (controlStrategyInputDatasetCount >= 1) {
            ControlStrategyInputDataset[] inputDatasets = controlStrategy.getControlStrategyInputDatasets();

            //TODO: look for any errors or warnings in the input inventories
            //i.e., missing sector or differing temporal period
            compareInventoriesTemporalResolution();
            //look for missing sector
            for (ControlStrategyInputDataset inputDataset : inputDatasets) 
                if (!inputDataset.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
                    if (getDatasetSector(inputDataset.getInputDataset()).length() == 0) 
                        setStatus("The dataset: " + inputDataset.getInputDataset().getName() + ", does not have a sector specified.");
                }
            
            //check to see if exists already, if so, then truncate its data and start over...
            boolean hasMergedDataset = false;
            EmfDataset mergedDataset = null;
            //see if it already has a merged dataset
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
                if (controlStrategy.getDeleteResults() || results.length == 0) truncateORLMergedDataset(mergedDataset);
            }
            if (controlStrategy.getDeleteResults() || results.length == 0) populateORLMergedDataset(mergedDataset);
            
        }
    }
}
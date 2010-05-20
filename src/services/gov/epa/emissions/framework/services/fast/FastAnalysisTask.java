package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class FastAnalysisTask {

    protected FastAnalysis fastAnalysis;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    private User user;
    
    private int recordCount;
    
    private StatusDAO statusDAO;
    
    private FastDAO fastRunDAO;
    
    private DatasetCreator creator;
    
    private Keywords keywords;

//    private TableFormat tableFormat;
    
    protected List<FastAnalysisOutput> fastRunOutputList;

    public FastAnalysisTask(FastAnalysis fastRun, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        this.fastAnalysis = fastRun;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.fastRunDAO = new FastDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(null, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.fastRunOutputList = new ArrayList<FastAnalysisOutput>();
        //setup the strategy run
        setup();
    }

    private void setup() {
        //
    }
    
    protected FastRunOutput createFastRunOutput(FastRunOutputType fastRunOutputType, EmfDataset outputDataset, EmfDataset inventory, int inventoryVersion) throws EmfException {
        FastRunOutput result = new FastRunOutput();
        result.setFastRunId(fastAnalysis.getId());
        result.setOutputDataset(outputDataset);
//        result.setInventoryDataset(inventory);
//        result.setInventoryDatasetVersion(inventoryVersion);
        
        result.setType(fastRunOutputType);
        result.setStartDate(new Date());
        result.setRunStatus("Start processing inventory dataset");

        //persist output
        saveFastRunOutput(result);
        return result;
    }
    
    public FastRunOutput createEECSDetailedMappingResultOutput(FastRunInventory fastRunInventory) throws Exception {
        EmfDataset inventory = fastRunInventory.getDataset();

        //setup result
        FastRunOutput eecsDetailedMappingOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");
            
            EmfDataset eecsDetailedMapping =  createEECSDetailedMappingDataset(inventory);

            eecsDetailedMappingOutput = createFastRunOutput(getFastRunOutputType(FastRunOutputType.DETAILED_EECS_MAPPING_RESULT), eecsDetailedMapping, inventory, fastRunInventory.getVersion());

            populateEECSDetailedMapping(fastRunInventory, eecsDetailedMappingOutput);
            
            updateOutputDatasetVersionRecordCount(eecsDetailedMappingOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");

        } catch(EmfException ex) {
            runStatus = "Failed creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ". Error processing inventory, " + fastRunInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (eecsDetailedMappingOutput != null) {
                eecsDetailedMappingOutput.setCompletionDate(new Date());
                eecsDetailedMappingOutput.setRunStatus(runStatus);
                saveFastRunOutput(eecsDetailedMappingOutput);
            }
        }

        return eecsDetailedMappingOutput;
    }

    public FastRunOutput createSectorDetailedMappingResultOutput(FastRunInventory fastRunInventory) throws Exception {
        EmfDataset inventory = fastRunInventory.getDataset();

        //setup result
        FastRunOutput sectorDetailedMappingOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + " from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");
            EmfDataset eecsDetailedMapping =  createSectorDetailedMappingDataset(inventory);

            sectorDetailedMappingOutput = createFastRunOutput(getFastRunOutputType(FastRunOutputType.DETAILED_SECTOR_MAPPING_RESULT), eecsDetailedMapping, inventory, fastRunInventory.getVersion());

            populateSectorDetailedMapping(fastRunInventory, sectorDetailedMappingOutput);
            
            updateOutputDatasetVersionRecordCount(sectorDetailedMappingOutput);

            runStatus = "Completed.";

            setStatus("Completed creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + " from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");
        } catch(EmfException ex) {
            runStatus = "Failed creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + ". Error processing inventory, " + fastRunInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (sectorDetailedMappingOutput != null) {
                sectorDetailedMappingOutput.setCompletionDate(new Date());
                sectorDetailedMappingOutput.setRunStatus(runStatus);
                saveFastRunOutput(sectorDetailedMappingOutput);
            }
        }

        return sectorDetailedMappingOutput;
    }

    public FastRunOutput createSectorSpecificInventoryOutput(String sector, FastRunInventory fastRunInventory) throws Exception {
        EmfDataset inventory = fastRunInventory.getDataset();

        //setup result
        FastRunOutput sectorSpecificInventoryOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating sector (" + sector + ") specific " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");

            EmfDataset newInventory =  createSectorSpecificInventoryDataset(sector, inventory);

            sectorSpecificInventoryOutput = createFastRunOutput(getFastRunOutputType(FastRunOutputType.SECTOR_SPECIFIC_INVENTORY), newInventory, inventory, fastRunInventory.getVersion());

            populateSectorSpecificInventory(sector, fastRunInventory, sectorSpecificInventoryOutput);
            
            updateOutputDatasetVersionRecordCount(sectorSpecificInventoryOutput);

            runStatus = "Completed.";

            setStatus("Completed creating sector (" + sector + ") specific " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + fastRunInventory.getDataset().getName() 
                    + ".");
        } catch(EmfException ex) {
            runStatus = "Failed creating sector (" + sector + ") specific " + inventory.getDatasetType().getName() + ". Error processing inventory, " + fastRunInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (sectorSpecificInventoryOutput != null) {
                sectorSpecificInventoryOutput.setCompletionDate(new Date());
                sectorSpecificInventoryOutput.setRunStatus(runStatus);
                saveFastRunOutput(sectorSpecificInventoryOutput);
            }
        }

        return sectorSpecificInventoryOutput;
    }

    private EmfDataset createEECSDetailedMappingDataset(EmfDataset inventory) throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.EECS_DETAILED_MAPPING_RESULT);
        return creator.addDataset("ds", fastAnalysis.getName() + "_" + DatasetType.EECS_DETAILED_MAPPING_RESULT, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }

    private EmfDataset createSectorDetailedMappingDataset(EmfDataset inventory) throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.SECTOR_DETAILED_MAPPING_RESULT);
        return creator.addDataset("ds", fastAnalysis.getName() + "_" + DatasetType.SECTOR_DETAILED_MAPPING_RESULT, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }

    private EmfDataset createSectorSpecificInventoryDataset(String sector, EmfDataset inventory) throws EmfException {
        DatasetType datasetType = inventory.getDatasetType();
        return creator.addDataset(fastAnalysis.getName() + "_" + inventory.getName() + "_" + sector, inventory, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                inventory.getDescription() + "\nSECTOR_SCENARIO_SECTOR=" + sector);
    }

    private void populateEECSDetailedMapping(FastRunInventory fastRunInventory, FastRunOutput fastRunOutput) throws EmfException {
        
        EmfDataset inventory = fastRunInventory.getDataset();
        int inventoryVersionNumber = fastRunInventory.getVersion();
        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
        String inventoryTableName = qualifiedEmissionTableName(inventory);

        EmfDataset eecsMappingDataset = null;//fastRun.getEecsMapppingDataset();
        int eecsMappingDatasetVersionNumber = 0;//fastRun.getEecsMapppingDatasetVersion();
        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
        
        cleanMappingDataset(eecsMappingDataset);
//        EmfDataset sectorMappingDataset = fastRun.getSectorMapppingDataset();
//        int sectorMappingDatasetVersionNumber = fastRun.getSectorMapppingDatasetVersion();
//        Version sectorMappingDatasetVersion = version(sectorMappingDataset.getId(), sectorMappingDatasetVersionNumber);
//        VersionedQuery sectorMappingDatasetVersionedQuery = new VersionedQuery(sectorMappingDatasetVersion);
//        String sectorMappingDatasetTableName = qualifiedEmissionTableName(sectorMappingDataset);

        
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(fastRunOutput.getOutputDataset()) + " (dataset_id, version, fips, plantid, pointid, stackid, segment, scc, plant, poll, ann_emis, avd_emis, mact, naics, map_mact, map_naics, map_scc, eecs, weight) " 
        + "select " + fastRunOutput.getOutputDataset().getId() + " as dataset_id, 0 as version, " 
        + "inv.fips, "
        + "inv.plantid, inv.pointid, "
        + "inv.stackid, inv.segment, "
        + "inv.scc, inv.plant, "
        + "inv.poll, "
        + "inv.ann_emis, "
        + "inv.avd_emis, "
        + "inv.mact, inv.naics, "
        + "map_mact, map_naics, "
        + "map_scc, "
        + "tblMatch.eecs, weight "
        + "from " + inventoryTableName + " inv "
        + "left outer join ( "
        + "select inv.record_id, "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics, " 
        + "eecs_map.scc as map_scc, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.mact = inv.mact "
        + "where eecs_map.mact is not null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id,  "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics,  "
        + "eecs_map.scc as map_scc,  "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.naics = inv.naics "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is not null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id,  "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics, " 
        + "eecs_map.scc as map_scc,  "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.scc = inv.scc "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is not null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + ") tblMatch "
        + "on tblMatch.record_id = inv.record_id "
        + "where " + inventoryVersionedQuery.query() + " ";
            
        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " table" + "\n" + e.getMessage());
        }
    }

    private void populateSectorDetailedMapping(FastRunInventory fastRunInventory, FastRunOutput fastRunOutput) throws EmfException {
        
        EmfDataset inventory = fastRunInventory.getDataset();
        int inventoryVersionNumber = fastRunInventory.getVersion();
        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
        String inventoryTableName = qualifiedEmissionTableName(inventory);

        EmfDataset eecsMappingDataset = null;//fastRun.getEecsMapppingDataset();
        int eecsMappingDatasetVersionNumber = 0;//fastRun.getEecsMapppingDatasetVersion();
        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
        
        EmfDataset sectorMappingDataset = null;//fastRun.getSectorMapppingDataset();
        int sectorMappingDatasetVersionNumber = 0;//fastRun.getSectorMapppingDatasetVersion();
        Version sectorMappingDatasetVersion = version(sectorMappingDataset.getId(), sectorMappingDatasetVersionNumber);
        VersionedQuery sectorMappingDatasetVersionedQuery = new VersionedQuery(sectorMappingDatasetVersion);
        String sectorMappingDatasetTableName = qualifiedEmissionTableName(sectorMappingDataset);

        cleanMappingDataset(sectorMappingDataset);

        
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(fastRunOutput.getOutputDataset()) + " (dataset_id, version, fips, plantid, pointid, stackid, segment, scc, plant, poll, ann_emis, avd_emis, eecs, sector, weight) " 
        + "select " + fastRunOutput.getOutputDataset().getId() + " as dataset_id, 0 as version, " 
        + "inv.fips, "
        + "inv.plantid, inv.pointid, "
        + "inv.stackid, inv.segment, "
        + "inv.scc, inv.plant, "
        + "inv.poll, "
        + "inv.ann_emis, "
        + "inv.avd_emis, "
        + "tbleecs.eecs, tblsector.sector, "
        + "tblsector.weight "
        + "from " + inventoryTableName + " inv "
        + "left outer join ( "
        + "select distinct on (record_id) "
        + "record_id, eecs, weight "
        + "from ( "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.mact = inv.mact "
        + "where eecs_map.mact is not null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.naics = inv.naics "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is not null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.scc = inv.scc "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is not null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + ") tbleecs "
        + "order by record_id, weight, eecs "
        + ") tbleecs "
        + "on tbleecs.record_id = inv.record_id "
        + "left outer join ( "
        + "select sector, eecs, weight "
        + "from " + sectorMappingDatasetTableName + " as sector_map "
        + "where sector_map.eecs is not null "
        + "and " + sectorMappingDatasetVersionedQuery.query() + " "
        + ") tblsector "
        + "on tblsector.eecs = tbleecs.eecs "
        + "where " + inventoryVersionedQuery.query() + " ";
            
        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " table" + "\n" + e.getMessage());
        }
    }

    private void populateSectorSpecificInventory(String sector, FastRunInventory fastRunInventory, FastRunOutput fastRunOutput) throws EmfException {
        
        EmfDataset inventory = fastRunInventory.getDataset();
        int inventoryVersionNumber = fastRunInventory.getVersion();
        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
        String inventoryTableName = qualifiedEmissionTableName(inventory);

        EmfDataset eecsMappingDataset = null;//fastRun.getEecsMapppingDataset();
        int eecsMappingDatasetVersionNumber = 0;//fastRun.getEecsMapppingDatasetVersion();
        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
        
        EmfDataset sectorMappingDataset = null;//fastRun.getSectorMapppingDataset();
        int sectorMappingDatasetVersionNumber = 0;//fastRun.getSectorMapppingDatasetVersion();
        Version sectorMappingDatasetVersion = version(sectorMappingDataset.getId(), sectorMappingDatasetVersionNumber);
        VersionedQuery sectorMappingDatasetVersionedQuery = new VersionedQuery(sectorMappingDatasetVersion);
        String sectorMappingDatasetTableName = qualifiedEmissionTableName(sectorMappingDataset);

        String selectList = "select " + fastRunOutput.getOutputDataset().getId() + " as dataset_id, '' as delete_versions, 0 as version";
        String columnList = "dataset_id, delete_versions, version";
        Column[] columns = inventory.getDatasetType().getFileFormat().cols();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("eecs")) {
                selectList += ", tbleecs.eecs";
                columnList += "," + columnName;
            } else {
                selectList += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(fastRunOutput.getOutputDataset()) + " (" + columnList + ") " 
        + selectList + " "
        + "from " + inventoryTableName + " inv "
        + "left outer join ( "
        + "select distinct on (record_id) "
        + "record_id, eecs, weight "
        + "from ( "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.mact = inv.mact "
        + "where eecs_map.mact is not null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.naics = inv.naics "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is not null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + "union  "
        + "select inv.record_id, "
        + "eecs_map.eecs, eecs_map.weight "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.scc = inv.scc "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is not null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + ") tbleecs "
        + "order by record_id, weight, eecs "
        + ") tbleecs "
        + "on tbleecs.record_id = inv.record_id "
        + "left outer join ( "
        + "select sector, eecs, weight "
        + "from " + sectorMappingDatasetTableName + " as sector_map "
        + "where sector_map.eecs is not null "
        + "and " + sectorMappingDatasetVersionedQuery.query() + " "
        + ") tblsector "
        + "on tblsector.eecs = tbleecs.eecs "
        + "where " + inventoryVersionedQuery.query() + " "
        + "and tblsector.sector = '" + sector.replace("'", "''") + "'";
            
        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to " + fastRunOutput.getOutputDataset().getName() + " table" + "\n" + e.getMessage());
        }
    }

    public void run() throws EmfException {
        
        //get rid of strategy results
        deleteStrategyOutputs();

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
//            FastRunInventory[] fastRunInventories = fastRun.getInventories();
//            
//            for (int i = 0; i < fastRunInventories.length; i++) {
                try {
                    createEECSDetailedMappingResultOutput(new FastRunInventory());
//                    FastRunOutput sectorDetailedMappingResultOutput = createSectorDetailedMappingResultOutput(fastRunInventories[i]);
                    
                    //see if the scenario has sectors specified, if not then lets auto populate what was found during the above
                    //analysis
                    
                    //TODO:   need to auto populate sector_scenario_sector table
//                    String[] sectors = getDistinctSectorListFromDataset(sectorDetailedMappingResultOutput.getOutputDataset().getId(), 0);
//                    if (fastRun.getSectors().length == 0) {
//                        fastRun.setSectors(sectors);
//                        saveFastRun(fastRun);
//                    }
                    
//                    for (String sector : sectors) {
//                        createSectorSpecificInventoryOutput(sector, fastRunInventories[i]);
//                    }

                    recordCount = 0; //loader.getRecordCount();
                    status = "Completed.";
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Failed. Error processing : " + "" + ". " + e.getMessage();
//                    setStatus(status);
                } finally {

                    //see if there was an error, if so, make sure and propogate to the calling method.
                    if (status.startsWith("Failed"))
                        throw new EmfException(status);
                            
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
                        status = "Cancelled. Sector scenario run was cancelled: " + fastAnalysis.getName();
                        setStatus(status);
                        return;
//                        throw new EmfException("Strategy run was cancelled.");
                    }
                    //
                }
//            }

            //now create the measure summary result based on the results from the strategy run...
//            generateStrategyMeasureSummaryResult();

//            //now create the county summary result based on the results from the strategy run...
//            generateStrategyCountySummaryResult();

        } catch (Exception e) {
            status = "Failed. Error processing inventory";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
//                updateVersionInfo();
            } catch (Exception e) {
                status = "Failed. Error processing inventory";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                disconnectDbServer();
            }
        }
    }

    private void afterRun() {
        // NOTE Auto-generated method stub
        
    }

    private void beforeRun() {
//        for (FastRunInventory fastRunInventory : fastRun.getInventories()) {
//            //make sure inventory has indexes created...
//            makeSureInventoryDatasetHaveIndexes(fastRunInventory);
//        }
//// NOTE Auto-generated method stub
        
    }

    protected void deleteStrategyOutputs() throws EmfException {
        //get rid of strategy results...
        if (true){
            Session session = sessionFactory.getSession();
            try {
                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
                //first get the datasets to delete
                EmfDataset[] datasets = fastRunDAO.getOutputDatasets(fastAnalysis.getId(), session);
                if (datasets != null) {
                    for (EmfDataset dataset : datasets) {
                        if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                            setStatus("The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.");
                        } else {
                            dsList.add(dataset);
                        }
                    }
                }
//                EmfDataset[] dsList = controlStrategyDAO.getResultDatasets(fastRun.getId(), session);
                //get rid of old strategy results...
                removeFastRunOutputs();
                //delete and purge datasets
                if (dsList != null && dsList.size() > 0){
                    fastRunDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                throw new EmfException("Could not remove sector scenario outputs.");
            } finally {
                session.close();
            }
        }
    }
    
    protected int getDaysInMonth(int year, int month) {
        return month != - 1 ? DateUtil.daysInZeroBasedMonth(year, month) : 31;
    }

    public void makeSureInventoryDatasetHaveIndexes(FastRunInventory fastRunInventory) {
        String query = "SELECT public.create_orl_table_indexes('" + emissionTableName(fastRunInventory.getDataset()).toLowerCase() + "');analyze " + qualifiedEmissionTableName(fastRunInventory.getDataset()).toLowerCase() + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    protected boolean isRunStatusCancelled() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return fastRunDAO.getFastRunRunStatus(fastAnalysis.getId(), session).equals("Cancelled");
        } catch (RuntimeException e) {
            throw new EmfException("Could not check if strategy run was cancelled.");
        } finally {
            session.close();
        }
    }
    
    protected int getRecordCount(EmfDataset dataset) throws EmfException {
        String query = "SELECT count(1) as record_count "
            + " FROM " + qualifiedEmissionTableName(dataset);
        ResultSet rs = null;
        Statement statement = null;
        int recordCount = 0;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /**/ }
                rs = null;
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { /**/ }
                statement = null;
            }
        }
        return recordCount;
    }

    protected String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String qualifiedName(String table) {
        return datasource.getName() + "." + table;
    }

//    private FastRunOutput createStrategyCountySummaryResult() throws EmfException {
//        FastRunOutput result = new FastRunOutput();
//        result.setFastRunId(fastRun.getId());
//        EmfDataset summaryResultDataset = createCountySummaryDataset();
//        
//        result.setOutputDataset(summaryResultDataset);
//        
//        result.setType(getFastRunOutputType(FastRunOutputType.detailedEECSMapping));
//        result.setStartDate(new Date());
//        result.setRunStatus("Start processing " + FastRunOutputType.detailedEECSMapping);
//
//        //persist result
//        saveFastRunSummaryOutput(result);
//        return result;
//    }

    protected FastRunOutputType getFastRunOutputType(String name) throws EmfException {
        FastRunOutputType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = fastRunDAO.getFastRunOutputType(name, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    protected EmfDataset getDataset(int id) {
        EmfDataset dataset = null;
        Session session = sessionFactory.getSession();
        try {
            dataset = new DatasetDAO().getDataset(session, id);
        } finally {
            session.close();
        }
        return dataset;
    }

    protected DatasetType getDatasetType(String name) {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    protected FastRunOutput[] getFastRunOutputs() {
        FastRunOutput[] results = new FastRunOutput[] {};
        Session session = sessionFactory.getSession();
        try {
            results = fastRunDAO.getFastRunOutputs(fastAnalysis.getId(), session).toArray(new FastRunOutput[0]);
        } finally {
            session.close();
        }
        return results;
    }

//    private String summaryResultDatasetDescription(String datasetTypeName) {
//        return "#" + datasetTypeName + " result\n" + 
//            "#Implements control strategy: " + fastRun.getName() + "\n#";
//    }

    protected void saveFastRunOutput(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRunOutput(fastRunOutput, session);
//          runQASteps(fastRunOutput);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveFastRun(FastRun fastRun) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRun(fastRun, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save sector scenario: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveFastRunSummaryOutput(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.updateFastRunOutput(fastRunOutput, session);
        } catch (Exception e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    protected void updateOutputDatasetVersionRecordCount(FastRunOutput fastRunOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();
        
        try {
            EmfDataset result = fastRunOutput.getOutputDataset();
            
            if (result != null) {
                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());
                
                if (version != null)
                    updateVersion(result, version, dbServer, session, dao);
            }
        } catch (Exception e) {
            throw new EmfException("Cannot update result datasets (strategy id: " + fastRunOutput.getFastRunId() + "). " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session, DatasetDAO dao) throws Exception {
        version = dao.obtainLockOnVersion(user, version.getId(), session);
        version.setNumberRecords((int)dao.getDatasetRecordsNumber(dbServer, session, dataset, version));
        dao.updateVersionNReleaseLock(version, session);
    }

    private void removeFastRunOutputs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            fastRunDAO.removeFastRunResults(fastAnalysis.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

//    private void removeFastRunOutput(int resultId) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            controlStrategyDAO.removeFastRunOutput(fastRun.getId(), resultId, session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not remove previous control strategy result(s)");
//        } finally {
//            session.close();
//        }
//    }

    public FastAnalysis getFastAnalysis() {
        return fastAnalysis;
    }

    protected void runQASteps(FastAnalysisOutput fastAnalysisOutput) {
//        EmfDataset resultDataset = (EmfDataset)fastRunOutput.getDetailedResultDataset();
        if (recordCount > 0) {
//            runSummaryQASteps(resultDataset, 0);
        }
//        excuteSetAndRunQASteps(inputDataset, fastRun.getDatasetVersion());
    }

    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
        //11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step templates...
        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
        if (qaStepTemplates != null) {
            String[] qaStepTemplateNames = new String[qaStepTemplates.length];
            for (int i = 0; i < qaStepTemplates.length; i++) qaStepTemplateNames[i] = qaStepTemplates[i].getName();
            qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, ""/*fastRun.getExportDirectory()*/);
        }
    }

    protected void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }
    
    public long getRecordCount() {
        return recordCount;
    }

    protected void addStatus(FastRunInventory fastRunInventory) {
        setStatus("Completed processing sector scenario inventory: " 
                + fastRunInventory.getDataset().getName() 
                + ".");
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("FastRun");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
    
    public String getFilterForSourceQuery() {
        String filterForSourceQuery = "";
        String sqlFilter = "";
        String filter = "";
        
        //get and build strategy filter...
        if (filter == null || filter.trim().length() == 0)
            sqlFilter = "";
        else 
            sqlFilter = " and (" + filter + ") "; 

        filterForSourceQuery = sqlFilter;
        return filterForSourceQuery;
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }
    
    private void cleanMappingDataset(EmfDataset mappingDataset) throws EmfException {
        ResultSet rs = null;
        Connection connection = null;
        Statement statement = null;
        String mappingDatasetQualifiedEmissionTableName = qualifiedEmissionTableName(mappingDataset);
        
//        boolean hasRpenColumn = hasColName("rpen",mappingDataset.getDatasetType().getFileFormat());
//        boolean hasMactColumn = hasColName("mact",(FileFormatWithOptionalCols) formatUnit.fileFormat());
//        boolean hasSicColumn = hasColName("sic",(FileFormatWithOptionalCols) formatUnit.fileFormat());
//        boolean hasCpriColumn = hasColName("cpri",(FileFormatWithOptionalCols) formatUnit.fileFormat());
//        boolean hasPrimaryDeviceTypeCodeColumn = hasColName("primary_device_type_code",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasSectorColumn = hasColName("sector", mappingDataset.getDatasetType().getFileFormat());
        try {
//            first lets clean up "" values and convert them to null values...
       

            //check to see if -9 even shows for any of the columns in the inventory
            String sql = "select 1 " 
                    + " from " + mappingDatasetQualifiedEmissionTableName
                    + " where dataset_id = " + mappingDataset.getId()
                    + " and (" 
                    + " trim(eecs) = '' or strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0"
                    + " or trim(mact) = '' or strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0"
                    + " or trim(naics) = '' or strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0"
                    + " or trim(scc) = '' or strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0"
                    + (hasSectorColumn ? " or trim(sector) = '' or strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0" : "")
                    +  ") limit 1;";
            
            connection = datasource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            System.out.println("start fix check query " + System.currentTimeMillis());
            rs = statement.executeQuery(sql);
            System.out.println("end fix check query " + System.currentTimeMillis());
            boolean foundNegative9 = false;
            while (rs.next()) {
                foundNegative9 = true;
            }

            if (foundNegative9) {
                sql = "update " + mappingDatasetQualifiedEmissionTableName
                    + " set eecs = case when trim(eecs) = '' then null::character varying(10) when strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0 then trim(eecs) else eecs end "
                    + "     ,mact = case when trim(mact) = '' then null::character varying(4) when strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0 then trim(mact) else mact end "
                    + "     ,scc = case when trim(scc) = '' then null::character varying(10) when strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0 then trim(scc) else scc end "
                    + "     ,naics = case when trim(naics) = '' then null::character varying(6) when strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0 then trim(naics) else naics end "
                    + (hasSectorColumn ? "     ,sector = case when trim(sector) = '' then null::character varying(64) when strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0 then trim(sector) else sector end " : "")
                    + " where dataset_id = " + mappingDataset.getId()
                    + " and (" 
                    + " trim(eecs) = '' or strpos(trim(substr(eecs, 1, 1) || substr(eecs, length(eecs), 1)), ' ') > 0"
                    + " or trim(mact) = '' or strpos(trim(substr(mact, 1, 1) || substr(mact, length(mact), 1)), ' ') > 0"
                    + " or trim(naics) = '' or strpos(trim(substr(naics, 1, 1) || substr(naics, length(naics), 1)), ' ') > 0"
                    + " or trim(scc) = '' or strpos(trim(substr(scc, 1, 1) || substr(scc, length(scc), 1)), ' ') > 0"
                    + (hasSectorColumn ? " or trim(sector) = '' or strpos(trim(substr(sector, 1, 1) || substr(sector, length(sector), 1)), ' ') > 0" : "")
                    +  ");";
                
                
//                sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end 
                
                statement.execute(sql);
                statement.execute("vacuum " + mappingDatasetQualifiedEmissionTableName);
                statement.close();
            }
        } catch (Exception exc) {
            // NOTE: this closes the db server for other importers
            // try
            // {
            // if ((connection != null) && !connection.isClosed()) connection.close();
            // }
            // catch (Exception ex)
            // {
            // throw ex;
            // }
            // throw exc;
            throw new EmfException(exc.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
        }

    }
    
    protected boolean hasColName(String colName, XFileFormat fileFormat) {
        Column[] cols = fileFormat.cols();
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }
}

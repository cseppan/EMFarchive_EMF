package gov.epa.emissions.framework.services.sms;

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
import gov.epa.emissions.commons.io.importer.DataTable;
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

public class SectorScenarioTask {

    protected SectorScenario sectorScenario;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    private User user;
    
    private int recordCount;
    
    private StatusDAO statusDAO;
    
    private SectorScenarioDAO sectorScenarioDAO;
    
    private DatasetCreator creator;
    
    private Keywords keywords;

//    private TableFormat tableFormat;
    
    protected List<SectorScenarioOutput> sectorScenarioOutputList;

    public SectorScenarioTask(SectorScenario sectorScenario, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        this.sectorScenario = sectorScenario;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.sectorScenarioDAO = new SectorScenarioDAO(dbServerFactory, sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator(null, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.sectorScenarioOutputList = new ArrayList<SectorScenarioOutput>();
        //setup the strategy run
        setup();
    }

    private void setup() {
        //
    }
    
    protected SectorScenarioOutput createSectorScenarioOutput(SectorScenarioOutputType sectorScenarioOutputType, EmfDataset outputDataset, EmfDataset inventory, int inventoryVersion) throws EmfException {
        SectorScenarioOutput result = new SectorScenarioOutput();
        result.setSectorScenarioId(sectorScenario.getId());
        result.setOutputDataset(outputDataset);
        result.setInventoryDataset(inventory);
        result.setInventoryDatasetVersion(inventoryVersion);
        
        result.setType(sectorScenarioOutputType);
        result.setStartDate(new Date());
        result.setRunStatus("Start processing inventory dataset");

        //persist output
        saveSectorScenarioOutput(result);
        return result;
    }
    
    protected SectorScenarioOutput createSectorScenarioOutput(SectorScenarioOutputType sectorScenarioOutputType, EmfDataset outputDataset) throws EmfException {
        SectorScenarioOutput result = new SectorScenarioOutput();
        result.setSectorScenarioId(sectorScenario.getId());
        result.setOutputDataset(outputDataset);
        
        result.setType(sectorScenarioOutputType);
        result.setStartDate(new Date());
        result.setRunStatus("Start processing inventory dataset");

        //persist output
        saveSectorScenarioOutput(result);
        return result;
    }

    public SectorScenarioOutput createEECSDetailedMappingResultOutput(SectorScenarioInventory[] sectorScenarioInventories) throws Exception {
        //setup result
        SectorScenarioOutput eecsDetailedMappingOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ".");
            EmfDataset eecsDetailedMapping =  createEECSDetailedMappingDataset();
            eecsDetailedMappingOutput = createSectorScenarioOutput(getSectorScenarioOutputType(SectorScenarioOutputType.DETAILED_EECS_MAPPING_RESULT), eecsDetailedMapping);
            for (SectorScenarioInventory sectorScenarioInventory : sectorScenarioInventories) {
//                EmfDataset inventory = sectorScenarioInventory.getDataset();
                setStatus("Started populating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " from the inventory, " 
                        + sectorScenarioInventory.getDataset().getName() 
                        + ".");
                populateEECSDetailedMapping(sectorScenarioInventory, eecsDetailedMappingOutput);
                setStatus("Completed populating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + " from the inventory, " 
                        + sectorScenarioInventory.getDataset().getName() 
                        + ".");
            }
            
            updateOutputDatasetVersionRecordCount(eecsDetailedMappingOutput);
            setStatus("Completed creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ".");

            runStatus = "Completed.";

        } catch(EmfException ex) {
            runStatus = "Failed creating " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (eecsDetailedMappingOutput != null) {
                eecsDetailedMappingOutput.setCompletionDate(new Date());
                eecsDetailedMappingOutput.setRunStatus(runStatus);
                saveSectorScenarioOutput(eecsDetailedMappingOutput);
            }
        }

        return eecsDetailedMappingOutput;
    }

    public SectorScenarioOutput createSectorDetailedMappingResultOutput(EmfDataset eecsDetailedMapping, SectorScenarioInventory[] sectorScenarioInventories) throws Exception {
        //setup result
        SectorScenarioOutput sectorDetailedMappingOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + ".");
            EmfDataset sectorDetailedMapping =  createSectorDetailedMappingDataset();

            sectorDetailedMappingOutput = createSectorScenarioOutput(getSectorScenarioOutputType(SectorScenarioOutputType.DETAILED_SECTOR_MAPPING_RESULT), sectorDetailedMapping);

//            for (SectorScenarioInventory sectorScenarioInventory : sectorScenarioInventories) {
//              EmfDataset inventory = sectorScenarioInventory.getDataset();
              setStatus("Started populating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + " from the " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ", " 
                      + eecsDetailedMapping.getName() 
                      + ".");
              populateSectorDetailedMapping(eecsDetailedMapping, sectorDetailedMappingOutput);
              setStatus("Completed populating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + " from the " + DatasetType.EECS_DETAILED_MAPPING_RESULT + ", " 
                      + eecsDetailedMapping.getName() 
                      + ".");
//            }

            updateOutputDatasetVersionRecordCount(sectorDetailedMappingOutput);

            setStatus("Completed creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + ".");

            runStatus = "Completed.";

        } catch(EmfException ex) {
            runStatus = "Failed creating " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (sectorDetailedMappingOutput != null) {
                sectorDetailedMappingOutput.setCompletionDate(new Date());
                sectorDetailedMappingOutput.setRunStatus(runStatus);
                saveSectorScenarioOutput(sectorDetailedMappingOutput);
            }
        }

        return sectorDetailedMappingOutput;
    }

    public SectorScenarioOutput createSectorSpecificInventoryOutput(EmfDataset sectorDetailedMappingDataset, String sector, SectorScenarioInventory sectorScenarioInventory) throws Exception {
        EmfDataset inventory = sectorScenarioInventory.getDataset();

        //setup result
        SectorScenarioOutput sectorSpecificInventoryOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + sectorScenarioInventory.getDataset().getName() 
                    + ".");

            EmfDataset newInventory =  createSectorSpecificInventoryDataset(sector, inventory);

            sectorSpecificInventoryOutput = createSectorScenarioOutput(getSectorScenarioOutputType(SectorScenarioOutputType.SECTOR_ANNOTATED_INVENTORY), newInventory, inventory, sectorScenarioInventory.getVersion());

            populateSectorAnnotatedInventory(sectorDetailedMappingDataset, new String[] { sector }, sectorScenarioInventory, sectorSpecificInventoryOutput);
            
            updateOutputDatasetVersionRecordCount(sectorSpecificInventoryOutput);

            runStatus = "Completed.";

            setStatus("Completed creating sector (" + sector + ") specific " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + sectorScenarioInventory.getDataset().getName() 
                    + ".");
        } catch(EmfException ex) {
            runStatus = "Failed creating sector (" + sector + ") specific " + inventory.getDatasetType().getName() + ". Error processing inventory, " + sectorScenarioInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (sectorSpecificInventoryOutput != null) {
                sectorSpecificInventoryOutput.setCompletionDate(new Date());
                sectorSpecificInventoryOutput.setRunStatus(runStatus);
                saveSectorScenarioOutput(sectorSpecificInventoryOutput);
            }
        }

        return sectorSpecificInventoryOutput;
    }

//    public SectorScenarioOutput createEecsAnnotatedInventoryOutput(EmfDataset eecsDetailedMappingDataset, SectorScenarioInventory sectorScenarioInventory) throws Exception {
//        EmfDataset inventory = sectorScenarioInventory.getDataset();
//
//        //setup result
//        SectorScenarioOutput eecsAnnotatedInventoryOutput = null;
//        String runStatus = "";
//        
//        //Create EECS Detailed Mapping Result Output
//        try {
//            setStatus("Started creating " + SectorScenarioOutputType.annotatedInventoryWithEECS + " from the inventory, " 
//                    + sectorScenarioInventory.getDataset().getName() 
//                    + ".");
//
//            EmfDataset newInventory =  createEecsAnnotatedInventoryDataset(inventory);
//
//            eecsAnnotatedInventoryOutput = createSectorScenarioOutput(getSectorScenarioOutputType(SectorScenarioOutputType.annotatedInventoryWithEECS), newInventory, inventory, sectorScenarioInventory.getVersion());
//
//            populateEecsAnnotatedInventory(eecsDetailedMappingDataset, sectorScenarioInventory, eecsAnnotatedInventoryOutput);
//            
//            updateOutputDatasetVersionRecordCount(eecsAnnotatedInventoryOutput);
//
//            runStatus = "Completed.";
//
//            setStatus("Completed creating " + SectorScenarioOutputType.annotatedInventoryWithEECS + " from the inventory, " 
//                    + sectorScenarioInventory.getDataset().getName() 
//                    + ".");
//        } catch(EmfException ex) {
//            runStatus = "Failed creating " + SectorScenarioOutputType.annotatedInventoryWithEECS + ". Error processing inventory, " + sectorScenarioInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
//            setStatus(runStatus);
//            throw ex;
//        } finally {
//            if (eecsAnnotatedInventoryOutput != null) {
//                eecsAnnotatedInventoryOutput.setCompletionDate(new Date());
//                eecsAnnotatedInventoryOutput.setRunStatus(runStatus);
//                saveSectorScenarioOutput(eecsAnnotatedInventoryOutput);
//            }
//        }
//
//        return eecsAnnotatedInventoryOutput;
//    }

    public SectorScenarioOutput createSectorAnnotatedInventoryOutput(EmfDataset sectorDetailedMappingDataset, String[] sectors, SectorScenarioInventory sectorScenarioInventory) throws Exception {
        EmfDataset inventory = sectorScenarioInventory.getDataset();

        //setup result
        SectorScenarioOutput sectorAnnotatedInventoryOutput = null;
        String runStatus = "";
        
        //Create EECS Detailed Mapping Result Output
        try {
            setStatus("Started creating " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + sectorScenarioInventory.getDataset().getName() 
                    + ".");

            EmfDataset newInventory =  createSectorAnnotatedInventoryDataset(inventory);

            sectorAnnotatedInventoryOutput = createSectorScenarioOutput(getSectorScenarioOutputType(SectorScenarioOutputType.SECTOR_SPECIFIC_INVENTORY), newInventory, inventory, sectorScenarioInventory.getVersion());

            populateSectorAnnotatedInventory(sectorDetailedMappingDataset, sectors, sectorScenarioInventory, sectorAnnotatedInventoryOutput);
            
            updateOutputDatasetVersionRecordCount(sectorAnnotatedInventoryOutput);

            runStatus = "Completed.";

            setStatus("Completed creating sector annotated " + inventory.getDatasetType().getName() + " inventory from the inventory, " 
                    + sectorScenarioInventory.getDataset().getName() 
                    + ".");
        } catch(EmfException ex) {
            runStatus = "Failed creating sector annotated " + inventory.getDatasetType().getName() + ". Error processing inventory, " + sectorScenarioInventory.getDataset().getName() + ". Exception = " + ex.getMessage();
            setStatus(runStatus);
            throw ex;
        } finally {
            if (sectorAnnotatedInventoryOutput != null) {
                sectorAnnotatedInventoryOutput.setCompletionDate(new Date());
                sectorAnnotatedInventoryOutput.setRunStatus(runStatus);
                saveSectorScenarioOutput(sectorAnnotatedInventoryOutput);
            }
        }

        return sectorAnnotatedInventoryOutput;
    }

    private EmfDataset createEECSDetailedMappingDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.EECS_DETAILED_MAPPING_RESULT);
        return creator.addDataset("ds", sectorScenario.getAbbreviation() + "_" + DatasetType.EECS_DETAILED_MAPPING_RESULT, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }

    private EmfDataset createSectorDetailedMappingDataset() throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.SECTOR_DETAILED_MAPPING_RESULT);
        return creator.addDataset("ds", sectorScenario.getAbbreviation() + "_" + DatasetType.SECTOR_DETAILED_MAPPING_RESULT, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                "");
    }

    private EmfDataset createSectorSpecificInventoryDataset(String sector, EmfDataset inventory) throws EmfException {
        //lookup up what the resulting inventory dataset type is...
        String outputInventoryDatasetType = new SectorScenarioInputToOutputDatasetTypeMap().getOutputDatasetType(inventory.getDatasetType().getName());
        DatasetType datasetType = getDatasetType(outputInventoryDatasetType);
        return creator.addDataset(sectorScenario.getAbbreviation() + "_" + inventory.getName() + "_" + sector, inventory, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                inventory.getDescription() + "\n#SECTOR_SCENARIO_SECTOR=" + sector);
    }

    private EmfDataset createSectorAnnotatedInventoryDataset(EmfDataset inventory) throws EmfException {
        //lookup up what the resulting inventory dataset type is...
        String outputInventoryDatasetType = new SectorScenarioInputToOutputDatasetTypeMap().getOutputDatasetType(inventory.getDatasetType().getName());
        DatasetType datasetType = getDatasetType(outputInventoryDatasetType);
        return creator.addDataset(sectorScenario.getAbbreviation() + "_" + inventory.getName(), "ds", inventory, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                inventory.getDescription());
    }

    private EmfDataset createEecsAnnotatedInventoryDataset(EmfDataset inventory) throws EmfException {
        DatasetType datasetType = inventory.getDatasetType();
        return creator.addDataset(sectorScenario.getAbbreviation() + "_" + inventory.getName() + "_eecs_annotated", inventory, 
                datasetType, new VersionedTableFormat(datasetType.getFileFormat(), dbServer.getSqlDataTypes()),
                inventory.getDescription());
    }

    private void populateEECSDetailedMapping(SectorScenarioInventory sectorScenarioInventory, SectorScenarioOutput sectorScenarioOutput) throws EmfException {
        
        EmfDataset inventory = sectorScenarioInventory.getDataset();
        int inventoryVersionNumber = sectorScenarioInventory.getVersion();
        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
        String inventoryTableName = qualifiedEmissionTableName(inventory);
        boolean inventoryHasMactColumn = hasColName("mact", inventory.getDatasetType().getFileFormat());
        boolean inventoryHasNaicsColumn = hasColName("naics", inventory.getDatasetType().getFileFormat());
        boolean inventoryHasPointColumns = hasColName("pointid", inventory.getDatasetType().getFileFormat());

        EmfDataset eecsMappingDataset = sectorScenario.getEecsMapppingDataset();
        int eecsMappingDatasetVersionNumber = sectorScenario.getEecsMapppingDatasetVersion();
        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
        
        cleanMappingDataset(eecsMappingDataset);
//        EmfDataset sectorMappingDataset = sectorScenario.getSectorMapppingDataset();
//        int sectorMappingDatasetVersionNumber = sectorScenario.getSectorMapppingDatasetVersion();
//        Version sectorMappingDatasetVersion = version(sectorMappingDataset.getId(), sectorMappingDatasetVersionNumber);
//        VersionedQuery sectorMappingDatasetVersionedQuery = new VersionedQuery(sectorMappingDatasetVersion);
//        String sectorMappingDatasetTableName = qualifiedEmissionTableName(sectorMappingDataset);

        
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(sectorScenarioOutput.getOutputDataset()) + " (dataset_id, version, fips, "
        + (inventoryHasPointColumns ?
        "plantid, pointid, stackid, segment, plant, "
        : "")
        + "scc, poll, ann_emis, avd_emis, mact, naics, map_mact, map_naics, map_scc, eecs, priority, original_dataset_id, original_record_id, dataset_type) " 
        + "select " + sectorScenarioOutput.getOutputDataset().getId() + " as dataset_id, 0 as version, " 
        + "inv.fips, "
        + (inventoryHasPointColumns ?
        "inv.plantid, inv.pointid, "
        + "inv.stackid, inv.segment, "
        + "inv.plant, "
        : "")
        + "inv.scc, "
        + "inv.poll, "
        + "inv.ann_emis, "
        + "inv.avd_emis, "
        + (inventoryHasMactColumn ?
        "inv.mact, "
        : "null::varchar(6) as mact, ")
        + (inventoryHasNaicsColumn ?
        "inv.naics, "
        : "null::varchar(6) as naics, ")
        + "map_mact, map_naics, "
        + "map_scc, "
        + "tblMatch.eecs, priority, "
        + "inv.dataset_id as original_dataset_id, "
        + "inv.record_id as original_record_id, "
        + "'" + inventory.getDatasetType().getName().replaceAll("'", "''") + "'::varchar(255) as dataset_type "
        + "from " + inventoryTableName + " inv "
        + "left outer join ( "
        + "select inv.record_id,  "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics, " 
        + "eecs_map.scc as map_scc,  "
        + "eecs_map.eecs, eecs_map.priority "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.scc = inv.scc "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is not null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        + (inventoryHasMactColumn ?
        "union  "
        + "select inv.record_id, "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics, " 
        + "eecs_map.scc as map_scc, "
        + "eecs_map.eecs, eecs_map.priority "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.mact = inv.mact "
        + "where eecs_map.mact is not null "
        + "and eecs_map.naics is null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        : "" )
        + (inventoryHasNaicsColumn ?
        "union  "
        + "select inv.record_id,  "
        + "eecs_map.mact as map_mact, eecs_map.naics as map_naics,  "
        + "eecs_map.scc as map_scc,  "
        + "eecs_map.eecs, eecs_map.priority "
        + "from " + inventoryTableName + " inv "
        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
        + "on eecs_map.naics = inv.naics "
        + "where eecs_map.mact is null "
        + "and eecs_map.naics is not null "
        + "and eecs_map.scc is null "
        + "and " + inventoryVersionedQuery.query() + " "
        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
        : "" )
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

    private void populateSectorDetailedMapping(EmfDataset eecsDetailedMappingDataset, SectorScenarioOutput sectorScenarioOutput) throws EmfException {
//        EmfDataset inventory = sectorScenarioInventory.getDataset();
//        int inventoryVersionNumber = sectorScenarioInventory.getVersion();
//        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
//        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
//        String inventoryTableName = qualifiedEmissionTableName(inventory);

//        EmfDataset eecsMappingDataset = sectorScenario.getEecsMapppingDataset();
//        int eecsMappingDatasetVersionNumber = sectorScenario.getEecsMapppingDatasetVersion();
//        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
//        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
//        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
        
        Version eecsDetailedMappingDatasetVersion = version(eecsDetailedMappingDataset.getId(), 0);
        VersionedQuery eecsDetailedMappingDatasetVersionedQuery = new VersionedQuery(eecsDetailedMappingDatasetVersion, "inv");
        String eecsDetailedMappingDatasetTableName = qualifiedEmissionTableName(eecsDetailedMappingDataset);
        createIndexes(emissionTableName(eecsDetailedMappingDataset));
        
        EmfDataset sectorMappingDataset = sectorScenario.getSectorMapppingDataset();
        int sectorMappingDatasetVersionNumber = sectorScenario.getSectorMapppingDatasetVersion();
        Version sectorMappingDatasetVersion = version(sectorMappingDataset.getId(), sectorMappingDatasetVersionNumber);
        VersionedQuery sectorMappingDatasetVersionedQuery = new VersionedQuery(sectorMappingDatasetVersion, "tblsector");
        String sectorMappingDatasetTableName = qualifiedEmissionTableName(sectorMappingDataset);

        cleanMappingDataset(sectorMappingDataset);

        
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(sectorScenarioOutput.getOutputDataset()) + " (dataset_id, version, fips, plantid, pointid, stackid, segment, scc, plant, poll, ann_emis, avd_emis, eecs, sector, priority, original_dataset_id, original_record_id, dataset_type) " 
        + "select " + sectorScenarioOutput.getOutputDataset().getId() + " as dataset_id, 0 as version, " 
        + "inv.fips, "
        + "inv.plantid, inv.pointid, "
        + "inv.stackid, inv.segment, "
        + "inv.scc, inv.plant, "
        + "inv.poll, "
        + "inv.ann_emis, "
        + "inv.avd_emis, "
        + "inv.eecs, tblsector.sector, "
        + "tblsector.priority, "
        + "inv.original_dataset_id, "
        + "inv.original_record_id, "
        + "inv.dataset_type "
        + "from " + eecsDetailedMappingDatasetTableName + " inv "
        + "left outer join "
//        + "select sector, eecs, priority "
//        + "from " + sectorMappingDatasetTableName + " as sector_map "
        + sectorMappingDatasetTableName + " tblsector "
        + "on tblsector.eecs = inv.eecs "
        + "and tblsector.eecs is not null "
        + "and " + sectorMappingDatasetVersionedQuery.query() + " "
        + "where " + eecsDetailedMappingDatasetVersionedQuery.query() + " "
        + "order by inv.record_id, tblsector.sector";

//        String sql = "INSERT INTO " + qualifiedEmissionTableName(sectorScenarioOutput.getOutputDataset()) + " (dataset_id, version, fips, plantid, pointid, stackid, segment, scc, plant, poll, ann_emis, avd_emis, eecs, sector, priority, original_dataset_id, original_record_id) " 
//        + "select " + sectorScenarioOutput.getOutputDataset().getId() + " as dataset_id, 0 as version, " 
//        + "inv.fips, "
//        + "inv.plantid, inv.pointid, "
//        + "inv.stackid, inv.segment, "
//        + "inv.scc, inv.plant, "
//        + "inv.poll, "
//        + "inv.ann_emis, "
//        + "inv.avd_emis, "
//        + "tbleecs.eecs, tblsector.sector, "
//        + "tblsector.priority, "
//        + "inv.dataset_id as original_dataset_id, "
//        + "inv.record_id as original_record_id "
//        + "from " + inventoryTableName + " inv "
//        + "left outer join ( "
//        + "select " + (!shouldDoubleCount ? "distinct on (record_id)" : "") + " "
//        + "record_id, eecs, priority "
//        + "from ( "
//        + "select inv.record_id, "
//        + "eecs_map.eecs, eecs_map.priority "
//        + "from " + inventoryTableName + " inv "
//        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
//        + "on eecs_map.mact = inv.mact "
//        + "where eecs_map.mact is not null "
//        + "and eecs_map.naics is null "
//        + "and eecs_map.scc is null "
//        + "and " + inventoryVersionedQuery.query() + " "
//        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
//        + "union  "
//        + "select inv.record_id, "
//        + "eecs_map.eecs, eecs_map.priority "
//        + "from " + inventoryTableName + " inv "
//        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
//        + "on eecs_map.naics = inv.naics "
//        + "where eecs_map.mact is null "
//        + "and eecs_map.naics is not null "
//        + "and eecs_map.scc is null "
//        + "and " + inventoryVersionedQuery.query() + " "
//        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
//        + "union  "
//        + "select inv.record_id, "
//        + "eecs_map.eecs, eecs_map.priority "
//        + "from " + inventoryTableName + " inv "
//        + "inner join " + eecsMappingDatasetTableName + " eecs_map "
//        + "on eecs_map.scc = inv.scc "
//        + "where eecs_map.mact is null "
//        + "and eecs_map.naics is null "
//        + "and eecs_map.scc is not null "
//        + "and " + inventoryVersionedQuery.query() + " "
//        + "and " + eecsMappingDatasetVersionedQuery.query() + " "
//        + ") tbleecs "
//        + "order by record_id, priority, eecs "
//        + ") tbleecs "
//        + "on tbleecs.record_id = inv.record_id "
//        + "left outer join ( "
//        + "select sector, eecs, priority "
//        + "from " + sectorMappingDatasetTableName + " as sector_map "
//        + "where sector_map.eecs is not null "
//        + "and " + sectorMappingDatasetVersionedQuery.query() + " "
//        + ") tblsector "
//        + "on tblsector.eecs = tbleecs.eecs "
//        + "where " + inventoryVersionedQuery.query() + " ";
//            
        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to " + DatasetType.SECTOR_DETAILED_MAPPING_RESULT + " table" + "\n" + e.getMessage());
        }
    }

    private void createIndexes(String tableName) {
        DataTable dataTable = new DataTable(sectorScenario.getEecsMapppingDataset(), this.datasource);
        tableName = tableName.toLowerCase();
        dataTable.addIndex(tableName, "record_id", true);
        dataTable.addIndex(tableName, "dataset_id", false);
        dataTable.addIndex(tableName, "version", false);
        dataTable.addIndex(tableName, "delete_versions", false);
        dataTable.addIndex(tableName, "eecs", false);
        dataTable.addIndex(tableName, "original_dataset_id", false);
        dataTable.addIndex(tableName, "original_record_id", false);
        dataTable.addIndex(tableName, "priority", false);
        dataTable.analyzeTable(tableName);
    }

    private void populateSectorAnnotatedInventory(EmfDataset sectorDetailedMappingDataset, String[] sectors, SectorScenarioInventory sectorScenarioInventory, SectorScenarioOutput sectorScenarioOutput) throws EmfException {
        
        
        EmfDataset inventory = sectorScenarioInventory.getDataset();
        int inventoryVersionNumber = sectorScenarioInventory.getVersion();
        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
        String inventoryTableName = qualifiedEmissionTableName(inventory);

//        EmfDataset eecsMappingDataset = sectorScenario.getEecsMapppingDataset();
//        int eecsMappingDatasetVersionNumber = sectorScenario.getEecsMapppingDatasetVersion();
//        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
//        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
//        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);

        Version sectorDetailedMappingDatasetVersion = version(sectorDetailedMappingDataset.getId(), 0);
        VersionedQuery sectorDetailedMappingDatasetVersionedQuery = new VersionedQuery(sectorDetailedMappingDatasetVersion, "sector_map");
        String sectorDetailedMappingDatasetTableName = qualifiedEmissionTableName(sectorDetailedMappingDataset);

        EmfDataset sectorAnnotatedInventory = sectorScenarioOutput.getOutputDataset();

        short annotateEecsOption = sectorScenario.getAnnotatingEecsOption();
        
        String selectList = "select distinct on (inv.record_id) " + sectorAnnotatedInventory.getId() + " as dataset_id, '' as delete_versions, 0 as version";
        String columnList = "dataset_id, delete_versions, version";
        Column[] columns = sectorAnnotatedInventory.getDatasetType().getFileFormat().cols();
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("eecs")) {
                //addEECSCol
                if (annotateEecsOption == 1) {
                    selectList += ", sector_map.eecs";
                //useEECSFromInv
                } else if (annotateEecsOption == 2) {
                    selectList += ", inv.eecs";
                //fillMissEECS
                } else if (annotateEecsOption == 3) {
                    selectList += ", coalesce(inv.eecs, sector_map.eecs) as eecs";
                }
//                selectList += ", sector_map.eecs";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("sector")) {
                //addEECSCol
                if (annotateEecsOption == 1) {
                    selectList += ", sector_map.sector";
                //useEECSFromInv
                } else if (annotateEecsOption == 2) {
                    selectList += ", inv.sector";
                //fillMissEECS
                } else if (annotateEecsOption == 3) {
                    selectList += ", case when inv.eecs is not null then inv.sector else sector_map.sector end as sector";
                }
//                    selectList += ", sector_map.sector";
                    columnList += "," + columnName;
            } else {
                selectList += "," + columnName;
                columnList += "," + columnName;
            }
        }
        String sectorList = "";
        for (String sector : sectors) {
            sectorList += (sectorList.length() > 0 ? "," : "") + "'" + sector.replace("'", "''") + "'";
        }
        selectList = selectList.toLowerCase();
        for (Column column : sectorDetailedMappingDataset.getDatasetType().getFileFormat().cols()) {
            String columnName = column.getName().toLowerCase();
            selectList = selectList.replace("," + columnName, ",inv." + columnName);
        }
        //SET work_mem TO '512MB';
        //NOTE:  Still need to  support mobile monthly files
        String sql = "INSERT INTO " + qualifiedEmissionTableName(sectorScenarioOutput.getOutputDataset()) + " (" + columnList + ") " 
        + selectList + " "
        + "from " + inventoryTableName + " inv "
        + "left outer join " + sectorDetailedMappingDatasetTableName + " as sector_map "
        + "on " + sectorDetailedMappingDatasetVersionedQuery.query() + " "
        + "and sector_map.original_record_id = inv.record_id "
        + "and sector_map.original_dataset_id = " + inventory.getId() + " "
        + "and coalesce(sector_map.sector,'') in (" + sectorList + ",'') " //always include missing sectors = ''
        + "where " + inventoryVersionedQuery.query() + " "
        + "order by inv.record_id, sector_map.priority ";
            
        System.out.println(sql);
        try {
            datasource.query().execute(sql);
        } catch (SQLException e) {
            throw new EmfException("Error occured when inserting data to " + sectorScenarioOutput.getOutputDataset().getName() + " table" + "\n" + e.getMessage());
        }
    }

//    private void populateEecsAnnotatedInventory(EmfDataset eecsDetailedMappingDataset, SectorScenarioInventory sectorScenarioInventory, SectorScenarioOutput sectorScenarioOutput) throws EmfException {
//        
//        
//        EmfDataset inventory = sectorScenarioInventory.getDataset();
//        int inventoryVersionNumber = sectorScenarioInventory.getVersion();
//        Version inventoryVersion = version(inventory.getId(), inventoryVersionNumber);
//        VersionedQuery inventoryVersionedQuery = new VersionedQuery(inventoryVersion, "inv");
//        String inventoryTableName = qualifiedEmissionTableName(inventory);
//
////        EmfDataset eecsMappingDataset = sectorScenario.getEecsMapppingDataset();
////        int eecsMappingDatasetVersionNumber = sectorScenario.getEecsMapppingDatasetVersion();
////        Version eecsMappingDatasetVersion = version(eecsMappingDataset.getId(), eecsMappingDatasetVersionNumber);
////        VersionedQuery eecsMappingDatasetVersionedQuery = new VersionedQuery(eecsMappingDatasetVersion, "eecs_map");
////        String eecsMappingDatasetTableName = qualifiedEmissionTableName(eecsMappingDataset);
//
//        Version eecsDetailedMappingDatasetVersion = version(eecsDetailedMappingDataset.getId(), 0);
//        VersionedQuery eecsDetailedMappingDatasetVersionedQuery = new VersionedQuery(eecsDetailedMappingDatasetVersion, "eecs_map");
//        String eecsDetailedMappingDatasetTableName = qualifiedEmissionTableName(eecsDetailedMappingDataset);
//
//        EmfDataset sectorAnnotatedInventory = sectorScenarioOutput.getOutputDataset();
//
//        short annotateEecsOption = sectorScenario.getAnnotatingEecsOption();
//        
//        String selectList = "select distinct on (inv.record_id) " + sectorAnnotatedInventory.getId() + " as dataset_id, '' as delete_versions, 0 as version";
//        String columnList = "dataset_id, delete_versions, version";
//        Column[] columns = sectorAnnotatedInventory.getDatasetType().getFileFormat().cols();
//        for (int i = 0; i < columns.length; i++) {
//            String columnName = columns[i].name();
//            if (columnName.equalsIgnoreCase("eecs")) {
//                //addEECSCol
//                if (annotateEecsOption == 1) {
//                    selectList += ", eecs_map.eecs";
//                //useEECSFromInv
//                } else if (annotateEecsOption == 2) {
//                    selectList += ", inv.eecs";
//                //fillMissEECS
//                } else if (annotateEecsOption == 3) {
//                    selectList += ", coalesce(inv.eecs, eecs_map.eecs) as eecs";
//                }
//                columnList += "," + columnName;
//            } else if (columnName.equalsIgnoreCase("sector")) {
//                //addEECSCol
//                if (annotateEecsOption == 1) {
//                    selectList += ", null::varchar(255) as sector";
//                //useEECSFromInv
//                } else if (annotateEecsOption == 2) {
//                    selectList += ", inv.sector";
//                //fillMissEECS
//                } else if (annotateEecsOption == 3) {
//                    selectList += ", case when inv.eecs is not null then inv.sector else null::varchar(255) end as sector";
//                }
//                columnList += "," + columnName;
//            } else {
//                selectList += "," + columnName;
//                columnList += "," + columnName;
//            }
//        }
//        selectList = selectList.toLowerCase();
//        for (Column column : eecsDetailedMappingDataset.getDatasetType().getFileFormat().cols()) {
//            String columnName = column.getName().toLowerCase();
//            selectList = selectList.replace("," + columnName, ",inv." + columnName);
//        }
//        //SET work_mem TO '512MB';
//        //NOTE:  Still need to  support mobile monthly files
//        String sql = "INSERT INTO " + qualifiedEmissionTableName(sectorScenarioOutput.getOutputDataset()) + " (" + columnList + ") " 
//        + selectList + " "
//        + "from " + inventoryTableName + " inv "
//        + "inner join " + eecsDetailedMappingDatasetTableName + " as eecs_map "
//        + "on " + eecsDetailedMappingDatasetVersionedQuery.query() + " "
//        + "and eecs_map.original_record_id = inv.record_id "
//        + "and eecs_map.original_dataset_id = " + inventory.getId() + " "
//        + "where " + inventoryVersionedQuery.query() + " order by inv.record_id, eecs_map.priority ";
//            
//        System.out.println(sql);
//        try {
//            datasource.query().execute(sql);
//        } catch (SQLException e) {
//            throw new EmfException("Error occured when inserting data to " + sectorScenarioOutput.getOutputDataset().getName() + " table" + "\n" + e.getMessage());
//        }
//    }

    private String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return sectorScenarioDAO.getDistinctSectorListFromDataset(session, dbServer, datasetId, versionNumber);
        } finally {
            session.close();
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

            SectorScenarioInventory[] sectorScenarioInventories = sectorScenario.getInventories();
            
            //create ECCS Detailed Mapping Result, will contain results for all input inventories
            SectorScenarioOutput eecsDetailedMappingResultOutput = createEECSDetailedMappingResultOutput(sectorScenarioInventories);
            
            
            //create Sector Detailed Mapping Result, will contain results for all input inventories
            SectorScenarioOutput sectorDetailedMappingResultOutput = createSectorDetailedMappingResultOutput(eecsDetailedMappingResultOutput.getOutputDataset(), sectorScenarioInventories);

            //see if the scenario has sectors specified, if not then lets auto populate what was found during the above
            //analysis

            if (sectorScenario.getSectors().length == 0) {
                String[] sectors = getDistinctSectorListFromDataset(sectorDetailedMappingResultOutput.getOutputDataset().getId(), 0);
                sectorScenario.setSectors(sectors);
                saveSectorScenario(sectorScenario);
            }
            
            for (int i = 0; i < sectorScenarioInventories.length; i++) {
                try {
                    
//                    //create EECS Annotated Inventory
//                    if (sectorScenario.getAnnotateInventoryWithEECS()) {
//                        createEecsAnnotatedInventoryOutput(eecsDetailedMappingResultOutput.getOutputDataset(), sectorScenarioInventories[i]);
//                    }
                    
                    Short createInventoryMethod = SectorScenario.CREATE_SINGLE_INVENTORY;//sectorScenario.getCreateInventoryMethod();
                    if (createInventoryMethod == SectorScenario.CREATE_SINGLE_INVENTORY) {
                        createSectorAnnotatedInventoryOutput(sectorDetailedMappingResultOutput.getOutputDataset(), sectorScenario.getSectors(), sectorScenarioInventories[i]);
                    } else if (createInventoryMethod == SectorScenario.CREATE_N_SECTOR_INVENTORIES) {
                        for (String sector : sectorScenario.getSectors()) {
                            createSectorSpecificInventoryOutput(sectorDetailedMappingResultOutput.getOutputDataset(), sector, sectorScenarioInventories[i]);
                        }
                    }

                    recordCount = 0; //loader.getRecordCount();
                    status = "Completed.";
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Failed. Error processing inventory: " + sectorScenarioInventories[i].getDataset().getName() + ". " + e.getMessage();
//                    setStatus(status);
                } finally {

                    //see if there was an error, if so, make sure and propogate to the calling method.
                    if (status.startsWith("Failed"))
                        throw new EmfException(status);
                            
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
                        status = "Cancelled. Sector scenario run was cancelled: " + sectorScenario.getName();
                        setStatus(status);
                        return;
//                        throw new EmfException("Strategy run was cancelled.");
                    }
                    //
                }
            }

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
        for (SectorScenarioInventory sectorScenarioInventory : sectorScenario.getInventories()) {
            //make sure inventory has indexes created...
            makeSureInventoryDatasetHaveIndexes(sectorScenarioInventory);
        }
// NOTE Auto-generated method stub
        
    }

    protected void deleteStrategyOutputs() throws EmfException {
        //get rid of strategy results...
        if (true){
            Session session = sessionFactory.getSession();
            try {
                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
                //first get the datasets to delete
                EmfDataset[] datasets = sectorScenarioDAO.getOutputDatasets(sectorScenario.getId(), session);
                if (datasets != null) {
                    for (EmfDataset dataset : datasets) {
                        if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                            setStatus("The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.");
                        } else {
                            dsList.add(dataset);
                        }
                    }
                }
//                EmfDataset[] dsList = controlStrategyDAO.getResultDatasets(sectorScenario.getId(), session);
                //get rid of old strategy results...
                removeSectorScenarioOutputs();
                //delete and purge datasets
                if (dsList != null && dsList.size() > 0){
                    sectorScenarioDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
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

    public void makeSureInventoryDatasetHaveIndexes(SectorScenarioInventory sectorScenarioInventory) {
        String query = "SELECT public.create_orl_table_indexes('" + emissionTableName(sectorScenarioInventory.getDataset()).toLowerCase() + "');analyze " + qualifiedEmissionTableName(sectorScenarioInventory.getDataset()).toLowerCase() + ";";
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
            return sectorScenarioDAO.getSectorScenarioRunStatus(sectorScenario.getId(), session).equals("Cancelled");
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

//    private SectorScenarioOutput createStrategyCountySummaryResult() throws EmfException {
//        SectorScenarioOutput result = new SectorScenarioOutput();
//        result.setSectorScenarioId(sectorScenario.getId());
//        EmfDataset summaryResultDataset = createCountySummaryDataset();
//        
//        result.setOutputDataset(summaryResultDataset);
//        
//        result.setType(getSectorScenarioOutputType(SectorScenarioOutputType.detailedEECSMapping));
//        result.setStartDate(new Date());
//        result.setRunStatus("Start processing " + SectorScenarioOutputType.detailedEECSMapping);
//
//        //persist result
//        saveSectorScenarioSummaryOutput(result);
//        return result;
//    }

    protected SectorScenarioOutputType getSectorScenarioOutputType(String name) throws EmfException {
        SectorScenarioOutputType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = sectorScenarioDAO.getSectorScenarioOutputType(name, session);
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

    protected SectorScenarioOutput[] getSectorScenarioOutputs() {
        SectorScenarioOutput[] results = new SectorScenarioOutput[] {};
        Session session = sessionFactory.getSession();
        try {
            results = sectorScenarioDAO.getSectorScenarioOutputs(sectorScenario.getId(), session).toArray(new SectorScenarioOutput[0]);
        } finally {
            session.close();
        }
        return results;
    }

//    private String summaryResultDatasetDescription(String datasetTypeName) {
//        return "#" + datasetTypeName + " result\n" + 
//            "#Implements control strategy: " + sectorScenario.getName() + "\n#";
//    }

    protected void saveSectorScenarioOutput(SectorScenarioOutput sectorScenarioOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            sectorScenarioDAO.updateSectorScenarioOutput(sectorScenarioOutput, session);
//          runQASteps(sectorScenarioOutput);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveSectorScenario(SectorScenario sectorScenario) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            sectorScenarioDAO.update(sectorScenario, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save sector scenario: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void saveSectorScenarioSummaryOutput(SectorScenarioOutput sectorScenarioOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            sectorScenarioDAO.updateSectorScenarioOutput(sectorScenarioOutput, session);
        } catch (Exception e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    protected void updateOutputDatasetVersionRecordCount(SectorScenarioOutput sectorScenarioOutput) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dao = new DatasetDAO();
        
        try {
            EmfDataset result = sectorScenarioOutput.getOutputDataset();
            
            if (result != null) {
                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());
                
                if (version != null) {
//                    version.setCreator(user);
                    updateVersion(result, version, dbServer, session, dao);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Cannot update result datasets (strategy id: " + sectorScenarioOutput.getSectorScenarioId() + "). " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session, DatasetDAO dao) throws Exception {
//        creator.updateVersionZeroRecordCount(dataset);

        version = dao.obtainLockOnVersion(user, version.getId(), session);
        version.setNumberRecords((int)dao.getDatasetRecordsNumber(dbServer, session, dataset, version));
        dao.updateVersionNReleaseLock(version, session);
    }

    private void removeSectorScenarioOutputs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            sectorScenarioDAO.removeSectorScenarioResults(sectorScenario.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

//    private void removeSectorScenarioOutput(int resultId) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            controlStrategyDAO.removeSectorScenarioOutput(sectorScenario.getId(), resultId, session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not remove previous control strategy result(s)");
//        } finally {
//            session.close();
//        }
//    }

    public SectorScenario getSectorScenario() {
        return sectorScenario;
    }

    protected void runQASteps(SectorScenarioOutput sectorScenarioOutput) {
//        EmfDataset resultDataset = (EmfDataset)sectorScenarioOutput.getDetailedResultDataset();
        if (recordCount > 0) {
//            runSummaryQASteps(resultDataset, 0);
        }
//        excuteSetAndRunQASteps(inputDataset, sectorScenario.getDatasetVersion());
    }

    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
        //11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step templates...
        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
        if (qaStepTemplates != null) {
            String[] qaStepTemplateNames = new String[qaStepTemplates.length];
            for (int i = 0; i < qaStepTemplates.length; i++) qaStepTemplateNames[i] = qaStepTemplates[i].getName();
            qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, sectorScenario.getExportDirectory());
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

    protected void addStatus(SectorScenarioInventory sectorScenarioInventory) {
        setStatus("Completed processing sector scenario inventory: " 
                + sectorScenarioInventory.getDataset().getName() 
                + ".");
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("SectorScenario");
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
            rs = statement.executeQuery(sql);
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

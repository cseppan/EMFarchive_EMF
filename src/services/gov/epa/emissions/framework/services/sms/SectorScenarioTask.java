package gov.epa.emissions.framework.services.sms;

//import gov.epa.emissions.commons.data.Dataset;
//import gov.epa.emissions.commons.data.DatasetType;
//import gov.epa.emissions.commons.data.InternalSource;
//import gov.epa.emissions.commons.data.QAStepTemplate;
//import gov.epa.emissions.commons.db.Datasource;
//import gov.epa.emissions.commons.db.DbServer;
//import gov.epa.emissions.commons.db.version.Version;
//import gov.epa.emissions.commons.db.version.Versions;
//import gov.epa.emissions.commons.io.VersionedQuery;
//import gov.epa.emissions.commons.security.User;
//import gov.epa.emissions.framework.client.meta.keywords.Keywords;
//import gov.epa.emissions.framework.services.DbServerFactory;
//import gov.epa.emissions.framework.services.EmfException;
//import gov.epa.emissions.framework.services.QAStepTask;
//import gov.epa.emissions.framework.services.basic.DateUtil;
//import gov.epa.emissions.framework.services.basic.Status;
//import gov.epa.emissions.framework.services.basic.StatusDAO;
//import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
//import gov.epa.emissions.framework.services.cost.analysis.common.StrategyCountySummaryTableFormat;
//import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLoader;
//import gov.epa.emissions.framework.services.cost.analysis.common.StrategyMeasureSummaryTableFormat;
//import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
//import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
//import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
//import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
//import gov.epa.emissions.framework.services.data.DatasetDAO;
//import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
//import gov.epa.emissions.framework.services.data.EmfDataset;
//import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.hibernate.Session;

public class SectorScenarioTask {

//    protected SectorScenario sectorScenario;
//
//    protected Datasource datasource;
//
//    protected HibernateSessionFactory sessionFactory;
//
//    protected DbServerFactory dbServerFactory;
//
//    protected DbServer dbServer;
//
//    protected User user;
//    
//    protected int recordCount;
//    
//    protected int controlStrategyInputDatasetCount;
//    
//    private StatusDAO statusDAO;
//    
//    protected SectorScenarioDAO sectorScenarioDAO;
//    
//    protected DatasetCreator creator;
//    
//    private Keywords keywords;
//
////    private TableFormat tableFormat;
//    
//    protected List<SectorScenarioOutput> strategyResultList;
//
//    public SectorScenarioTask(SectorScenario sectorScenario, User user, 
//            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
//        this.sectorScenario = sectorScenario;
//        this.controlStrategyInputDatasetCount = sectorScenario.getInventories().length;
//        this.dbServerFactory = dbServerFactory;
//        this.dbServer = dbServerFactory.getDbServer();
//        this.datasource = dbServer.getEmissionsDatasource();
//        this.sessionFactory = sessionFactory;
//        this.user = user;
//        this.statusDAO = new StatusDAO(sessionFactory);
//        this.sectorScenarioDAO = new SectorScenarioDAO(dbServerFactory, sessionFactory);
//        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
//        this.creator = new DatasetCreator(null, user, 
//                sessionFactory, dbServerFactory,
//                datasource, keywords);
//        this.strategyResultList = new ArrayList<SectorScenarioOutput>();
//        //setup the strategy run
//        setup();
//    }
//
//    private void setup() {
//        //
//    }
//    
//    protected SectorScenarioOutput createStrategyResult(EmfDataset inputDataset, int inputDatasetVersion) throws EmfException {
//        SectorScenarioOutput result = new SectorScenarioOutput();
//        result.setSectorScenarioId(sectorScenario.getId());
//        result.setInventoryDataset(inputDataset);
//        result.setInventoryDatasetVersion(inputDatasetVersion);
//        
//        result.setType(null);
//        result.setStartDate(new Date());
//        result.setRunStatus("Start processing dataset");
//
//        //persist result
//        saveSectorScenarioOutput(result);
//        return result;
//    }
//    
//    public SectorScenarioOutput loadStrategyResult(SectorScenarioInventory controlStrategyInputDataset) throws Exception {
//        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
//        //make sure inventory has indexes created...
//        makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset);
//        //make sure inventory has the target pollutant, if not show a warning message
//        //reset counters
//        recordCount = 0;
//        
//        //setup result
//        ControlStrategyResult detailedResult = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
//
//        runStrategy(controlStrategyInputDataset, detailedResult);
//        
//        //create strategy messages result
//        strategyMessagesResult = createStrategyMessagesResult(inputDataset, controlStrategyInputDataset.getVersion());
//        populateStrategyMessagesDataset(controlStrategyInputDataset, strategyMessagesResult, detailedResult);
//        setResultCount(strategyMessagesResult);
//        
//        //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
//        //is no point and keeping it around.
//        if (strategyMessagesResult.getRecordCount() == 0) {
//            deleteStrategyMessageResult(strategyMessagesResult);
//        } else {
//            strategyMessagesResult.setCompletionTime(new Date());
//            strategyMessagesResult.setRunStatus("Completed.");
//            saveControlStrategyResult(strategyMessagesResult);
//        }
//
//        //do this after updating the previous result, else it will override it...
//        //still need to set the record count...
//        //still need to calculate the total cost and reduction...
//        setResultTotalCostTotalReductionAndCount(detailedResult);
//
//
//        return detailedResult;
//    }
//
//    private void runStrategy(SectorScenarioInventory controlStrategyInputDataset, SectorScenarioOutput controlStrategyResult) throws EmfException {
//        String query = "";
//        query = "SELECT public.run_max_emis_red_strategy("  + sectorScenario.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
////        System.out.println(System.currentTimeMillis() + " " + query);
//        try {
//            datasource.query().execute(query);
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            //3
//        }
//    }
//
//    public void run(StrategyLoader loader) throws EmfException {
//        
//        //get rid of strategy results
//        deleteStrategyResults();
//
//        //run any pre processes
//        try {
//            beforeRun();
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        } finally {
//            //
//        }
//
//        String status = "";
//        try {
//            //process/load each input dataset
//            SectorScenarioInventory[] controlStrategyInputDatasets = sectorScenario.getInventories();
//            
//            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
//                SectorScenarioOutput result = null;
//                try {
//                    result = loadStrategyResult(controlStrategyInputDatasets[i]);
//                    recordCount = loader.getRecordCount();
//                    status = "Completed.";
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    status = "Failed. Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". " + e.getMessage();
//                    setStatus(status);
//                } finally {
//                    if (result != null) {
//                        result.setCompletionDate(new Date());
//                        result.setRunStatus(status);
//                        saveSectorScenarioOutput(result);
//                        strategyResultList.add(result);
//                        addStatus(controlStrategyInputDatasets[i]);
//                    }
//                    //see if there was an error, if so, make sure and propogate to the calling method.
//                    if (status.startsWith("Failed"))
//                        throw new EmfException(status);
//                            
//                    //make sure somebody hasn't cancelled this run.
//                    if (isRunStatusCancelled()) {
//                        status = "Cancelled. Strategy run was cancelled: " + sectorScenario.getName();
//                        setStatus(status);
//                        return;
////                        throw new EmfException("Strategy run was cancelled.");
//                    }
//                    //
//                }
//            }
//
//            //now create the measure summary result based on the results from the strategy run...
////            generateStrategyMeasureSummaryResult();
//
////            //now create the county summary result based on the results from the strategy run...
////            generateStrategyCountySummaryResult();
//
//        } catch (Exception e) {
//            status = "Failed. Error processing input dataset";
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        } finally {
//            //run any post processes
//            try {
//                afterRun();
//                updateVersionInfo();
//            } catch (Exception e) {
//                status = "Failed. Error processing input dataset";
//                e.printStackTrace();
//                throw new EmfException(e.getMessage());
//            } finally {
//                loader.disconnectDbServer();
//                disconnectDbServer();
//            }
//        }
//    }
//
//    private void afterRun() {
//        // NOTE Auto-generated method stub
//        
//    }
//
//    private void beforeRun() {
//        // NOTE Auto-generated method stub
//        
//    }
//
//    protected void updateVersionInfo() throws EmfException {
//        SectorScenarioOutput[] results = getSectorScenarioOutputs();
//        
//        for (SectorScenarioOutput result : results)
//            updateResultDataset(result);
//    }
//    
//    protected void deleteStrategyResults() throws EmfException {
//        //get rid of strategy results...
//        if (false){
//            Session session = sessionFactory.getSession();
//            try {
//                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
//                //first get the datasets to delete
//                EmfDataset[] datasets = sectorScenarioDAO.getResultDatasets(sectorScenario.getId(), session);
//                if (datasets != null) {
//                    for (EmfDataset dataset : datasets) {
//                        if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
//                            setStatus("The control strategy result dataset, " + dataset.getName() + ", will not be deleted since you are not the creator of, this control strategy result will not be deleted.");
//                        } else {
//                            dsList.add(dataset);
//                        }
//                    }
//                }
////                EmfDataset[] dsList = controlStrategyDAO.getResultDatasets(sectorScenario.getId(), session);
//                //get rid of old strategy results...
//                removeSectorScenarioOutputs();
//                //delete and purge datasets
//                if (dsList != null && dsList.size() > 0){
//                    sectorScenarioDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
//                }
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//                throw new EmfException("Could not remove Control Strategies results.");
//            } finally {
//                session.close();
//            }
//        }
//    }
//    
////    protected void deleteStrategyResults(SectorScenarioOutput[] results) throws EmfException {
////        //get rid of strategy results...
////        if (sectorScenario.getDeleteResults()){
////            Session session = sessionFactory.getSession();
////            try {
////                List<EmfDataset> dsList = new ArrayList<EmfDataset>();
////                //first ge tthe datasets to delete
////                if (results != null) {
////                    for (SectorScenarioOutput result : results) {
////                        for (EmfDataset dataset : controlStrategyDAO.getResultDatasets(sectorScenario.getId(), result.getId(), session)) {
////                            if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
////                                setStatus("Since you are not the creator of " + dataset.getName() + ", this control strategy result will not be deleted.");
////                            } else {
////                                dsList.add(dataset);
////                            }
////                        }
////                    }
////                    for (SectorScenarioOutput result : results) {
////                        //get rid of old strategy results...
////                        removeSectorScenarioOutput(result.getId());
////                    }
////                }
////                //delete and purge datasets
////                if (dsList != null && dsList.size() > 0) {
////                    controlStrategyDAO.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
////                }
////            } catch (RuntimeException e) {
////                e.printStackTrace();
////                throw new EmfException("Could not remove Control Strategies results.");
////            } finally {
////                session.close();
////            }
////        }
////    }
//    
//    protected void generateStrategyCountySummaryResult(SectorScenarioOutput[] results) throws EmfException {
//        //now create the summary detailed result based on the results from the strategy run...
//        if (results.length > 0) {
//            //create dataset and strategy region summary result 
//            SectorScenarioOutput countySummaryResult = createStrategyCountySummaryResult();
//            //now populate the summary result with data...
////            populateStrategyCountySummaryDataset(results, countySummaryResult);
//            
//            //finalize the result, update completion time and run status...
//            countySummaryResult.setCompletionDate(new Date());
//            countySummaryResult.setRunStatus("Completed.");
//            getRecordCount(countySummaryResult.getOutputDataset());
//            saveSectorScenarioSummaryOutput(countySummaryResult);
////            runSummaryQASteps((EmfDataset)countySummaryResult.getDetailedResultDataset(), 0);
//        }
//    }
//
////    private void populateStrategyMeasureSummaryDataset(SectorScenarioOutput[] results, SectorScenarioOutput summaryResult) throws EmfException {
////        if (results.length > 0) {
////            
////            String sql = "INSERT INTO " + qualifiedEmissionTableName(summaryResult.getDetailedResultDataset()) + " (dataset_id, version, sector, fips, scc, poll, Control_Measure_Abbreviation, Control_Measure, Control_Technology, source_group, avg_ann_cost_per_ton, Annual_Cost, input_emis, Emis_Reduction, Pct_Red) " 
////            + "select " + summaryResult.getDetailedResultDataset().getId() + ", 0, "
////            + "summary.sector, "
////            + "summary.fips, "
////            + "summary.scc, "
////            + "summary.poll, "
////            + "cm.abbreviation, "
////            + "cm.name, "
////            + "ct.name as Control_Technology, "
////            + "sg.name as source_group, "
////            + "case when sum(summary.Emis_Reduction) <> 0 then sum(summary.Annual_Cost) / sum(summary.Emis_Reduction) else null::double precision end as avg_cost_per_ton, " 
////            + "sum(summary.Annual_Cost) as Annual_Cost, "
////            + "sum(summary.input_emis) as input_emis, " 
////            + "sum(summary.Emis_Reduction) as Emis_Reduction, " 
////            + "case when sum(summary.input_emis) <> 0 then (sum(summary.Emis_Reduction)) / sum(summary.input_emis) * 100.0 else null::double precision end as Pct_Red " 
////            + "from (";
////            int count = 0;
////            for (int i = 0; i < results.length; i++) {
////                if (results[i].getDetailedResultDataset() != null) {
////                    String tableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
////                    sql += (count > 0 ? " union all " : "") 
////                        + "select e.sector, e.fips, e.scc, e.poll, e.cm_id, sum(e.Annual_Cost) as Annual_Cost, sum(e.Emis_Reduction) as Emis_Reduction, "
////                        + "sum(e.input_emis) as input_emis "
////                        + "from " + tableName + " e "
////                        + "group by e.sector, e.fips, e.scc, e.poll, e.cm_id ";
////                    ++count;
////                }
////            }
////
////
////
////
//////            SectorScenarioInventory[] inventories = sectorScenario.getSectorScenarioInventorys();
//////            int count = 0;
//////            
//////            EmfDataset mergedInventory = null;
//////            //we need to create a controlled inventory for each inventory, except the merged inventory
//////            for (int i = 0; i < inventories.length; i++) {
//////                if (inventories[i].getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
//////                    mergedInventory = inventories[i].getInputDataset();
//////                    break;
//////                }
//////            }
//////            //if merged inventory, then there is only one result
//////            if (sectorScenario.getMergeInventories() && mergedInventory != null) {
//////                for (int i = 0; i < inventories.length; i++) {
////////                      EmfDataset inventory = inventories[i].getInputDataset();
//////                  SectorScenarioInventory inventory = inventories[i];
//////                  if (!inventory.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
//////
//////                      for (int j = 0; j < results.length; j++) {
//////                          if (results[j].getDetailedResultDataset() != null 
//////                              && results[j].getInputDataset() != null) {
//////                              String detailedresultTableName = qualifiedEmissionTableName(results[j].getDetailedResultDataset());
//////                              String inventoryTableName = qualifiedEmissionTableName(inventory.getInputDataset());
//////                              String sector = inventory.getInputDataset().getSectors().length > 0 ? inventory.getInputDataset().getSectors()[0].getName() : "";
//////                              Version v = version(inventory);
//////                              VersionedQuery versionedQuery = new VersionedQuery(v);
//////                              int month = inventory.getInputDataset().applicableMonth();
//////                              int noOfDaysInMonth = 31;
//////                              if (month != -1) {
//////                                  noOfDaysInMonth = getDaysInMonth(month);
//////                              }
//////                              String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
//////                              sql += (count > 0 ? " union all " : "") 
//////                                  + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
//////                                  + "i.fips, "
//////                                  + "i.poll, "
//////                                  + "i.scc, "
//////                                  + "e.cm_id, "
//////                                  + "sum(" + sqlAnnEmis + ") as Input_Emis, "
//////                                  + "sum(e.Emis_Reduction) as Emis_Reduction, "
//////                                  + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
//////                                  + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
//////                                  + "sum(e.Annual_Cost) as Annual_Cost, "
//////                                  + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
//////                                  + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
//////                                  + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
//////                                  + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
//////                                  + "from " + inventoryTableName + " i "
//////                                  + "left outer join " + detailedresultTableName + " e "
//////                                  + "on e.source_id = i.record_id "
//////                                  + "and e.ORIGINAL_DATASET_ID = " + inventory.getInputDataset().getId() + " "
//////                                  + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
//////                                  + "group by i.fips, i.poll, i.scc, e.cm_id ";
//////                              ++count;
//////                              }
//////                          }
//////                      }
//////                }
//////            //not a merged inventory, then there could be multiple results
//////            } else {
////////case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + " else 0.0 end as ceff
//////                for (int i = 0; i < results.length; i++) {
//////                    if (results[i].getDetailedResultDataset() != null && results[i].getInputDataset() != null) {
//////                        String detailedresultTableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
//////                        String inventoryTableName = qualifiedEmissionTableName(results[i].getInputDataset());
//////                        String sector = results[i].getInputDataset().getSectors().length > 0 ? results[i].getInputDataset().getSectors()[0].getName() : "";
//////                        Version v = version(results[i].getInputDataset().getId(), results[i].getInputDatasetVersion());
//////                        VersionedQuery versionedQuery = new VersionedQuery(v);
//////                        int month = results[i].getInputDataset().applicableMonth();
//////                        int noOfDaysInMonth = 31;
//////                        if (month != -1) {
//////                            noOfDaysInMonth = getDaysInMonth(month);
//////                        }
//////                        String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
//////                        sql += (count > 0 ? " union all " : "") 
//////                            + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
//////                            + "i.fips, "
//////                            + "i.poll, "
//////                            + "i.scc, "
//////                            + "e.cm_id, "
//////                            + "sum(" + sqlAnnEmis + ") as Input_Emis, "
//////                            + "sum(e.Emis_Reduction) as Emis_Reduction, "
//////                            + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
//////                            + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
//////                            + "sum(e.Annual_Cost) as Annual_Cost, "
//////                            + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
//////                            + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
//////                            + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
//////                            + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
//////                            + "from " + inventoryTableName + " i "
//////                            + "left outer join " + detailedresultTableName + " e "
//////                            + "on e.source_id = i.record_id "
//////                            + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
//////                            + "group by i.fips, i.poll, i.scc, e.cm_id";
//////                        ++count;
//////                    }
//////                }
//////            }
//////            
////            
////            sql += ") summary "
////                + "left outer join emf.control_measures cm "
////                + "on cm.id = summary.cm_id "
////                + "left outer join emf.control_technologies ct "
////                + "on ct.id = cm.control_technology "
////                + "left outer join emf.source_groups sg "
////                + "on sg.id = cm.source_group "
////                + "group by summary.sector, summary.fips, summary.scc, summary.poll, cm.abbreviation, cm.name, ct.name, sg.name "
////                + "order by summary.sector, summary.fips, summary.scc, summary.poll, cm.abbreviation, cm.name, ct.name, sg.name";
////            System.out.println(sql);
////            try {
////                datasource.query().execute(sql);
////            } catch (SQLException e) {
////                throw new EmfException("Error occured when inserting data to strategy measure summary table" + "\n" + e.getMessage());
////            }
////        }
////    }
////
////    private void populateStrategyCountySummaryDataset(SectorScenarioOutput[] results, SectorScenarioOutput countySummaryResult) throws EmfException {
////        if (results.length > 0) {
////            SectorScenarioInventory[] inventories = sectorScenario.getSectorScenarioInventorys();
////
////            //SET work_mem TO '512MB';
////            //NOTE:  Still need to  support mobile monthly files
////            String sql = "INSERT INTO " + qualifiedEmissionTableName(countySummaryResult.getDetailedResultDataset()) + " (dataset_id, version, sector, fips, poll, Input_Emis, Emis_Reduction, Remaining_Emis, Pct_Red, Annual_Cost, Annual_Oper_Maint_Cost, Annualized_Capital_Cost, Total_Capital_Cost, Avg_Ann_Cost_per_Ton) " 
////            + "select " + countySummaryResult.getDetailedResultDataset().getId() + ", 0, "
////                      + "sector, "
////                      + "fips, "
////                      + "poll, "
////                      + "sum(summary.Input_Emis) as Input_Emis, "
////                      + "sum(summary.Emis_Reduction) as Emis_Reduction, "
////                      + "sum(summary.Remaining_Emis) as Remaining_Emis, "
////                      + "case when sum(summary.Input_Emis) <> 0 then sum(summary.Emis_Reduction) / sum(summary.Input_Emis) * 100.0 else null::double precision end as Pct_Red, "
////                      + "sum(summary.Annual_Cost) as Annual_Cost, "
////                      + "sum(summary.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
////                      + "sum(summary.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
////                      + "sum(summary.Total_Capital_Cost) as Total_Capital_Cost, "
////                      + "case when sum(summary.Emis_Reduction) <> 0 then sum(summary.Annual_Cost) / sum(summary.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton " 
////            + "from (";
////            int count = 0;
////            
////            EmfDataset mergedInventory = null;
////            //we need to create a controlled inventory for each invnentory, except the merged inventory
////            for (int i = 0; i < inventories.length; i++) {
////                if (inventories[i].getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
////                    mergedInventory = inventories[i].getInputDataset();
////                    break;
////                }
////            }
////            //if merged inventory, then there is only one result
////            if (sectorScenario.getMergeInventories() && mergedInventory != null) {
////                for (int i = 0; i < inventories.length; i++) {
//////                      EmfDataset inventory = inventories[i].getInputDataset();
////                  SectorScenarioInventory inventory = inventories[i];
////                  if (!inventory.getInputDataset().getDatasetType().getName().equals(DatasetType.orlMergedInventory)) {
////
////                      for (int j = 0; j < results.length; j++) {
////                          if (results[j].getDetailedResultDataset() != null 
////                              && results[j].getInputDataset() != null) {
////                              String detailedresultTableName = qualifiedEmissionTableName(results[j].getDetailedResultDataset());
////                              String inventoryTableName = qualifiedEmissionTableName(inventory.getInputDataset());
////                              String sector = inventory.getInputDataset().getSectors().length > 0 ? inventory.getInputDataset().getSectors()[0].getName() : "";
////                              Version v = version(inventory);
////                              VersionedQuery versionedQuery = new VersionedQuery(v);
////                              int month = inventory.getInputDataset().applicableMonth();
////                              int noOfDaysInMonth = 31;
////                              if (month != -1) {
////                                  noOfDaysInMonth = getDaysInMonth(month);
////                              }
////                              String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
////                              sql += (count > 0 ? " union all " : "") 
////                                  + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
////                                  + "i.fips, "
////                                  + "i.poll, "
////                                  + "sum(" + sqlAnnEmis + ") as Input_Emis, "
////                                  + "sum(e.Emis_Reduction) as Emis_Reduction, "
////                                  + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
////                                  + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
////                                  + "sum(e.Annual_Cost) as Annual_Cost, "
////                                  + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
////                                  + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
////                                  + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
////                                  + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
////                                  + "from " + inventoryTableName + " i "
////                                  + "left outer join " + detailedresultTableName + " e "
////                                  + "on e.source_id = i.record_id "
////                                  + "and e.ORIGINAL_DATASET_ID = " + inventory.getInputDataset().getId() + " "
////                                  + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
////                                  + "group by i.fips, i.poll ";
////                              ++count;
////                              }
////                          }
////                      }
////                }
////            //not a merged inventory, then there could be multiple results
////            } else {
//////case when " + (month != -1 ? "coalesce(avd_emis, ann_emis)" : "ann_emis") + " <> 0 then " + (month != -1 ? "b.final_emissions / " + noOfDaysInMonth + " / coalesce(avd_emis, ann_emis)" : "b.final_emissions / ann_emis") + " else 0.0 end as ceff
////                for (int i = 0; i < results.length; i++) {
////                    if (results[i].getDetailedResultDataset() != null && results[i].getInputDataset() != null) {
////                        String detailedresultTableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
////                        String inventoryTableName = qualifiedEmissionTableName(results[i].getInputDataset());
////                        String sector = results[i].getInputDataset().getSectors().length > 0 ? results[i].getInputDataset().getSectors()[0].getName() : "";
////                        Version v = version(results[i].getInputDataset().getId(), results[i].getInputDatasetVersion());
////                        VersionedQuery versionedQuery = new VersionedQuery(v);
////                        int month = results[i].getInputDataset().applicableMonth();
////                        int noOfDaysInMonth = 31;
////                        if (month != -1) {
////                            noOfDaysInMonth = getDaysInMonth(month);
////                        }
////                        String sqlAnnEmis = (month != -1 ? "coalesce(" + noOfDaysInMonth + " * i.avd_emis, i.ann_emis)" : "i.ann_emis");
////                        sql += (count > 0 ? " union all " : "") 
////                            + "select '" + sector.replace("'", "''") + "'::character varying(64) as sector, "
////                            + "i.fips, "
////                            + "i.poll, "
////                            + "sum(" + sqlAnnEmis + ") as Input_Emis, "
////                            + "sum(e.Emis_Reduction) as Emis_Reduction, "
////                            + "sum(" + sqlAnnEmis + ") - sum(e.Emis_Reduction) as Remaining_Emis, "
////                            + "case when sum(" + sqlAnnEmis + ") <> 0 then sum(e.Emis_Reduction) / sum(" + sqlAnnEmis + ") * 100.0 else null::double precision end as Pct_Red, "
////                            + "sum(e.Annual_Cost) as Annual_Cost, "
////                            + "sum(e.Annual_Oper_Maint_Cost) as Annual_Oper_Maint_Cost, "
////                            + "sum(e.Annualized_Capital_Cost) as Annualized_Capital_Cost, "
////                            + "sum(e.Total_Capital_Cost) as Total_Capital_Cost, "
////                            + "case when sum(e.Emis_Reduction) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null::double precision end as Avg_Ann_Cost_per_Ton "
////                            + "from " + inventoryTableName + " i "
////                            + "left outer join " + detailedresultTableName + " e "
////                            + "on e.source_id = i.record_id "
////                            + "where " + versionedQuery.query().replaceAll("delete_versions ", "i.delete_versions ").replaceAll("version ", "i.version ").replaceAll("dataset_id", "i.dataset_id") + " "
////                            + "group by i.fips, i.poll ";
////                        ++count;
////                    }
////                }
////            }
////            sql += ") summary "
////                + "group by sector, fips, poll ";
////            sql += "order by sector, fips, poll ";
////            
////            System.out.println(sql);
////            try {
////                datasource.query().execute(sql);
////            } catch (SQLException e) {
////                throw new EmfException("Error occured when inserting data to strategy county summary table" + "\n" + e.getMessage());
////            }
////        }
////    }
//
//    protected int getDaysInMonth(int year, int month) {
//        return month != - 1 ? DateUtil.daysInZeroBasedMonth(year, month) : 31;
//    }
//
//    public void makeSureInventoryDatasetHasIndexes(SectorScenarioInventory controlStrategyInputDataset) {
//        String query = "SELECT public.create_orl_table_indexes('" + emissionTableName(controlStrategyInputDataset.getInputDataset()).toLowerCase() + "');analyze " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()).toLowerCase() + ";";
//        try {
//            datasource.query().execute(query);
//        } catch (SQLException e) {
//            //e.printStackTrace();
//            //supress all errors, the indexes might already be on the table...
//        } finally {
//            //
//        }
//    }
//
//    protected boolean isRunStatusCancelled() throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            return sectorScenarioDAO.getSectorScenarioRunStatus(sectorScenario.getId(), session).equals("Cancelled");
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not check if strategy run was cancelled.");
//        } finally {
//            session.close();
//        }
//    }
//    
//    protected int getRecordCount(EmfDataset dataset) throws EmfException {
//        String query = "SELECT count(1) as record_count "
//            + " FROM " + qualifiedEmissionTableName(dataset);
//        ResultSet rs = null;
//        Statement statement = null;
//        int recordCount = 0;
//        try {
//            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            rs = statement.executeQuery(query);
//            while (rs.next()) {
//                recordCount = rs.getInt(1);
//            }
//            rs.close();
//            rs = null;
//            statement.close();
//            statement = null;
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            if (rs != null) {
//                try { rs.close(); } catch (SQLException e) { /**/ }
//                rs = null;
//            }
//            if (statement != null) {
//                try { statement.close(); } catch (SQLException e) { /**/ }
//                statement = null;
//            }
//        }
//        return recordCount;
//    }
//
//    protected String qualifiedEmissionTableName(Dataset dataset) {
//        return qualifiedName(emissionTableName(dataset));
//    }
//
//    protected String emissionTableName(Dataset dataset) {
//        InternalSource[] internalSources = dataset.getInternalSources();
//        return internalSources[0].getTable().toLowerCase();
//    }
//
//    private String qualifiedName(String table) {
//        return datasource.getName() + "." + table;
//    }
//
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
//
//    private SectorScenarioOutputType getSectorScenarioOutputType(String name) throws EmfException {
//        SectorScenarioOutputType resultType = null;
//        Session session = sessionFactory.getSession();
//        try {
//            resultType = sectorScenarioDAO.getSectorScenarioOutputType(name, session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get detailed strategy result type");
//        } finally {
//            session.close();
//        }
//        return resultType;
//    }
//
//    private DatasetType getDatasetType(String name) {
//        DatasetType datasetType = null;
//        Session session = sessionFactory.getSession();
//        try {
//            datasetType = new DatasetTypesDAO().get(name, session);
//        } finally {
//            session.close();
//        }
//        return datasetType;
//    }
//
//    protected SectorScenarioOutput[] getSectorScenarioOutputs() {
//        SectorScenarioOutput[] results = new SectorScenarioOutput[] {};
//        Session session = sessionFactory.getSession();
//        try {
//            results = sectorScenarioDAO.getSectorScenarioOutputs(sectorScenario.getId(), session).toArray(new SectorScenarioOutput[0]);
//        } finally {
//            session.close();
//        }
//        return results;
//    }
//
//    private EmfDataset createCountySummaryDataset() throws EmfException {
//        return creator.addDataset("CSCS", 
//                DatasetCreator.createDatasetName("Strat_County_Sum"), 
//                getDatasetType(DatasetType.strategyCountySummary), 
//                new StrategyCountySummaryTableFormat(dbServer.getSqlDataTypes()), 
//                summaryResultDatasetDescription(DatasetType.strategyCountySummary));
//    }
//
//    private String summaryResultDatasetDescription(String datasetTypeName) {
//        return "#" + datasetTypeName + " result\n" + 
//            "#Implements control strategy: " + sectorScenario.getName() + "\n#";
//    }
//
//    protected void saveSectorScenarioOutput(SectorScenarioOutput strategyResult) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            sectorScenarioDAO.updateSectorScenarioOutput(strategyResult, session);
//            if (controlStrategyInputDatasetCount < 2) {
////                runQASteps(strategyResult);
//            }
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not save control strategy results: " + e.getMessage());
//        } finally {
//            session.close();
//        }
//    }
//
//    protected void saveSectorScenario(SectorScenario strategy) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            sectorScenarioDAO.update(strategy, session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not save control strategy: " + e.getMessage());
//        } finally {
//            session.close();
//        }
//    }
//
//    protected void saveSectorScenarioSummaryOutput(SectorScenarioOutput strategyResult) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            sectorScenarioDAO.updateSectorScenarioOutput(strategyResult, session);
//        } catch (Exception e) {
//            throw new EmfException("Could not save control strategy results: " + e.getMessage());
//        } finally {
//            session.close();
//        }
//    }
//    
//    protected void updateResultDataset(SectorScenarioOutput strategyResult) throws EmfException {
//        Session session = sessionFactory.getSession();
//        DatasetDAO dao = new DatasetDAO();
//        
//        try {
//            EmfDataset result = strategyResult.getOutputDataset();
//            
//            if (result != null) {
//                Version version = dao.getVersion(session, result.getId(), result.getDefaultVersion());
//                
//                if (version != null)
//                    updateVersion(result, version, dbServer, session, dao);
//            }
//        } catch (Exception e) {
//            throw new EmfException("Cannot update result datasets (strategy id: " + strategyResult.getSectorScenarioId() + "). " + e.getMessage());
//        } finally {
//            if (session != null && session.isConnected())
//                session.close();
//        }
//    }
//    
//    private void updateVersion(EmfDataset dataset, Version version, DbServer dbServer, Session session, DatasetDAO dao) throws Exception {
//        version = dao.obtainLockOnVersion(user, version.getId(), session);
//        version.setNumberRecords((int)dao.getDatasetRecordsNumber(dbServer, session, dataset, version));
//        dao.updateVersionNReleaseLock(version, session);
//    }
//
//    private void removeSectorScenarioOutputs() throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            sectorScenarioDAO.removeSectorScenarioResults(sectorScenario.getId(), session);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not remove previous control strategy result(s)");
//        } finally {
//            session.close();
//        }
//    }
//
////    private void removeSectorScenarioOutput(int resultId) throws EmfException {
////        Session session = sessionFactory.getSession();
////        try {
////            controlStrategyDAO.removeSectorScenarioOutput(sectorScenario.getId(), resultId, session);
////        } catch (RuntimeException e) {
////            throw new EmfException("Could not remove previous control strategy result(s)");
////        } finally {
////            session.close();
////        }
////    }
//
//    public SectorScenario getSectorScenario() {
//        return sectorScenario;
//    }
//
//    protected void runQASteps(SectorScenarioOutput strategyResult) {
////        EmfDataset resultDataset = (EmfDataset)strategyResult.getDetailedResultDataset();
//        if (recordCount > 0) {
////            runSummaryQASteps(resultDataset, 0);
//        }
////        excuteSetAndRunQASteps(inputDataset, sectorScenario.getDatasetVersion());
//    }
//
//    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
//        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
//        //11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step templates...
//        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
//        if (qaStepTemplates != null) {
//            String[] qaStepTemplateNames = new String[qaStepTemplates.length];
//            for (int i = 0; i < qaStepTemplates.length; i++) qaStepTemplateNames[i] = qaStepTemplates[i].getName();
//            qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, sectorScenario.getExportDirectory());
//        }
//    }
//
//    protected void disconnectDbServer() throws EmfException {
//        try {
//            dbServer.disconnect();
//        } catch (Exception e) {
//            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
//        }
//    }
//    
//    public long getRecordCount() {
//        return recordCount;
//    }
//
//    protected void addStatus(SectorScenarioInventory controlStrategyInputDataset) {
//        setStatus("Completed processing control strategy input dataset: " 
//                + controlStrategyInputDataset.getInputDataset().getName() 
//                + ".");
//    }
//
//    protected void setStatus(String message) {
//        Status endStatus = new Status();
//        endStatus.setUsername(user.getUsername());
//        endStatus.setType("Strategy");
//        endStatus.setMessage(message);
//        endStatus.setTimestamp(new Date());
//
//        statusDAO.add(endStatus);
//    }
//    
//    public String getFilterForSourceQuery() {
//        String filterForSourceQuery = "";
//        String sqlFilter = "";
//        String filter = "";
//        
//        //get and build strategy filter...
//        if (filter == null || filter.trim().length() == 0)
//            sqlFilter = "";
//        else 
//            sqlFilter = " and (" + filter + ") "; 
//
//        filterForSourceQuery = sqlFilter;
//        return filterForSourceQuery;
//    }
//
//    protected Version version(SectorScenarioInventory controlStrategyInputDataset) {
//        return version(controlStrategyInputDataset.getInputDataset().getId(), controlStrategyInputDataset.getVersion());
//    }
//
//    private Version version(int datasetId, int version) {
//        Session session = sessionFactory.getSession();
//        try {
//            Versions versions = new Versions();
//            return versions.get(datasetId, version, session);
//        } finally {
//            session.close();
//        }
//    }
//    
}

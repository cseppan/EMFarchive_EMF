package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;


public abstract class AbstractStrategyTask implements Strategy {
    
    protected ControlStrategy controlStrategy;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;

    protected DbServer dbServer;

    protected User user;
    
    protected int batchSize;
    
    protected int recordCount;
    
    protected int controlStrategyInputDatasetCount;
    
    private StatusDAO statusDAO;

    private String exportDirectory;
    
    private DatasetCreator creator;
    
    private Keywords keywords;

    private TableFormat tableFormat;
    
    private List<ControlStrategyResult> strategyResultList;

    public AbstractStrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            String exportDirectory) throws EmfException {
        this.controlStrategy = controlStrategy;
        this.controlStrategyInputDatasetCount = controlStrategy.getControlStrategyInputDatasets().length;
        this.dbServerFactory = dbServerFactory;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.exportDirectory = exportDirectory;
        this.statusDAO = new StatusDAO(sessionFactory);
        this.tableFormat = new StrategySummaryResultTableFormat(dbServer.getSqlDataTypes());
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.creator = new DatasetCreator("Summary_", "CSSR_", 
                controlStrategy, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.strategyResultList = new ArrayList<ControlStrategyResult>();
        //setup the strategy run
        setup();
    }

    private void setup() throws EmfException {
        //get rid of old strategy results...
        removeControlStrategyResults();
 
    }
    
    public void run(StrategyLoader loader) throws EmfException {
        String status = "";
        try {
            //process/load each input dataset
            ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                if (loader.inventoryHasTargetPollutant(controlStrategyInputDatasets[i])) {
                    ControlStrategyResult result = new ControlStrategyResult();
                    try {
                        result = loader.loadStrategyResult(controlStrategyInputDatasets[i]);
                        recordCount = loader.getRecordCount();
                        result.setRecordCount(recordCount);
                        status = "Completed.";
                    } catch (Exception e) {
                        e.printStackTrace();
                        status = "Failed. Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". " + result.getRunStatus();
                    } finally {
                        result.setCompletionTime(new Date());
                        result.setRunStatus(status);
                        saveControlStrategyResult(result);
                        strategyResultList.add(result);
                        addStatus(controlStrategyInputDatasets[i]);
                    }
                } else {
                    setStatus("Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". Target pollutant is not in the inventory.");
                }
            }
            
            //now create the summary detailed result based on the results from the strategy run...
            if (controlStrategyInputDatasets.length > 0) {
                //create dataset and strategy summary result 
                ControlStrategyResult summaryResult = createSummaryStrategyResult(controlStrategyInputDatasets[0].getInputDataset());
                //now populate the summary result with data...
                populateStrategySummaryResultDataset(strategyResultList.toArray(new ControlStrategyResult[0]), summaryResult);
                
                //finalize the result, update completion time and run status...
                summaryResult.setCompletionTime(new Date());
                summaryResult.setRunStatus("Completed.");
                saveControlStrategySummaryResult(summaryResult);
                runSummaryQASteps(summaryResult.getInputDataset(), 0);
            }
        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            loader.disconnectDbServer();
            disconnectDbServer();
        }
    }

    private void populateStrategySummaryResultDataset(ControlStrategyResult[] results, ControlStrategyResult summaryResult) throws EmfException {
        if (results.length > 0) {
            String sql = "INSERT INTO " + qualifiedEmissionTableName(summaryResult.getInputDataset()) + " (dataset_id, version, fips, scc, poll, Control_Technology, avg_ann_cost_per_ton, Annual_Cost, Emis_Reduction)" 
            + "select " + summaryResult.getInputDataset().getId() + ", 0, summary.fips, summary.scc, summary.poll, ct.name as Control_Technology, "
            + "sum(summary.Annual_Cost) / sum(summary.Emis_Reduction) as avg_cost_per_ton, " 
            + "sum(summary.Annual_Cost) as Annual_Cost, "
            + "sum(summary.Emis_Reduction) as Emis_Reduction " 
            + "from (";
            for (int i = 0; i < results.length; i++) {
                String tableName = qualifiedEmissionTableName(results[i].getDetailedResultDataset());
                sql += (i > 0 ? " union all " : "") 
                    + "select e.fips, e.scc, e.poll, e.cm_id, sum(e.Annual_Cost) as Annual_Cost, sum(e.Emis_Reduction) as Emis_Reduction "
                    + "from " + tableName + " e "
                    + "group by e.fips, e.scc, e.poll, e.cm_id ";
            }
            sql += ") summary "
                + "inner join emf.control_measures cm "
                + "on cm.id = summary.cm_id "
                + "inner join emf.control_technologies ct "
                + "on ct.id = cm.control_technology "
                + "group by summary.fips, summary.scc, summary.poll, ct.name "
                + "order by summary.fips, summary.scc, summary.poll, ct.name";
            try {
                datasource.query().execute(sql);
            } catch (SQLException e) {
                throw new EmfException("Error occured when inserting data to strategy summary table" + "\n" + e.getMessage());
            }
        }
    }

    private String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    private String qualifiedName(String table) {
        return datasource.getName() + "." + table;
    }

    private ControlStrategyResult createSummaryStrategyResult(EmfDataset inputDataset) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        EmfDataset summaryResultDataset = createSummaryResultDataset(inputDataset);
        
        result.setInputDataset(summaryResultDataset);
        
        result.setStrategyResultType(getSummaryStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing summary result");

        //persist result
        saveControlStrategySummaryResult(result);
        return result;
    }

    private StrategyResultType getSummaryStrategyResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getSummaryStrategyResultType(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private DatasetType getControlStrategySummaryResultDatasetType() {
        DatasetType datasetType = null;
        Session session = sessionFactory.getSession();
        try {
            datasetType = new DatasetTypesDAO().get("Control Strategy Result Summary", session);
        } finally {
            session.close();
        }
        return datasetType;
    }

    private EmfDataset createSummaryResultDataset(EmfDataset inputDataset) throws EmfException {
        return creator.addDataset(inputDataset, getControlStrategySummaryResultDatasetType(), 
                tableFormat);
    }

    protected void saveControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(strategyResult, session);
            if (controlStrategyInputDatasetCount < 2) {
                runQASteps(strategyResult);
            }
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void saveControlStrategySummaryResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(strategyResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void removeControlStrategyResults() throws EmfException {
        ControlStrategyDAO dao = new ControlStrategyDAO();
        Session session = sessionFactory.getSession();
        try {
            dao.removeControlStrategyResults(controlStrategy.getId(), session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

    protected void runQASteps(ControlStrategyResult strategyResult) throws EmfException {
        EmfDataset resultDataset = (EmfDataset)strategyResult.getDetailedResultDataset();
        if (recordCount > 0) {
            runSummaryQASteps(resultDataset, 0);
        }
//        excuteSetAndRunQASteps(inputDataset, controlStrategy.getDatasetVersion());
    }

    protected void runSummaryQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServerFactory);
        //11/14/07 DCD instead of running the default qa steps specified in the property table, lets run all qa step templates...
        QAStepTemplate[] qaStepTemplates = dataset.getDatasetType().getQaStepTemplates();
        String[] qaStepTemplateNames = new String[qaStepTemplates.length];
        for (int i = 0; i < qaStepTemplates.length; i++) qaStepTemplateNames[i] = qaStepTemplates[i].getName();
        qaTask.runSummaryQAStepsAndExport(qaStepTemplateNames, exportDirectory);
    }

    protected void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }
    }
    
    public long getRecordCount() {
        return recordCount;
    }

    private void addStatus(ControlStrategyInputDataset controlStrategyInputDataset) {
        setStatus("Completed processing control strategy input dataset: " 
                + controlStrategyInputDataset.getInputDataset().getName() 
                + ". There were " + recordCount + " records returned.");
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
}
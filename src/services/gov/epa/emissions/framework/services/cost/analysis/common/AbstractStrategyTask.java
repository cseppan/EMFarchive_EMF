package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
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
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;


public abstract class AbstractStrategyTask implements Strategy {
    
    protected ControlStrategy controlStrategy;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DbServer dbServer;

    protected User user;
    
    protected int batchSize;
    
    protected int recordCount;
    
    private StatusDAO statusDAO;

    private String exportDirectory;
    
    public AbstractStrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            String exportDirectory) throws EmfException {
        this.controlStrategy = controlStrategy;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.exportDirectory = exportDirectory;
        this.statusDAO = new StatusDAO(sessionFactory);
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
                    addStatus(controlStrategyInputDatasets[i]);
                }
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

    protected void saveControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(strategyResult, session);
            runQASteps(strategyResult);
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

//    protected StrategyResultType getDetailedStrategyResultType() throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            ControlStrategyDAO dao = new ControlStrategyDAO();
//            StrategyResultType resultType = dao.getDetailedStrategyResultType(session);
//
//            return resultType;
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not get detailed strategy result type");
//        } finally {
//            session.close();
//        }
//    }
//
//    protected String emissionTableName(EmfDataset inputDataset) {
//        InternalSource[] internalSources = inputDataset.getInternalSources();
//        return qualifiedName(datasource, internalSources[0].getTable());
//    }
//
//    protected String qualifiedName(Datasource datasource, String table) {
//        return datasource.getName() + "." + table;
//    }
//
//    protected EmfDataset resultDataset(EmfDataset inputDataset) throws EmfException {
//        return creator.addDataset(inputDataset, detailDatasetType(), description(controlStrategy), tableFormat,
//                source(controlStrategy));
//    }
//
//    protected DatasetType detailDatasetType() {
//        Session session = sessionFactory.getSession();
//        try {
//            return new DatasetTypesDAO().get("Control Strategy Detailed Result", session);
//        } finally {
//            session.close();
//        }
//    }
//
//    protected String description(ControlStrategy controlStrategy) {
//        return "#Control strategy detailed result\n" + 
//           "#Implements control strategy: " + controlStrategy.getName() + "\n"
//                + "#Input dataset used: " + inputDataset.getName()+"\n#";
//    }
//
//    protected String source(ControlStrategy controlStrategy) {
//        Dataset dataset = controlStrategy.getInputDatasets()[0];
//        return dataset.getName();
//    }

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
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServer);
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
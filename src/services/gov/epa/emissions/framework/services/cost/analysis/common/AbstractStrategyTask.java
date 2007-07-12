package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.cost.controlStrategy.io.CSCountyFileFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.io.CSCountyImporter;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;


public abstract class AbstractStrategyTask implements Strategy {
    
    protected TableFormat tableFormat;

    protected ControlStrategy controlStrategy;

    protected Datasource datasource;

    protected HibernateSessionFactory sessionFactory;

    protected DatasetCreator creator;

    protected EmfDataset inputDataset;

    protected DbServer dbServer;

    protected User user;
    
    protected ControlStrategyResult result;
    
    protected OptimizedQuery optimizedQuery;

    protected int batchSize;

    public AbstractStrategyTask(ControlStrategy strategy, User user, DbServerFactory dbServerFactory,
            Integer batchSize, HibernateSessionFactory sessionFactory) throws EmfException {
        this.controlStrategy = strategy;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.batchSize = batchSize.intValue();
        this.tableFormat = new StrategyDetailedResultTableFormat(dbServer.getSqlDataTypes());
        this.creator = new DatasetCreator("Strategy_", "CSDR_", controlStrategy, user, sessionFactory, dbServerFactory);
        this.inputDataset = controlStrategy.getInputDatasets()[0];
        //setup the strategy run
        setup();
    }

    private void setup() throws EmfException {
        //get rid of old strategy
        removeControlStrategyResult(controlStrategy);
        //create result dataset
        EmfDataset resultDataset = resultDataset();
        //create table for result dataset
        createTable(creator.outputTableName());
        //create strat result object
        result = strategyResult(controlStrategy, resultDataset);
        optimizedQuery = sourceQuery(inputDataset);
    }
    
//    public void run() throws EmfException {
//        GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer,
//                emissionTableName(inputDataset), controlStrategy, sessionFactory);
//        SccControlMeasuresMap map = mapGenerator.create();
//
//        OptimizedQuery optimizedQuery = sourceQuery(inputDataset, controlStrategy);
//        String status = "";
//        try {
//            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, sessionFactory, dbServer, result, map,
//                    controlStrategy);
//            loader.load(optimizedQuery);
//            status = "Completed. Input dataset: " + inputDataset.getName() + ".";
//            result.setRunStatus(status);
//        } catch (Exception e) {
//            status = "Failed. Error processing input dataset: " + inputDataset.getName() + ". " + result.getRunStatus();
//            throw new EmfException(e.getMessage());
//        } finally {
//            close(optimizedQuery);
//            result.setCompletionTime(new Date());
//            result.setRunStatus(status);
//            saveResults(result);
//            try {
//                dbServer.disconnect();
//            } catch (SQLException e) {
//                throw new EmfException("Could not disconnect DbServer -" + e.getMessage());
//            }
//        }
//    }

    protected void saveResults() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            setAndRunQASteps();
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResults(result, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results-" + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected ControlStrategyResult strategyResult(ControlStrategy controlStrategy, EmfDataset resultDataset)
            throws EmfException {

        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDatasetId(inputDataset.getId());
        result.setDetailedResultDataset(resultDataset);

        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Created for input dataset: " + inputDataset.getName());

        return result;
    }

    protected void removeControlStrategyResult(ControlStrategy controlStrategy) throws EmfException {
        ControlStrategyDAO dao = new ControlStrategyDAO();
        Session session = sessionFactory.getSession();
        try {
            dao.removeControlStrategyResult(controlStrategy, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result");
        } finally {
            session.close();
        }
    }

    protected StrategyResultType getDetailedStrategyResultType() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            StrategyResultType resultType = dao.getDetailedStrategyResultType(session);

            return resultType;
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
    }

    protected void createTable(String tableName) throws EmfException {
        TableCreator creator = new TableCreator(datasource);
        try {
            if (creator.exists(tableName))
                creator.drop(tableName);

            creator.create(tableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table '" + tableName + "'+\n" + e.getMessage());
        }
    }

    protected String emissionTableName(EmfDataset inputDataset) {
        InternalSource[] internalSources = inputDataset.getInternalSources();
        return qualifiedName(datasource, internalSources[0].getTable());
    }

    protected String qualifiedName(Datasource datasource, String table) {
        return datasource.getName() + "." + table;
    }

    protected EmfDataset resultDataset() throws EmfException {
        return creator.addDataset(detailDatasetType(), description(controlStrategy), tableFormat,
                source(controlStrategy), datasource);
    }

    protected DatasetType detailDatasetType() {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetTypesDAO().get("Control Strategy Detailed Result", session);
        } finally {
            session.close();
        }
    }

    protected String description(ControlStrategy controlStrategy) {
        return "#Control strategy detailed result\n" + 
           "#Implements control strategy: " + controlStrategy.getName() + "\n"
                + "#Input dataset used: " + inputDataset.getName()+"\n#";
    }

    protected String source(ControlStrategy controlStrategy) {
        Dataset dataset = controlStrategy.getInputDatasets()[0];
        return dataset.getName();
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

    protected void setAndRunQASteps() throws EmfException {
        EmfDataset resultDataset = (EmfDataset) result.getDetailedResultDataset();
        excuteSetAndRunQASteps(resultDataset, 0);
//        excuteSetAndRunQASteps(inputDataset, controlStrategy.getDatasetVersion());
    }

    protected void excuteSetAndRunQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServer);
        qaTask.runSummaryQASteps(qaTask.getDefaultSummaryQANames());
    }

    protected OptimizedQuery sourceQuery(EmfDataset dataset) throws EmfException {
        String query = "SELECT *, case when poll = '" + controlStrategy.getTargetPollutant().getName() 
            + "' then 1 else 0 end as sort FROM " + emissionTableName(dataset) 
            + filterForSourceQuery()
            + " order by scc, fips" 
            + (controlStrategy.getDatasetType().getName().equalsIgnoreCase("ORL Point Inventory (PTINV)") ? ", plantid, pointid, stackid, segment" : "" ) 
            + ", sort desc, poll ";
        try {
            return datasource.optimizedQuery(query, batchSize);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());

        }
    }

    protected String filterForSourceQuery() throws EmfException {
        String sqlFilter = "";
        String filter = controlStrategy.getFilter();
        String countyFile = controlStrategy.getCountyFile();

        //get and build strategy filter...
        if (filter == null || filter.trim().length() == 0)
            sqlFilter = "";
        else 
            sqlFilter = " where " + filter; 

        //get and build county filter...
        if (countyFile != null && countyFile.trim().length() > 0) {
            CSCountyImporter countyImporter = new CSCountyImporter(new File(countyFile), 
                    new CSCountyFileFormat());
            String[] fips;
            try {
                fips = countyImporter.run();
            } catch (ImporterException e) {
                throw new EmfException(e.getMessage());
            }
            if (fips.length > 0) 
                sqlFilter += (sqlFilter.length() == 0 ? " where " : " and ") 
                    + " fips in (";
            for (int i = 0; i < fips.length; i++) {
                sqlFilter += (i > 0 ? "," : "") + "'" 
                    + fips[i] + "'";
            }
            if (fips.length > 0) 
                sqlFilter += ")";
        }

        return sqlFilter;
    }

    protected void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }

    }
    
    protected void closeOptimizedQuery() throws EmfException {
        try {
            optimizedQuery.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close optimized query - " + e.getMessage());
        }
    }
}
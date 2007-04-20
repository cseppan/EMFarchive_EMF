package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

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
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
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

public class MaxEmsRedStrategy implements Strategy {

    private TableFormat tableFormat;

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private HibernateSessionFactory sessionFactory;

    private DatasetCreator creator;

    private EmfDataset inputDataset;

    private int batchSize;

    private DbServer dbServer;

    private User user;

    public MaxEmsRedStrategy(ControlStrategy strategy, User user, DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) {
        this.controlStrategy = strategy;
        this.dbServer = dbServerFactory.getDbServer();
        this.datasource = dbServer.getEmissionsDatasource();
        this.batchSize = batchSize.intValue();
        this.sessionFactory = sessionFactory;
        this.user = user;
        this.tableFormat = new MaxEmsRedTableFormat(dbServer.getSqlDataTypes());
        creator = new DatasetCreator("Strategy_", "CSDR_", controlStrategy, user, sessionFactory);
        inputDataset = controlStrategy.getInputDatasets()[0];
    }

    public void run() throws EmfException {
        GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer,
                emissionTableName(inputDataset), controlStrategy, sessionFactory);
        SccControlMeasuresMap map = mapGenerator.create();

        removeControlStrategyResult(controlStrategy);
        EmfDataset resultDataset = resultDataset();
        createTable(creator.outputTableName());
        OptimizedQuery optimizedQuery = sourceQuery(inputDataset, controlStrategy);
        ControlStrategyResult result = strategyResult(controlStrategy, resultDataset);
        String status = "";
        try {
            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, sessionFactory, dbServer, result, map,
                    controlStrategy);
            loader.load(optimizedQuery);
            status = "Completed. Input dataset: " + inputDataset.getName() + ".";
            result.setRunStatus(status);
        } catch (Exception e) {
            status = "Failed. Error processing input dataset: " + inputDataset.getName() + ". " + result.getRunStatus();
            throw new EmfException(e.getMessage());
        } finally {
            close(optimizedQuery);
            result.setCompletionTime(new Date());
            result.setRunStatus(status);
            saveResults(result);
            try {
                dbServer.disconnect();
            } catch (SQLException e) {
                throw new EmfException("Could not disconnect DbServer -" + e.getMessage());
            }
        }
    }

    private void saveResults(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            setAndRunQASteps(result);
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResults(result, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results-" + e.getMessage());
        } finally {
            session.close();
        }
    }

    private ControlStrategyResult strategyResult(ControlStrategy controlStrategy, EmfDataset resultDataset)
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

    private void removeControlStrategyResult(ControlStrategy controlStrategy) throws EmfException {
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

    private StrategyResultType getDetailedStrategyResultType() throws EmfException {
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

    private void close(OptimizedQuery optimizedQuery) throws EmfException {
        try {
            optimizedQuery.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close optimized query -" + e.getMessage());
        }
    }

    private OptimizedQuery sourceQuery(EmfDataset dataset, ControlStrategy controlStrategy) throws EmfException {
        String query = "SELECT *, case when poll = '" + controlStrategy.getTargetPollutant().getName() + "' then 1 else 0 end as sort FROM " + emissionTableName(dataset) + conditionForQuery(controlStrategy)
            + " order by scc, fips" + (controlStrategy.getDatasetType().getName().equalsIgnoreCase("ORL Point Inventory (PTINV)") ? ", plantid, pointid, stackid, segment" : "" ) + ", sort desc";
        try {
            return datasource.optimizedQuery(query, batchSize);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());

        }

    }

    private String conditionForQuery(ControlStrategy controlStrategy) throws EmfException {
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

//    private String whereTag(String filter) {
//        String condition = " ";
//        if (filter.toUpperCase().indexOf("WHERE") == -1)
//            condition = " WHERE ";
//        return condition;
//    }

    private void createTable(String tableName) throws EmfException {
        TableCreator creator = new TableCreator(datasource);
        try {
            if (creator.exists(tableName))
                creator.drop(tableName);

            creator.create(tableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table '" + tableName + "'+\n" + e.getMessage());
        }
    }

    private String emissionTableName(EmfDataset inputDataset) {
        InternalSource[] internalSources = inputDataset.getInternalSources();
        return qualifiedName(datasource, internalSources[0].getTable());
    }

    private String qualifiedName(Datasource datasource, String table) {
        return datasource.getName() + "." + table;
    }

    private EmfDataset resultDataset() throws EmfException {
        return creator.addDataset(detailDatasetType(), description(controlStrategy), tableFormat,
                source(controlStrategy), datasource);
    }

    private DatasetType detailDatasetType() {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetTypesDAO().get("Control Strategy Detailed Result", session);
        } finally {
            session.close();
        }
    }

    private String description(ControlStrategy controlStrategy) {
        return "#Control strategy detailed result\n" + 
           "#Implements control strategy: " + controlStrategy.getName() + "\n"
                + "#Input dataset used: " + inputDataset.getName()+"\n#";
    }

    private String source(ControlStrategy controlStrategy) {
        Dataset dataset = controlStrategy.getInputDatasets()[0];
        return dataset.getName();
    }

    public void close() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

    private void setAndRunQASteps(ControlStrategyResult result) throws EmfException {
        EmfDataset resultDataset = (EmfDataset) result.getDetailedResultDataset();
        excuteSetAndRunQASteps(resultDataset, 0);
        excuteSetAndRunQASteps(inputDataset, controlStrategy.getDatasetVersion());
    }

    private void excuteSetAndRunQASteps(EmfDataset dataset, int version) throws EmfException {
        QAStepTask qaTask = new QAStepTask(dataset, version, user, sessionFactory, dbServer);
        qaTask.runSummaryQASteps(qaTask.getDefaultSummaryQANames());
    }

}

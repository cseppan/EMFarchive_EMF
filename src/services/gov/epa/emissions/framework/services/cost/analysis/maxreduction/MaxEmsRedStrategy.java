package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

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

    public MaxEmsRedStrategy(ControlStrategy strategy, User user, DbServer dbServer, Integer batchSize,
            HibernateSessionFactory sessionFactory) {
        this.controlStrategy = strategy;
        this.dbServer = dbServer;
        this.datasource = dbServer.getEmissionsDatasource();
        this.batchSize = batchSize.intValue();
        this.sessionFactory = sessionFactory;
        this.tableFormat = new MaxEmsRedTableFormat(dbServer.getSqlDataTypes());
        creator = new DatasetCreator("CSDR_", controlStrategy, user, sessionFactory);
        inputDataset = controlStrategy.getInputDatasets()[0];
    }

    public void run() throws EmfException {
        GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer,
                emissionTableName(inputDataset), controlStrategy, sessionFactory);
        SccControlMeasuresMap map = mapGenerator.create();
        EmfDataset resultDataset = resultDataset();
        createTable(creator.outputTableName());
        OptimizedQuery optimizedQuery = sourceQuery(inputDataset, controlStrategy);
        ControlStrategyResult result = strategyResult(resultDataset);

        try {
            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, dbServer, result, map,
                    controlStrategy);
            loader.load(optimizedQuery);
            result.setRunStatus("Completed. Inutput dataset: " + inputDataset.getName() + ".");
        } catch (Exception e) {
            result.setRunStatus("Failed. Error in processing input dataset: " + inputDataset.getName() + ". "
                    + result.getRunStatus());
            throw new EmfException(e.getMessage());
        } finally {
            close(optimizedQuery);
            result.setCompletionTime(new Date());
        }
        saveResults(result);
    }

    private void saveResults(ControlStrategyResult result) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.add(result, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results-" + e.getMessage());
        } finally {
            session.close();
        }
    }

    private ControlStrategyResult strategyResult(EmfDataset resultDataset) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDatasetId(inputDataset.getId());
        result.setDetailedResultDataset(resultDataset);

        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Created for input dataset: " + inputDataset.getName());

        return result;
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
            throw new EmfException("Could not close optimized query-" + e.getMessage());
        }
    }

    private OptimizedQuery sourceQuery(EmfDataset dataset, ControlStrategy controlStrategy) throws EmfException {
        String query = "SELECT * FROM " + emissionTableName(dataset) + conditionForQuery(controlStrategy);
        try {
            return datasource.optimizedQuery(query, batchSize);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());

        }

    }

    private String conditionForQuery(ControlStrategy controlStrategy) {
        String filter = controlStrategy.getFilter();

        String pollutantCondition = "poll=" + "\'" + controlStrategy.getTargetPollutant() + "\'";
        if (filter == null || filter.length() == 0)
            return " WHERE " + pollutantCondition;

        String condition = whereTag(filter);
        return condition + filter + " AND " + pollutantCondition;
    }

    private String whereTag(String filter) {
        String condition = " ";
        if (filter.toUpperCase().indexOf("WHERE") == -1)
            condition = " WHERE ";
        return condition;
    }

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
        return "#Detail Resultset Dataset\n" + "#Implements control strategy " + controlStrategy.getName() + "\n"
                + "#Input dataset used " + inputDataset.getName();
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

}

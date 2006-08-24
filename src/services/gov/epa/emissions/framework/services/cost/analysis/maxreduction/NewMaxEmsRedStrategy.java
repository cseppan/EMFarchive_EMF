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
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class NewMaxEmsRedStrategy implements Strategy {

    private TableFormat tableFormat;

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private HibernateSessionFactory sessionFactory;

    private DatasetCreator creator;

    private EmfDataset inputDataset;

    private int batchSize;

    private DbServer dbServer;

    public NewMaxEmsRedStrategy(ControlStrategy strategy, User user, DbServer dbServer, Integer batchSize,
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

    public void run(User user2) throws EmfException {
        GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer,emissionTableName(inputDataset),controlStrategy,sessionFactory);
        SccControlMeasuresMap map = mapGenerator.create();
        EmfDataset resultDataset = resultDataset();
        createTable(creator.outputTableName());
        OptimizedQuery optimizedQuery = sourceQuery(inputDataset);
        StrategyResult result = strategyResult(resultDataset);

        try {
            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, dbServer, result,map,controlStrategy);
            loader.load(optimizedQuery);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not run control strategy" + "\n" + e.getMessage());
        } finally {
            close(optimizedQuery);
            result.setCompletionTime(new Date());
        }
        //update strateg result status 
        controlStrategy.setStrategyResults(new StrategyResult[] { result });
    }

    private StrategyResult strategyResult(EmfDataset resultDataset) {
        StrategyResult result = new StrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDatasetId(inputDataset.getId());
        result.setDetailedResultDataset(resultDataset);
        return result;
    }

    private void close(OptimizedQuery optimizedQuery) throws EmfException {
        try {
            optimizedQuery.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close optimized query-" + e.getMessage());
        }
    }

    private OptimizedQuery sourceQuery(EmfDataset dataset) throws EmfException {
        String query = "SELECT * FROM " + emissionTableName(dataset) + " WHERE poll=" + "\'"
                + controlStrategy.getTargetPollutant() + "\'";
        try {
            return datasource.optimizedQuery(query, batchSize);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());

        }

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
        return "#Detail Resultset Dataset\n" + "Implements control strategy " + controlStrategy.getName() + "\n"
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

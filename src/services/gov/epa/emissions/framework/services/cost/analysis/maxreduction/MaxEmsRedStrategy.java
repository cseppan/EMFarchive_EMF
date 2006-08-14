package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasuresDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.analysis.ResultTable;
import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class MaxEmsRedStrategy implements Strategy {

    private TableFormat tableFormat;

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private Dataset[] datasets;

    private List strategyResults;

    private String[] sccs;

    private ControlMeasure[] measures;

    private SCCControlMeasureMap map;

    private ResultTable resultTable;

    private int batchSize;

    private DbServer dbServer;

    private HibernateSessionFactory sessionFactory;

    public MaxEmsRedStrategy(ControlStrategy strategy, DbServer dbServer, Integer batchSize, HibernateSessionFactory sessionFactory) throws EmfException {
        this.controlStrategy = strategy;
        this.dbServer = dbServer;
        this.datasource = dbServer.getEmissionsDatasource();
        this.sessionFactory = sessionFactory;
        this.datasets = strategy.getInputDatasets();
        this.batchSize = batchSize.intValue();
        this.strategyResults = new ArrayList();

        this.tableFormat = new MaxEmsRedTableFormat(dbServer.getSqlDataTypes());
        setup();
    }

    private void setup() throws EmfException {
        try {
            this.sccs = getSCCs(datasets, datasource);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        this.measures = getMeasures(sessionFactory);
        this.map = new SCCControlMeasureMap(sccs, measures, controlStrategy.getTargetPollutant(), controlStrategy
                .getCostYear());
    }

    public ControlMeasure[] getMeasures(HibernateSessionFactory sessionFactory) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasuresDAO dao = new ControlMeasuresDAO();
            List all = dao.all(session);

            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            throw new EmfException("could not retrieve control measures.");
        }finally{
            session.close();
        }
    }

    public void run(User user) throws EmfException {
        try {
            calculateResult(datasets, user, datasource);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } 
        setCompletionDate();
    }

    private void calculateResult(Dataset[] datasets, User user, Datasource datasource) throws Exception {
        for (int i = 0; i < datasets.length; i++)
            calculateResultForSingleDataset(datasets[i], user, datasource);

        controlStrategy.setStrategyResults((StrategyResult[]) strategyResults.toArray(new StrategyResult[0]));
    }

    private void calculateResultForSingleDataset(Dataset inputDataset, User user, Datasource datasource)
            throws Exception {
        String query = getSourceQueryString(inputDataset, datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);
        DatasetCreator creator = new DatasetCreator("CSDR_", controlStrategy, user,sessionFactory);
        OptimizedTableModifier modifier = createResultTable(creator.outputTableName());
        String description="TODO: add header comments??";
        EmfDataset outputDataset = creator.addDataset(getDetailedResultDatasetType(),description, tableFormat, inputDataset.getName(), datasource);
        StrategyResult strategyResult = createStrategyResult(outputDataset, inputDataset);
        Dataset resultDataset = strategyResult.getDetailedResultDataset();
        this.strategyResults.add(strategyResult);

        try {
            while (runner.execute()) {
                ResultSet resultSet = runner.getResultSet();
                writeBatchOfData(inputDataset, resultDataset.getId(), resultSet, modifier, strategyResult);
                resultSet.close();
            }

            runner.close();
            strategyResult.setRunStatus("Completed. Inutput dataset: " + inputDataset.getName() + ".");
        } catch (Exception e) {
            strategyResult.setRunStatus("Failed. Error in processing input dataset: " + inputDataset.getName() + ". "
                    + strategyResult.getRunStatus());
        } finally {
            closeResultTable(modifier);
            strategyResult.setCompletionTime(new Date());
        }
    }

    private OptimizedTableModifier createResultTable(String table) throws Exception {
        OptimizedTableModifier modifier = null;
        resultTable = new ResultTable(table, datasource);
        try {
            if (resultTable.exists(table))
                resultTable.drop(table);

            resultTable.create(tableFormat);
            modifier = dataModifier(datasource, table);
            modifier.start();
        } catch (Exception e) {
            resultTable.drop();
            throw new EmfException(e.getMessage());
        }

        return modifier;
    }

    private OptimizedTableModifier dataModifier(Datasource datasource, String table) throws ImporterException {
        try {
            return new OptimizedTableModifier(datasource, table);
        } catch (SQLException e) {
            throw new ImporterException(e.getMessage());
        }
    }

    private void writeBatchOfData(Dataset inputDataset, int resultDatasetId, ResultSet resultSet,
            OptimizedTableModifier modifier, StrategyResult strategyResult) throws Exception {
        double totalCost = 0;
        double totalReduction = 0;
        int sourceCount = 0;

        while (resultSet.next()) {
            sourceCount = resultSet.getInt("Record_Id");
            ControlMeasure cm = map.getMaxRedControlMeasure(resultSet.getString("scc"));
            if (cm == null)
                continue;

            try {
                RecordGenerator generator = new RecordGenerator(inputDataset.getId(), resultDatasetId, resultSet, cm,
                        controlStrategy);
                Record record = generator.getRecord();
                totalCost += generator.getCost();
                totalReduction += generator.getReducedEmissions();
                insertRecord(record, modifier);
            } catch (SQLException e) {
                strategyResult.setRunStatus("Failed. Error in processing record for result table. Input dataset: "
                        + inputDataset.getName() + ". Source record: " + sourceCount + ".");
            }
        }

        strategyResult.setTotalCost(totalCost);
        strategyResult.setTotalReduction(totalReduction);
    }

    private void insertRecord(Record record, OptimizedTableModifier dataModifier) throws Exception {
        try {
            int colsSize = tableFormat.cols().length;
            if (record.size() < colsSize)
                throw new EmfException("The number of tokens in the record are " + record.size()
                        + ", It's less than the number of columns expected(" + colsSize + ")");

            dataModifier.insert((String[]) record.tokens().toArray(new String[0]));
        } catch (SQLException e) {
            throw new EmfException("Error in inserting query\n" + e.getMessage());
        }
    }

    private void closeResultTable(OptimizedTableModifier modifier) throws EmfException {
        try {
            if (modifier == null)
                return;

            modifier.finish();
            modifier.close();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    private String[] getSCCs(Dataset[] datasets, Datasource datasource) throws SQLException {
        String query = getSCCQueryString(datasets, datasource);
        DataQuery dq = datasource.query();

        ResultSet resultSet = dq.executeQuery(query);
        if (resultSet == null)
            return new String[0];

        String[] sccs = extractSCCs(resultSet);
        resultSet.close();

        return sccs;
    }

    private String[] extractSCCs(ResultSet resultSet) throws SQLException {
        List sccs = new ArrayList();

        while (resultSet.next())
            sccs.add(resultSet.getString(1));

        return (String[]) sccs.toArray(new String[0]);
    }

    private String getSCCQueryString(Dataset[] datasets, Datasource datasource) {
        String queryBase = "SELECT DISTINCT scc FROM ";
        String whereClause = "WHERE poll=" + "\'" + controlStrategy.getTargetPollutant() + "\'";

        return getQueryString(datasets, datasource, queryBase, whereClause);
    }

    private String getSourceQueryString(Dataset dataset, Datasource datasource) {
        String queryBase = "SELECT * FROM ";
        String whereClause = "WHERE poll=" + "\'" + controlStrategy.getTargetPollutant() + "\'";

        return getQueryString(new Dataset[] { dataset }, datasource, queryBase, whereClause);
    }

    private String getQueryString(Dataset[] datasets, Datasource datasource, String queryBase, String whereClause) {
        String qualifiedTables = "";

        for (int i = 0; i < datasets.length; i++) {
            InternalSource[] sources = datasets[i].getInternalSources();
            if (i == datasets.length - 1) {
                qualifiedTables += getTableName(datasource, sources) + " ";
                break;
            }

            qualifiedTables += getTableName(datasource, sources) + ", ";
        }

        return queryBase + qualifiedTables + whereClause;
    }

    private String getTableName(Datasource datasource, InternalSource[] sources) {
        String tableName = "";
        for (int i = 0; i < sources.length; i++) {
            if (i == sources.length - 1)
                return tableName += datasource.getName() + "." + sources[i].getTable();

            tableName += datasource.getName() + "." + sources[i].getTable() + ", ";
        }

        return null;
    }

    public void setCompletionDate() {
        controlStrategy.setCompletionDate(new Date());
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
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

    private StrategyResult createStrategyResult(EmfDataset dataset, Dataset inputDataset) throws EmfException {
        Date start = new Date();

        StrategyResult result = new StrategyResult();
        result.setInputDatasetId(inputDataset.getId());
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setDetailedResultDataset(dataset);
        result.setStartTime(start);
        result.setRunStatus("Created for input dataset: " + inputDataset.getName());

        return result;
    }
    
    private DatasetType getDetailedResultDatasetType() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DataCommonsDAO dao = new DataCommonsDAO();
            List types = dao.getDatasetTypes(session);
            for (int i = 0; i < types.size(); i++) {
                DatasetType type = (DatasetType) types.get(i);
                if (type.getName().equalsIgnoreCase("Control Strategy Detailed Result"))
                    return type;
            }
            return null;
        } catch (RuntimeException e) {
            throw new EmfException("Could not get dataset types");
        } finally {
            session.close();
        }
    }

    public void close() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

}

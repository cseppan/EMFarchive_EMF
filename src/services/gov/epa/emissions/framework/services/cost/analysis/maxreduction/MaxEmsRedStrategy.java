package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.HibernateSessionFactory;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasuresDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.analysis.ResultTable;
import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

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

    private double totalCost;

    private double totalReduction;

    private EmfDbServer emfDbServer;

    public MaxEmsRedStrategy(ControlStrategy strategy, Integer batchSize) throws EmfException {
        this.controlStrategy = strategy;
        this.datasource = getDatasource();
        this.datasets = strategy.getInputDatasets();
        this.batchSize = batchSize.intValue();
        this.strategyResults = new ArrayList();

        this.tableFormat = new MaxEmsRedTableFormat(emfDbServer.getSqlDataTypes());
        setup();
    }

    private Datasource getDatasource() throws EmfException {
        try {
            emfDbServer = new EmfDbServer();
            return emfDbServer.getEmissionsDatasource();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void setup() throws EmfException {
        try {
            this.sccs = getSCCs(datasets, datasource);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        this.measures = getMeasures();
        this.map = new SCCControlMeasureMap(sccs, measures, controlStrategy.getTargetPollutant(), controlStrategy
                .getCostYear());
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        try {
            ControlMeasuresDAO dao = new ControlMeasuresDAO();
            Session session = HibernateSessionFactory.get().getSession();
            List all = dao.all(session);
            session.close();
            session.disconnect();

            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            throw new EmfException("could not retrieve control measures.");
        }
    }

    public void run() throws EmfException {
        try {
            calculateResult(datasets, datasource);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            closeConnection();
        }
        setCompletionDate();
    }

    private void closeConnection() throws EmfException {
        try {
            emfDbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void calculateResult(Dataset[] datasets, Datasource datasource) throws Exception {
        for (int i = 0; i < datasets.length; i++)
            calculateResultForSingleDataset(datasets[i], datasource);
        
        controlStrategy.setStrategyResults((StrategyResult[])strategyResults.toArray(new StrategyResult[0]));
    }

    private void calculateResultForSingleDataset(Dataset dataset, Datasource datasource) throws Exception {
        String query = getSourceQueryString(dataset, datasource);
        OptimizedQuery runner = runner = datasource.optimizedQuery(query, batchSize);

        String tableName = getResultTableName();
        OptimizedTableModifier modifier = createResultTable(tableName);
        StrategyResult strategyResult = createStrategyResult(tableName);
        Dataset resultDataset = strategyResult.getDetailedResultDataset();
        this.strategyResults.add(strategyResult);
        
        try {
            while (runner.execute()) {
                ResultSet resultSet = runner.getResultSet();
                writeBatchOfData(dataset.getId(), resultDataset.getId(), resultSet, modifier);
                resultSet.close();
            }

            runner.close();
        } finally {
            closeResultTable(modifier);
            resetResultDataset(resultDataset, tableName);
        }
    }

    private String getResultTableName() {
        String prefix = "MaxEmsRedStrategy_" + controlStrategy.getName().replace(' ', '_');
        String timestamp = "_time_" + new Date().getTime();
        
        return prefix + timestamp;
    }
    
    private void resetResultDataset(Dataset dataset, String tableName) {
        dataset.setName(tableName);
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

    private void writeBatchOfData(int datasetId, int resultDatasetId, ResultSet resultSet, OptimizedTableModifier modifier) throws Exception {
        while (resultSet.next()) {
            ControlMeasure cm = map.getMaxRedControlMeasure(resultSet.getString("scc"));
            if (cm == null)
                continue;

            RecordGenerator generator = new RecordGenerator(datasetId, resultDatasetId, resultSet, cm, controlStrategy);
            Record record = generator.getRecord();
            totalCost += generator.getCost();
            totalReduction += generator.getReducedEmissions("", "");
            insertRecord(record, modifier);
        }
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
                qualifiedTables += getTableNames(datasource, sources) + " ";
                break;
            }

            qualifiedTables += getTableNames(datasource, sources) + ", ";
        }

        return queryBase + qualifiedTables + whereClause;
    }

    private String getTableNames(Datasource datasource, InternalSource[] sources) {
        String tableNames = "";
        for (int i = 0; i < sources.length; i++) {
            if (i == sources.length - 1)
                return tableNames += datasource.getName() + "." + sources[i].getTable();

            tableNames += datasource.getName() + "." + sources[i].getTable() + ", ";
        }

        return null;
    }

    public void setCompletionDate() {
        controlStrategy.setCompletionDate(new Date());
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

    private void addDataset(EmfDataset dataset) throws EmfException {
        try {
            DatasetDAO dao = new DatasetDAO();
            Session session = HibernateSessionFactory.get().getSession();
            if (dao.nameUsed(dataset.getName(), EmfDataset.class, session)) 
                throw new EmfException("The selected dataset name is already in use.");
            
            dao.add(dataset, session);
            session.close();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not add dataset: " + dataset.getName());
        }
    }

    private DatasetType getDetailedResultDatasetType() throws EmfException {
        try {
            DataCommonsDAO dao = new DataCommonsDAO();
            Session session = HibernateSessionFactory.get().getSession();
            List types = dao.getDatasetTypes(session);
            
            for (int i = 0; i < types.size(); i++) {
                DatasetType type = (DatasetType)types.get(i);
                if (type.getName().equalsIgnoreCase("Control Strategy Detailed Result"))
                        return type;
            }
            
            session.close();
            return null;
        } catch (RuntimeException e) {
            throw new EmfException("Could not get dataset types");
        }
    }
    
    private StrategyResultType getDetailedStrategyResultType() throws EmfException {
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            Session session = HibernateSessionFactory.get().getSession();
            StrategyResultType resultType = dao.getDetailedStrategyResultType(session);
            session.close();
            
            return resultType;
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        }
    }
    
    private StrategyResult createStrategyResult(String datasetName) throws EmfException {
        EmfDataset dataset = new EmfDataset();

        dataset.setName(datasetName);
        dataset.setCreator(controlStrategy.getCreator().getUsername());
        dataset.setDatasetType(getDetailedResultDatasetType());
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
        dataset.setStatus("Created by control strategy");
        addDataset(dataset);
        
        StrategyResult result = new StrategyResult();
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setDetailedResultDataset(dataset);
        
        return result;
    }

}

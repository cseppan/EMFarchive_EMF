package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.analysis.ResultTable;
import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MaxEmsRedStrategy implements Strategy {

    private TableFormat tableFormat;

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private Dataset[] datasets;

    private String[] sccs;

    private ControlMeasure[] measures;

    private SCCControlMeasureMap map;

    private int batchSize;

    private CostService costService;

    public MaxEmsRedStrategy(DbServer dbServer, CostService costService, ControlStrategy strategy, Integer batchSize)
            throws EmfException {
        this.controlStrategy = strategy;
        this.datasource = dbServer.getEmissionsDatasource();
        this.datasets = strategy.getDatasets();
        this.batchSize = batchSize.intValue();
        this.costService = costService;
        this.tableFormat = new MaxEmsRedTableFormat(dbServer.getSqlDataTypes());

        //setup();
        if (1 == -1) 
            throw new EmfException("this will never happen.");
    }

    public void setup() throws EmfException {
        try {
            this.sccs = getSCCs(datasets, datasource);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        this.measures = costService.getMeasures();
        this.map = new SCCControlMeasureMap(sccs, measures);
    }

    public void run() throws EmfException {
//        try {
//            calculateResult(datasets, datasource);
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        }
//
//        setControlStrategy();
        if (1 == -1) 
            throw new EmfException("this will never happen.");
    }

    public void calculateResult(Dataset[] datasets, Datasource datasource) throws Exception {
        for (int i = 0; i < datasets.length; i++)
            calculateResultForSingleDataset(datasets[i], datasource);
    }

    private void calculateResultForSingleDataset(Dataset dataset, Datasource datasource) throws Exception {
        String query = getSourceQueryString(dataset, datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);

        OptimizedTableModifier modifier = createResultTable("MaxEmsRedStrategy_" + dataset.getName());

        while (runner.execute()) {
            ResultSet resultSet = runner.getResultSet();
            writeBatchOfData(dataset.getId(), resultSet, modifier);
            resultSet.close();
        }

        runner.close();
        closeResultTable(modifier);
    }

    private OptimizedTableModifier createResultTable(String table) throws Exception {
        OptimizedTableModifier modifier = null;
        ResultTable resultTable = new ResultTable(table, datasource);
        try {
            resultTable.create(tableFormat);
            modifier = dataModifier(datasource, table);
            modifier.start();
        } catch (Exception e) {
            resultTable.drop();
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

    private void writeBatchOfData(int datasetId, ResultSet resultSet, OptimizedTableModifier modifier) throws Exception {
        while (resultSet.next()) {
            String scc = resultSet.getString("scc");
            int sourceId = resultSet.getInt("Record_Id");
            List measures = map.getMappedControlMeasuresList(scc);
            float ann_emis = resultSet.getFloat("ANN_EMIS");
            MaxRedControlMeasureFinder finder = new MaxRedControlMeasureFinder(datasetId, sourceId, measures, scc, 
                    ann_emis, controlStrategy);
            Record record = finder.getRecord();
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
            if (modifier != null)
                modifier.close();
            modifier.finish();
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
        String whereClause = "WHERE poll=" + "\'50000\'";
//        String whereClause = "WHERE poll=" + "\'" + controlStrategy.getMajorPollutant() + "\'";

        return getQueryString(datasets, datasource, queryBase, whereClause);
    }

    private String getSourceQueryString(Dataset dataset, Datasource datasource) {
        String queryBase = "SELECT * FROM ";
        String whereClause = "WHERE poll=" + "\'" + controlStrategy.getMajorPollutant() + "\'";

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
        System.out.println("max strategy: scc query: " + queryBase + qualifiedTables + whereClause);
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

    public void setControlStrategy() {
        controlStrategy.setCompletionDate(new Date());
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

}

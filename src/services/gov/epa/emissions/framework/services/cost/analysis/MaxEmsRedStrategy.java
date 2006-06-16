package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.data.StrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MaxEmsRedStrategy implements Strategy {

    private StrategyResult result;

    private ControlStrategy controlStrategy;

    private Datasource datasource;

    private Dataset[] datasets;

    private String[] sccs;

    private ControlMeasure[] measures;

    private int batchSize;

    private CostService costService;

    public MaxEmsRedStrategy(DbServer dbServer, CostService costService, ControlStrategy strategy, Integer batchSize)
            throws EmfException {
        this.controlStrategy = strategy;
        this.datasource = dbServer.getEmissionsDatasource();
        this.datasets = strategy.getDatasets();
        this.batchSize = batchSize.intValue();
        this.costService = costService;

        setup();
    }

    private void setup() throws EmfException {
        this.result = new StrategyResult();
        try {
            this.sccs = getSCCs(datasets, datasource);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        this.measures = costService.getMeasures();
    }

    public void run() throws EmfException {
        try {
            calculateResult(datasets, datasource);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

        setResult();
        setControlStrategy();
    }

    private void calculateResult(Dataset[] datasets, Datasource datasource) throws SQLException {
        for (int i = 0; i < datasets.length; i++)
            calculateResultForSingleDataset(datasets[i], datasource);
    }

    private void calculateResultForSingleDataset(Dataset dataset, Datasource datasource) throws SQLException {
        String query = getSourceQueryString(dataset, datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);

        while (runner.execute()) {
            ResultSet resultSet = runner.getResultSet();
            writeBatchOfData(measures, resultSet, sccs);
            resultSet.close();
        }

        runner.close();
    }

    private void writeBatchOfData(ControlMeasure[] measures, ResultSet resultSet, String[] sccs) {
        for (int i = 0; i < measures.length; i++)
            System.out.println("maxemisredstrategy: writeBatchofData: measures: " + measures[i].getName());

    }

    private String[] getSCCs(Dataset[] datasets, Datasource datasource) throws SQLException {
        String query = getSCCQueryString(datasets, datasource);
        DataQuery dq = datasource.query();

        ResultSet resultSet = dq.executeQuery(query);
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
        String whereClause = "WHERE pollutant = " + "\'" + controlStrategy.getMajorPollutant() + "\'";

        return getQueryString(datasets, datasource, queryBase, whereClause);
    }

    private String getSourceQueryString(Dataset dataset, Datasource datasource) {
        String queryBase = "SELECT * FROM ";
        String whereClause = "WHERE pollutant = " + "\'" + controlStrategy.getMajorPollutant() + "\'";

        return getQueryString(new Dataset[] { dataset }, datasource, queryBase, whereClause);
    }

    private String getQueryString(Dataset[] datasets, Datasource datasource, String queryBase, String whereClause) {
        String qualifiedTables = "";

        for (int i = 0; i < datasets.length; i++) {
            InternalSource[] sources = datasets[i].getInternalSources();
            if (i == datasets.length - 1) {
                qualifiedTables += getTableNames(datasource, sources);
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

    private void setControlStrategy() {
        controlStrategy.setTotalCost(Math.random() * 100);
        controlStrategy.setReduction(Math.random() * 1000);
        controlStrategy.setCompletionDate(new Date());
    }

    private void setResult() {
        result.setSourceId(1);
        result.setDatasetId(1);
        result.setControlMeasureID(1);
        result.setControlStrategy(controlStrategy.getName());
        result.setPollutant(controlStrategy.getMajorPollutant());
        result.setCost(Math.random() * 100);
        result.setCostPerTon(Math.random() * 10);
        result.setRedEmissions(Math.random() * 1000);
    }

    public StrategyResult getResult() {
        return result;
    }

    public ControlStrategy getControlStrategy() {
        return controlStrategy;
    }

}

package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StrategyLoader {

    private TableFormat tableFormat;

    private OptimizedTableModifier modifier;

    private StrategyResult result;

    private CalMaxEmsRedControlMeasure maxEmsReduction;

    private double totalCost;

    private double totalReduction;

    public StrategyLoader(String tableName, TableFormat tableFormat, DbServer dbServer, StrategyResult result,
            SccControlMeasuresMap map, ControlStrategy controlStrategy) throws EmfException {
        this.tableFormat = tableFormat;
        this.result = result;
        modifier = dataModifier(tableName, dbServer.getEmissionsDatasource());
        CostYearTable costYearTable = new CostYearTableReader(dbServer, controlStrategy.getCostYear()).costYearTable();
        maxEmsReduction = new CalMaxEmsRedControlMeasure(map, costYearTable, controlStrategy);
        start();
        totalCost = 0.0;
        totalReduction = 0.0;
    }

    private void start() throws EmfException {
        try {
            modifier.start();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void load(OptimizedQuery optimizedQuery) throws Exception {
        try {
            while (optimizedQuery.execute()) {
                ResultSet resultSet = optimizedQuery.getResultSet();
                doBatchInsert(resultSet, optimizedQuery);
            }
        } finally {
            modifier.close();
        }
        result.setTotalCost(totalCost);
        result.setTotalReduction(totalReduction);
    }

    private void doBatchInsert(ResultSet resultSet, OptimizedQuery optimizedQuery) throws SQLException, Exception {
        int sourceCount = 0;
        try {
            while (resultSet.next()) {
                sourceCount = resultSet.getInt("Record_Id");
                String scc = resultSet.getString("scc");
                String fips = resultSet.getString("fips");

                MaxEmsRedContorlMeasure maxCM = maxEmsReduction.getControlMeasure(scc, fips);
                if (maxCM == null)
                    continue; // LOG???
                try {
                    NewRecordGenerator generator = new NewRecordGenerator(result);
                    Record record = generator.getRecord(resultSet, maxCM);
                    totalCost += maxCM.cost();
                    totalReduction += generator.reducedEmission();
                    insertRecord(record, modifier);
                } catch (SQLException e) {
                    result.setRunStatus("Failed. Error in processing record for source record: " + sourceCount + ".");
                }

            }
        } finally {
            resultSet.close();
        }
    }

    private OptimizedTableModifier dataModifier(String table, Datasource datasource) throws EmfException {
        try {
            return new OptimizedTableModifier(datasource, table);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
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

}

package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.Pollutants;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyLoader {

    private TableFormat tableFormat;

    private OptimizedTableModifier modifier;

    private ControlStrategyResult result;

    private RetrieveMaxEmsRedControlMeasure retrieveMeasure;

    private ControlStrategy controlStrategy;

    private double totalCost;

    private double totalReduction;
    
    private DatasetType type;
    
    private boolean pointDatasetType;
    
    private String sourceScc = "";
    
    private String sourceFips = "";

    private String sourcePlantId = "";
    
    private String sourcePointId = "";

    private String sourceStackId = "";
    
    private String sourceSegment = "";

    private boolean newSource;

    private MaxEmsRedControlMeasure maxCM;
    
    private Pollutants pollutants;

    public StrategyLoader(String tableName, TableFormat tableFormat, HibernateSessionFactory sessionFactory, DbServer dbServer, ControlStrategyResult result,
            SccControlMeasuresMap map, ControlStrategy controlStrategy) throws EmfException {
        this.tableFormat = tableFormat;
        this.result = result;
        this.controlStrategy = controlStrategy;
        this.type = this.controlStrategy.getDatasetType();
        this.pointDatasetType = this.type.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)");
        this.pollutants = new Pollutants(sessionFactory);
        modifier = dataModifier(tableName, dbServer.getEmissionsDatasource());
        CostYearTable costYearTable = new CostYearTableReader(dbServer, controlStrategy.getCostYear()).costYearTable();
        retrieveMeasure = new RetrieveMaxEmsRedControlMeasure(map, costYearTable, controlStrategy);
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
                doBatchInsert(resultSet);
            }
        } finally {
            modifier.finish();
            modifier.close();
        }
        result.setTotalCost(totalCost);
        result.setTotalReduction(totalReduction);
    }

    private void doBatchInsert(ResultSet resultSet) throws SQLException, Exception {
        int sourceCount = 0;
        String scc = "";
        String fips = "";
        String plantId = "";
        String pointId = "";
        String stackId = "";
        String segment = "";
        try {
            while (resultSet.next()) {
                
                scc = resultSet.getString("scc");
                fips = resultSet.getString("fips");
                if (pointDatasetType) {
                    plantId = resultSet.getString("plantid");
                    pointId = resultSet.getString("pointid");
                    stackId = resultSet.getString("stackid");
                    segment = resultSet.getString("segment");
                }
                //default first record to beginning values or reset values to new source...
                if (sourceScc.length() == 0 || 
                        !(
                                sourceScc.equals(scc)
                                && sourceFips.equals(fips)
                                && sourcePlantId.equals(plantId)
                                && sourcePointId.equals(pointId)
                                && sourceStackId.equals(stackId)
                                && sourceSegment.equals(segment)
                         )
                    ) {
                    sourceScc = scc;
                    sourceFips = fips;
                    if (pointDatasetType) {
                        sourcePlantId = plantId;
                        sourcePointId = pointId;
                        sourceStackId = stackId;
                        sourceSegment = segment;
                    }
                    newSource = true;
                } else {
                    newSource = false;
                }
                sourceCount = resultSet.getInt("Record_Id");
                
                //find best measure for source (based on target pollutant)...
                if (newSource) {
                    if (!pointDatasetType) {
                        maxCM = retrieveMeasure.findBestMaxEmsRedMeasure(scc, fips, pollutants.getPollutant(resultSet.getString("poll")),
                                resultSet.getDouble("CEFF"), resultSet.getDouble("RPEN"), 
                                resultSet.getDouble("REFF"), resultSet.getDouble("ANN_EMIS"));
                    } else {
                        maxCM = retrieveMeasure.findBestMaxEmsRedMeasure(scc, fips, pollutants.getPollutant(resultSet.getString("poll")),
                                resultSet.getDouble("CEFF"), 100, 
                                resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF"), resultSet.getDouble("ANN_EMIS"));
                    }
                //find best efficiency record for source and cobenefit pollutant, measure was already determined above...
                } else {
                    maxCM = retrieveMeasure.getMaxEmsRedMeasureForCobenefitPollutant(pollutants.getPollutant(resultSet.getString("poll")));
                }
                if (maxCM == null)
                    continue; // LOG???
                try {
                    RecordGenerator generator = getRecordGenerator();
                    Record record = generator.getRecord(resultSet, maxCM);
                    totalCost += generator.reducedEmission() * maxCM.adjustedCostPerTon();
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

    private RecordGenerator getRecordGenerator() {

        if (type.getName().equalsIgnoreCase("ORL Nonpoint Inventory (ARINV)"))
            return new NonpointRecordGenerator(result);
        else if (type.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)"))
            return new PointRecordGenerator(result);
        else if (type.getName().equalsIgnoreCase("ORL Onroad Inventory (MBINV)"))
            return new OnroadRecordGenerator(result);
        else if (type.getName().equalsIgnoreCase("ORL Nonroad Inventory (ARINV)"))
            return new NonroadRecordGenerator(result);

        return null;
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
            throw new EmfException("Error processing insert query: " + e.getMessage());
        }
    }

}

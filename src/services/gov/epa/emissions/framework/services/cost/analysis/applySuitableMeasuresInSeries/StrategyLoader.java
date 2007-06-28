package gov.epa.emissions.framework.services.cost.analysis.applySuitableMeasuresInSeries;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.NonpointRecordGenerator;
import gov.epa.emissions.framework.services.cost.analysis.common.NonroadRecordGenerator;
import gov.epa.emissions.framework.services.cost.analysis.common.OnroadRecordGenerator;
import gov.epa.emissions.framework.services.cost.analysis.common.PointRecordGenerator;
import gov.epa.emissions.framework.services.cost.analysis.common.RecordGenerator;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.Pollutants;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyLoader {

    private static Log LOG = LogFactory.getLog(StrategyLoader.class);

    private TableFormat tableFormat;

    private OptimizedTableModifier modifier;

    private ControlStrategyResult result;

    private RetrieveBestMeasureEffRecords retrieveBestMeasureEffRecords;
    
    private RetrieveBestMeasures retrieveBestMeasures;

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

    private boolean newScc;

    private boolean newFips;

    private boolean targetPollutant;

//    private boolean hasConstraints;

//    private boolean startSourcePollutant;
//
//    private boolean endSourcePollutant;

//    private double sourceEmis;

//    private List<BestMeasureEffRecord> targetPollutantBestMeasureList;
    
    private ControlMeasure[] sccMeasures = {};
    
    private ControlMeasure[] sourceMeasures = {};
    
    private Pollutants pollutants;

    private DecimalFormat decFormat;

//    private DbServer dbServer;
//    
//    private HibernateSessionFactory sessionFactory;
//    
    private CostYearTable costYearTable;
    
    private Datasource datasource;

    private GenerateSccControlMeasuresMap mapGenerator;

    public StrategyLoader(String tableName, TableFormat tableFormat, HibernateSessionFactory sessionFactory, 
            DbServer dbServer, ControlStrategyResult result, ControlStrategy controlStrategy) throws EmfException {
        this.tableFormat = tableFormat;
        this.result = result;
        this.controlStrategy = controlStrategy;
        this.type = this.controlStrategy.getDatasetType();
        this.pointDatasetType = this.type.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)");
        this.pollutants = new Pollutants(sessionFactory);
        this.decFormat = new DecimalFormat("0.#####E0");
//        this.dbServer = dbServer;
//        this.sessionFactory = sessionFactory;
        this.datasource = dbServer.getEmissionsDatasource();
        this.modifier = dataModifier(tableName, dbServer.getEmissionsDatasource());
        this.mapGenerator = new GenerateSccControlMeasuresMap(dbServer,
                emissionTableName(controlStrategy.getInputDatasets()[0]), controlStrategy, sessionFactory);
        this.costYearTable = new CostYearTableReader(dbServer, controlStrategy.getCostYear()).costYearTable();
        this.retrieveBestMeasureEffRecords = new RetrieveBestMeasureEffRecords(controlStrategy, costYearTable);
        this.retrieveBestMeasures = new RetrieveBestMeasures(controlStrategy, costYearTable);
//        this.hasConstraints = controlStrategy.getConstraint().hasConstraints();
        start();
        this.totalCost = 0.0;
        this.totalReduction = 0.0;
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
        Pollutant poll = null;
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
                    if (sourceScc.equals(scc)) newScc = false; else newScc = true;
                    if (sourceFips.equals(fips)) newFips = false; else newFips = true;
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
                    newScc = false;
                    newFips = false;
                }
                
                sourceCount = resultSet.getInt("Record_Id");
                poll = pollutants.getPollutant(resultSet.getString("poll"));
                targetPollutant = poll.equals(controlStrategy.getTargetPollutant());

                //for a new source get a list of best measures for the strat target pollutant
                if (newSource) {
//                    sccMeasureMap = mapGenerator.create(scc);
                    if (newScc)
                        sccMeasures = mapGenerator.create(scc).getControlMeasures(scc);

                    if (newFips && targetPollutant) {
                        sourceMeasures = retrieveBestMeasures.findTargetPollutantBestMeasures(sccMeasures, fips, 
                            resultSet.getDouble("CEFF"), resultSet.getDouble("RPEN"), 
                            resultSet.getDouble("REFF"), resultSet.getDouble("ANN_EMIS"));
                    } else {
                        sourceMeasures = sccMeasures;
                    }
//                    System.out.println("scc = " + scc + ", fips = " + fips + ", Measure Count = " + measures.length);
//                    targetPollutantBestMeasureList = retrieveBestMeasureEffRecords.findTargetPollutantBestMeasureEffRecords(mapGenerator.create(scc).getControlMeasures(scc), fips, targetPollutant ? resultSet.getDouble("CEFF") : 0, 
//                            targetPollutant ? resultSet.getDouble("RPEN") : 0, targetPollutant ? resultSet.getDouble("REFF") : 0, targetPollutant ? resultSet.getDouble("ANN_EMIS") : 0);
//                    System.out.println(targetPollutant + "scc = " + scc + ", Measues Count = " + measureList.size());

                    //Print anagram groups.
//                      for (BestMeasureEffRecord l : targetPollutantBestMeasureList)
//                          LOG.error((l.adjustedCostPerTon() == null ? -1 : l.adjustedCostPerTon()) + ": " + l.measure().getAbbreviation() + ": " + l.efficiencyRecord().getPollutant() + ": " + l.efficiencyRecord().getLocale() + ": " + l.efficiencyRecord().getCostPerTon());
                }
//                System.out.println(hasConstraints);

                //get best measure eff records...
                List<BestMeasureEffRecord> bestMeasureEffRecordList = 
                    targetPollutant 
                    ? retrieveBestMeasureEffRecords.findTargetPollutantBestMeasureEffRecords(sourceMeasures, fips, resultSet.getDouble("CEFF"), 
                            !pointDatasetType ? resultSet.getDouble("RPEN") : 100, !pointDatasetType ? resultSet.getDouble("REFF") : resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF"), resultSet.getDouble("ANN_EMIS"))
                    : retrieveBestMeasureEffRecords.findCobenefitPollutantBestMeasureEffRecords(sourceMeasures, fips, poll, resultSet.getDouble("ANN_EMIS"));

for (BestMeasureEffRecord l : bestMeasureEffRecordList)
    LOG.error(scc + " " + fips + " " + (l.adjustedCostPerTon() == null ? -1 : l.adjustedCostPerTon()) + ": " + l.measure().getAbbreviation() + ": " + l.efficiencyRecord().getPollutant() + ": " + l.efficiencyRecord().getLocale() + ": " + l.efficiencyRecord().getCostPerTon());
                
                if (bestMeasureEffRecordList == null || bestMeasureEffRecordList.size() == 0)
                    continue;

                int listSize = bestMeasureEffRecordList.size();
                double sourceEmis = resultSet.getDouble("ANN_EMIS");
                for (int i = 0; i < listSize; i++) {
                    BestMeasureEffRecord bestMeasureEffRecord = bestMeasureEffRecordList.get(i);
                    try {
                        RecordGenerator generator = getRecordGenerator();
                        Record record = generator.getRecord(resultSet, bestMeasureEffRecord, sourceEmis, i == 0 ? true : false, i == listSize - 1 ? true : false);
                        totalCost += generator.reducedEmission() * bestMeasureEffRecord.adjustedCostPerTon();
                        sourceEmis -= generator.reducedEmission();
                        if (poll.equals(controlStrategy.getTargetPollutant()))
                            totalReduction += generator.reducedEmission();
                        insertRecord(record, modifier);
                    } catch (SQLException e) {
                        result.setRunStatus("Failed. Error in processing record for source record: " + sourceCount + ".");
                    }
                }
            }
        } finally {
            resultSet.close();
        }
    }

    private RecordGenerator getRecordGenerator() {

        if (type.getName().equalsIgnoreCase("ORL Nonpoint Inventory (ARINV)"))
            return new NonpointRecordGenerator(result, decFormat);
        else if (type.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)"))
            return new PointRecordGenerator(result, decFormat);
        else if (type.getName().equalsIgnoreCase("ORL Onroad Inventory (MBINV)"))
            return new OnroadRecordGenerator(result, decFormat);
        else if (type.getName().equalsIgnoreCase("ORL Nonroad Inventory (ARINV)"))
            return new NonroadRecordGenerator(result, decFormat);

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

    private String emissionTableName(EmfDataset inputDataset) {
        InternalSource[] internalSources = inputDataset.getInternalSources();
        return qualifiedName(datasource, internalSources[0].getTable());
    }

    private String qualifiedName(Datasource datasource, String table) {
        return datasource.getName() + "." + table;
    }
}
package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StrategyLoader extends AbstractStrategyLoader {

    private RetrieveBestMeasureEffRecords retrieveBestMeasureEffRecords;
    
    private RetrieveBestMeasures retrieveBestMeasures;

    private boolean newScc;

    private boolean newFips;

    private boolean targetPollutant;

    private ControlMeasure[] sccMeasures = {};
    
    private ControlMeasure[] sourceMeasures = {};
    
    private GenerateSccControlMeasuresMap mapGenerator;
    
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
        this.retrieveBestMeasureEffRecords = new RetrieveBestMeasureEffRecords(controlStrategy, costYearTable);
        this.retrieveBestMeasures = new RetrieveBestMeasures(controlStrategy, costYearTable,
                dbServer, sessionFactory);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        this.mapGenerator = new GenerateSccControlMeasuresMap(dbServer, qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()), 
                controlStrategy, sessionFactory);
        newScc = false;
        newFips = false;
        targetPollutant = false;
        //call the abstract method to do the work...
        return super.loadStrategyResult(controlStrategyInputDataset);
    }

    protected void doBatchInsert(ResultSet resultSet) throws Exception {
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
                
                currentTime = System.currentTimeMillis();
                
                //for a new source get a list of best measures for the strat target pollutant
                if (newSource) {
//                    sccMeasureMap = mapGenerator.create(scc);
                    if (newScc) {
                        currentTime = System.currentTimeMillis();
                        sccMeasures = mapGenerator.create(scc).getControlMeasures(scc);
                        getSourceMeasuresTime += System.currentTimeMillis() - currentTime;
                        currentTime = System.currentTimeMillis();
                    }

                    if (newFips && targetPollutant) {
                        currentTime = System.currentTimeMillis();
                        sourceMeasures = retrieveBestMeasures.findTargetPollutantBestMeasures(sccMeasures, fips, 
                            resultSet.getDouble("CEFF"), !pointDatasetType ? resultSet.getDouble("RPEN") : 100, 
                            resultSet.getDouble("REFF"), getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")));
                    } else {
                        sourceMeasures = sccMeasures;
                    }
//                    System.out.println(targetPollutant + " scc = " + scc + ", Measues Count = " + measureList.size());

                }

                //get best measure eff records...
                List<BestMeasureEffRecord> bestMeasureEffRecordList = 
                    targetPollutant 
                    ? retrieveBestMeasureEffRecords.findTargetPollutantBestMeasureEffRecords(sourceMeasures, fips, 
                            resultSet.getDouble("CEFF"), !pointDatasetType ? resultSet.getDouble("RPEN") : 100, 
                            !pointDatasetType ? resultSet.getDouble("REFF") : resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF"), getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")))
                    : retrieveBestMeasureEffRecords.findCobenefitPollutantBestMeasureEffRecords(sourceMeasures, fips, 
                            poll, getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")));

                matchTime += System.currentTimeMillis() - currentTime;
//for (BestMeasureEffRecord l : bestMeasureEffRecordList)
//    System.err.println(targetPollutant + " " + scc + " " + fips + " " + (l.adjustedCostPerTon() == null ? -1 : l.adjustedCostPerTon()) + ": " + l.measure().getAbbreviation() + ": " + l.efficiencyRecord().getPollutant() + ": " + l.efficiencyRecord().getLocale() + ": " + l.efficiencyRecord().getCostPerTon() + ": " + l.measure().getApplyOrder());

                if (bestMeasureEffRecordList == null || bestMeasureEffRecordList.size() == 0)
                    continue;

                int listSize = bestMeasureEffRecordList.size();
                double sourceEmis = getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS"));
                for (int i = 0; i < listSize; i++) {
                    BestMeasureEffRecord bestMeasureEffRecord = bestMeasureEffRecordList.get(i);
                    try {
                        Record record = recordGenerator.getRecord(resultSet, bestMeasureEffRecord, 
                                sourceEmis, i == 0 ? true : false, i == listSize - 1 ? true : false);
                        totalCost += recordGenerator.totalCost() != null ? recordGenerator.totalCost() : 0.0;
                        sourceEmis -= recordGenerator.reducedEmission();
                        if (poll.equals(controlStrategy.getTargetPollutant()))
                            totalReduction += recordGenerator.reducedEmission();
                        currentTime = System.currentTimeMillis();
                        insertRecord(record, modifier);
                        insertSourceTime += System.currentTimeMillis() - currentTime;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        throw new EmfException("Error processing record for source record: " + sourceCount + ". Exception: " + e.getMessage());
                    }
                }
            }
        } finally {
            //
        }
    }
}

package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StrategyLoader extends AbstractStrategyLoader {

    private RetrieveBestMeasureEffRecord retrieveMeasure;

    private BestMeasureEffRecord bestMeasureEffRecord;

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize, boolean useSQLApproach) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        //set up things that are specific for this strategy...
        GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer, qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()), 
                controlStrategy, sessionFactory, super.getSourcePollutantList(controlStrategyInputDataset));
        SccControlMeasuresMap map = !useSQLApproach ? mapGenerator.create() : null;
        retrieveMeasure = !useSQLApproach ? new RetrieveBestMeasureEffRecord(map, costYearTable, 
                controlStrategy, dbServer,
                sessionFactory) : null;
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
                poll = pollutants.getPollutant(resultSet.getString("poll"));
                //find best measure for source (based on target pollutant)...
                if (newSource) {
                    if (!pointDatasetType) {
                        bestMeasureEffRecord = retrieveMeasure.findBestMaxEmsRedMeasure(scc, fips, 
                                poll, resultSet.getDouble("CEFF"), 
                                resultSet.getDouble("RPEN"), resultSet.getDouble("REFF"), 
                                getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")));
                    } else {
                        bestMeasureEffRecord = retrieveMeasure.findBestMaxEmsRedMeasure(scc, fips, 
                                poll, resultSet.getDouble("CEFF"), 
                                100, resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF"), 
                                getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")));
                    }
                //find best efficiency record for source and cobenefit pollutant, measure was already determined above from the first pass...
                } else {
                    bestMeasureEffRecord = retrieveMeasure.getMaxEmsRedMeasureForCobenefitPollutant(poll, getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")));
                }
                if (bestMeasureEffRecord == null)
                    continue;
                try {
                    Record record = recordGenerator.getRecord(resultSet, bestMeasureEffRecord, 
                            getEmission(resultSet.getDouble("ANN_EMIS"), resultSet.getDouble("AVD_EMIS")), true, 
                            true);
                    
                    totalCost += recordGenerator.totalCost() != null ? recordGenerator.totalCost() : 0.0;
                    if (poll.equals(controlStrategy.getTargetPollutant()))
                        totalReduction += recordGenerator.reducedEmission();
                    insertRecord(record, modifier);
                } catch (SQLException e) {
                    throw new EmfException("Error in processing record for source record: " + sourceCount + ". Exception: " + e.getMessage());
                }
            }
        } finally {
            //
        }
    }
}
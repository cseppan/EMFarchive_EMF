package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class RetrieveBestEffRecord {

    private EfficiencyRecordUtil efficiencyRecordUtil;

    private double tollerance;

    private CostYearTable costYearTable;

    public RetrieveBestEffRecord(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.efficiencyRecordUtil = new EfficiencyRecordUtil();
        this.tollerance = 1e-5;
    }

    public EfficiencyRecord findBestEfficiencyRecord(ControlMeasure controlMeasure, EfficiencyRecord[] ers) throws EmfException {
        if (ers.length == 0)
            return null;

        if (ers.length == 1)
            return ers[0];

        EfficiencyRecord maxRecord = ers[0];

        for (int i = 1; i < ers.length; i++) {
            maxRecord = findBestEfficiencyRecord(controlMeasure, ers[i], 
                    controlMeasure, maxRecord);
        }

        return maxRecord;

    }

    public EfficiencyRecord findBestEfficiencyRecord(ControlMeasure controlMeasure, EfficiencyRecord record, 
            ControlMeasure bestControlMeasure, EfficiencyRecord bestRecord) throws EmfException {
        if (record == null && bestRecord == null) 
            return null;
        
        if (bestRecord == null) 
            return record;
        
        if (record == null) 
            return bestRecord;
        
        double diff = efficiencyRecordUtil.effectiveReduction(controlMeasure, record)
                - efficiencyRecordUtil.effectiveReduction(bestControlMeasure, bestRecord);
        if (diff > tollerance) {
            return record;
        }
        if (diff < tollerance)
            return bestRecord;

        return compareCost(record, bestRecord);
    }

    private EfficiencyRecord compareCost(EfficiencyRecord record, EfficiencyRecord maxRecord) throws EmfException {
        double cost = efficiencyRecordUtil.adjustedCostPerTon(record, costYearTable);
        double maxCost = efficiencyRecordUtil.adjustedCostPerTon(maxRecord, costYearTable);

        double diff = cost - maxCost;

        if (diff >= tollerance) {
            return record;
        }
        return maxRecord;
    }
}
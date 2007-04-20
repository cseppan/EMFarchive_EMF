package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class RetrieveMaxEmsRedEfficiencyRecord {

    private EfficiencyRecordUtil efficiencyRecordUtil;

    private double tollerance;

    private CostYearTable costYearTable;

    public RetrieveMaxEmsRedEfficiencyRecord(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.efficiencyRecordUtil = new EfficiencyRecordUtil();
        this.tollerance = 1e-5;
    }

    public EfficiencyRecord findBestEfficiencyRecord(EfficiencyRecord[] ers) throws EmfException {
        if (ers.length == 0)
            return null;

        if (ers.length == 1)
            return ers[0];

        EfficiencyRecord maxRecord = ers[0];

        for (int i = 1; i < ers.length; i++) {
            maxRecord = findBestEfficiencyRecord(ers[i], maxRecord);
        }

        return maxRecord;

    }

    public EfficiencyRecord findBestEfficiencyRecord(EfficiencyRecord record, EfficiencyRecord maxRecord) throws EmfException {
        if (record == null && maxRecord == null) 
            return null;
        
        if (maxRecord == null) 
            return record;
        
        if (record == null) 
            return maxRecord;
        
        double diff = efficiencyRecordUtil.effectiveReduction(record)
                - efficiencyRecordUtil.effectiveReduction(maxRecord);
        if (diff > tollerance) {
            return record;
        }
        if (diff < tollerance)
            return maxRecord;

        return compareCost(record, maxRecord);
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
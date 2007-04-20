package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

// This class represents the best measure which was picked based on the target pollutant and also
// contains the best efficiency record for that pollutant (which could be the target or cobenefit pollutant)
public class MaxEmsRedControlMeasure {

    private ControlMeasure controlMeasure;

    private EfficiencyRecord maxRecord;

    private CostYearTable table;

    private EfficiencyRecordUtil efficiencyRecordUtil;

    public MaxEmsRedControlMeasure(ControlMeasure controlMeasure, EfficiencyRecord record, CostYearTable table) {
        this.controlMeasure = controlMeasure;
        this.maxRecord = record;
        this.table = table;
        efficiencyRecordUtil = new EfficiencyRecordUtil();
    }

    public ControlMeasure measure() {
        return controlMeasure;
    }

    public double adjustedCostPerTon() throws EmfException {
        return efficiencyRecordUtil.adjustedCostPerTon(maxRecord, table);
    }

    public double effectiveReduction() {
        return efficiencyRecordUtil.effectiveReduction(maxRecord);
    }

    public double costPerTon() {
        return maxRecord.getCostPerTon();
    }

    public double controlEfficiency() {
        return maxRecord.getEfficiency();
    }

    public double rulePenetration() {
        return maxRecord.getRulePenetration();
    }

    public double ruleEffectiveness() {
        return maxRecord.getRuleEffectiveness();
    }
}

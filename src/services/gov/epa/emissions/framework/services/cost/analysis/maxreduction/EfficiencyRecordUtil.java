package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EfficiencyRecordUtil {

    public double effectionReduction(EfficiencyRecord record) {
        return (record.getEfficiency() * record.getRuleEffectiveness() * record.getRuleEffectiveness())
                / (100 * 100 * 100);
    }

    public double cost(EfficiencyRecord record, CostYearTable costYearTable) throws EmfException {
        int costYear = record.getCostYear();
        double factor = costYearTable.factor(costYear);
        return factor * record.getCostPerTon();
    }

}

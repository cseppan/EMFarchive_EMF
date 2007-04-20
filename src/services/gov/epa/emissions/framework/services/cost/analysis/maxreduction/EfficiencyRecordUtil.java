package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EfficiencyRecordUtil {

    public double effectiveReduction(EfficiencyRecord record) {
        return (record.getEfficiency() * record.getRuleEffectiveness() * record.getRulePenetration())
                / (100 * 100 * 100);
    }

    public double adjustedCostPerTon(EfficiencyRecord record, CostYearTable costYearTable) throws EmfException {
        int costYear = record.getCostYear();
        double factor = costYearTable.factor(costYear);
        return factor * record.getCostPerTon();
    }

    public double calculateEmissionReduction(EfficiencyRecord record, double invenControlEfficiency, double invenRulePenetration, double invenRuleEffectiveness, double invenAnnualEmissions) {
        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
                / (100 * 100 * 100);
        double effectiveReduction = effectiveReduction(record);

        if (invenEffectiveReduction == 0.0) {
            return invenAnnualEmissions * effectiveReduction;
        }

        if (invenEffectiveReduction < effectiveReduction) {
            return  invenAnnualEmissions / invenEffectiveReduction * effectiveReduction;
        }

        return invenAnnualEmissions / invenControlEfficiency * invenEffectiveReduction;
    }
}

package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.cost.controlstrategy.LocaleFilter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class EfficiencyRecordUtil {

    private LocaleFilter localeFilter;
//    private static Log LOG = LogFactory.getLog(EfficiencyRecordUtil.class);

    public EfficiencyRecordUtil() {
        this.localeFilter = new LocaleFilter();
    }

    public double effectiveReduction(EfficiencyRecord record) {
        return (record.getEfficiency() * record.getRuleEffectiveness() * record.getRulePenetration())
                / (100 * 100 * 100);
    }

    public Double adjustedCostPerTon(EfficiencyRecord record, CostYearTable costYearTable) throws EmfException {
        int costYear = record.getCostYear();
        double factor = costYearTable.factor(costYear);
        return record.getCostPerTon() != null ? factor * record.getCostPerTon() : 0;
    }

    public double calculateEmissionReduction(EfficiencyRecord record, double invenControlEfficiency, double invenRulePenetration, double invenRuleEffectiveness, double invenAnnualEmissions) {
//        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
//                / (100 * 100 * 100);
        double effectiveReduction = effectiveReduction(record);

        //FIXME -- TEMPORARY - Ignore if inv item has an exisiting measure, just replace for now...
        return invenAnnualEmissions * effectiveReduction;
        
//        if (invenEffectiveReduction == 0.0) {
//            return invenAnnualEmissions * effectiveReduction;
//        }
//
//        if (invenEffectiveReduction < effectiveReduction) {
//            return  invenAnnualEmissions / invenEffectiveReduction * effectiveReduction;
//        }
//
//        return invenAnnualEmissions / invenControlEfficiency * invenEffectiveReduction;
    }

    public EfficiencyRecord[] effectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        return new EffectiveDateFilter(efficiencyRecords, inventoryYear).filter();
    }

    public EfficiencyRecord[] pollutantFilter(EfficiencyRecord[] efficiencyRecords, Pollutant pollutant) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getPollutant().equals(pollutant)) {
                records.add(efficiencyRecords[i]);
            }
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    public EfficiencyRecord[] minMaxEmisFilter(EfficiencyRecord[] efficiencyRecords, double invenAnnualEmission) {
        return new MinMaxEmissionFilter(efficiencyRecords, invenAnnualEmission).filter();
    }

    public EfficiencyRecord[] localeFilter(EfficiencyRecord[] efficiencyRecords, String fips) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            String locale = efficiencyRecords[i].getLocale();
            if (localeFilter.acceptLocale(locale, fips))
                records.add(efficiencyRecords[i]);
        }
        return localeFilter.closestRecords(records);
    }

    public EfficiencyRecord[] filterByConstraints(ControlStrategyConstraint constraint, CostYearTable costYearTable, EfficiencyRecord[] efficiencyRecords, 
            double invenControlEfficiency, double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        return new ConstraintFilter(constraint, costYearTable).filter(efficiencyRecords, invenControlEfficiency, 
                invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);
    }
}

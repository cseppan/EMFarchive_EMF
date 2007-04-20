package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.cost.controlstrategy.LocaleFilter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class RetrieveMaxEmsRedControlMeasure {

    private SccControlMeasuresMap map;

    private ControlStrategy controlStrategy;

    private ConstrainControlStrategyMeasureEfficiencyRecords constrainEfficiencyRecords;
    
    private LocaleFilter localeFilter;

    private CostYearTable costYearTable;
    
    private MaxEmsRedControlMeasure maxMeasure;
    
    private String fips;
    
    private RetrieveMaxEmsRedEfficiencyRecord retrieveEfficiencyRecord;
    
    public RetrieveMaxEmsRedControlMeasure(SccControlMeasuresMap map, CostYearTable costYearTable,
            ControlStrategy controlStrategy) {
        this.map = map;
        this.costYearTable = costYearTable;
        this.controlStrategy = controlStrategy;
        this.retrieveEfficiencyRecord = new RetrieveMaxEmsRedEfficiencyRecord(costYearTable);
        this.constrainEfficiencyRecords = new ConstrainControlStrategyMeasureEfficiencyRecords(controlStrategy, costYearTable);
//        ControlStrategyConstraint constraint = controlStrategy.getConstraint();
//        if (constraint != null) {
//            this.maxControlEfficiency = constraint.getMaxControlEfficiency();
//            this.minCostPerTon = constraint.getMinCostPerTon();
//        }
        this.localeFilter = new LocaleFilter();
    }

    //gets the best measure for the target pollutant
    public MaxEmsRedControlMeasure findBestMaxEmsRedMeasureForTargetPollutant(String scc, String fips, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        ControlMeasure[] controlMeasures = map.getControlMeasures(scc);
        this.fips = fips;
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord record = findBestEfficiencyRecordForTargetPollutant(controlMeasures[i], fips, 
                    controlStrategy.getInventoryYear(), invenControlEfficiency, 
                    invenRulePenetration, invenRuleEffectiveness, 
                    invenAnnualEmissions);
            if (record != null) {
                reduction.add(controlMeasures[i], record);
            }
        }

        return maxMeasure = reduction.findBestMeasure();
    }

    //gets the best measure for the target pollutant
    public MaxEmsRedControlMeasure findBestMaxEmsRedMeasure(String scc, String fips, Pollutant pollutant,
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        ControlMeasure[] controlMeasures = map.getControlMeasures(scc);
        this.fips = fips;
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord record = findBestEfficiencyRecordForTargetPollutant(controlMeasures[i], fips, 
                    controlStrategy.getInventoryYear(), invenControlEfficiency, 
                    invenRulePenetration, invenRuleEffectiveness, 
                    invenAnnualEmissions);
            if (record != null) {
                reduction.add(controlMeasures[i], record);
            }
        }
        /*MaxEmsRedControlMeasure */maxMeasure = reduction.findBestMeasure();
        
        //if pollutant is same as target pollutant, don't find best eff record again, since we already 
        //filtered by constraints, the below logic doesn't care about the constraints...
        if (pollutant.equals(controlStrategy.getTargetPollutant())) 
            return maxMeasure;
        
        if (maxMeasure == null) return null;
        ControlMeasure controlMeasure = maxMeasure.measure();

        reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        EfficiencyRecord record = findBestEfficiencyRecord(controlMeasure, fips, pollutant, controlStrategy.getInventoryYear());
        if (record != null) {
            reduction.add(controlMeasure, record);
        }

        return maxMeasure = reduction.findBestMeasure();
    }

    public MaxEmsRedControlMeasure getMaxEmsRedMeasureForCobenefitPollutant(Pollutant pollutant) throws EmfException {
        if (maxMeasure == null) return null;
        ControlMeasure controlMeasure = maxMeasure.measure();
        MaxEmsRedControlMeasureMap reduction = new MaxEmsRedControlMeasureMap(costYearTable);
        EfficiencyRecord record = findBestEfficiencyRecord(controlMeasure, fips, pollutant, controlStrategy.getInventoryYear());
        if (record != null) {
            reduction.add(controlMeasure, record);
        }

        return reduction.findBestMeasure();
    }

    private EfficiencyRecord findBestEfficiencyRecordForTargetPollutant(ControlMeasure measure, String fips, 
            int inventoryYear, double invenControlEfficiency, 
            double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        EfficiencyRecord[] efficiencyRecords = pollutantFilter(measure, controlStrategy.getTargetPollutant());
        efficiencyRecords = localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effectiveDateFilter(efficiencyRecords, inventoryYear);
        //apply this additional filter ONLY for TARGET POLLUTANTS...
        efficiencyRecords = filterByConstraints(efficiencyRecords, invenControlEfficiency, 
                invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);

        return retrieveEfficiencyRecord.findBestEfficiencyRecord(efficiencyRecords);
    }

    private EfficiencyRecord findBestEfficiencyRecord(ControlMeasure measure, String fips, Pollutant pollutant, int inventoryYear) throws EmfException {
        EfficiencyRecord[] efficiencyRecords = pollutantFilter(measure, pollutant);
        efficiencyRecords = localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effectiveDateFilter(efficiencyRecords, inventoryYear);

        return retrieveEfficiencyRecord.findBestEfficiencyRecord(efficiencyRecords);
    }

    private EfficiencyRecord[] effectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        return new EffectiveDateFilter(efficiencyRecords, inventoryYear).filter();
    }

    private EfficiencyRecord[] pollutantFilter(ControlMeasure measure, Pollutant pollutant) {
        List records = new ArrayList();
        EfficiencyRecord[] efficiencyRecords = measure.getEfficiencyRecords();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getPollutant().equals(pollutant))
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    private EfficiencyRecord[] localeFilter(EfficiencyRecord[] efficiencyRecords, String fips) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            String locale = efficiencyRecords[i].getLocale();
            if (localeFilter.acceptLocale(locale, fips))
                records.add(efficiencyRecords[i]);
        }
        return localeFilter.closestRecords(records);
    }

    private EfficiencyRecord[] filterByConstraints(EfficiencyRecord[] efficiencyRecords, double invenControlEfficiency, 
            double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        return constrainEfficiencyRecords.filter(efficiencyRecords, invenControlEfficiency, 
                invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);
    }
}
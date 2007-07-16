package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.EfficiencyRecordUtil;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class RetrieveBestMeasures {

    private ControlStrategy controlStrategy;

    private CostYearTable costYearTable;
    
    private EfficiencyRecordUtil effRecordUtil;
    
    public RetrieveBestMeasures(ControlStrategy controlStrategy, CostYearTable costYearTable) {
        this.controlStrategy = controlStrategy;
        this.costYearTable = costYearTable;
        this.effRecordUtil = new EfficiencyRecordUtil();
    }

    //get the best measures for the TARGET POLLUTANT
    public ControlMeasure[] findTargetPollutantBestMeasures(ControlMeasure[] controlMeasures, String fips, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
        //if the strat doesn't have any constraints then return all measures.
        //the constraints are the only filter that could eliminate a measure from a source
        if (controlStrategy.getConstraint() == null || !controlStrategy.getConstraint().hasConstraints()) return controlMeasures;
        List<ControlMeasure> measureEffRecordList = new ArrayList<ControlMeasure>();
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(controlMeasures[i].getEfficiencyRecords(), controlStrategy.getTargetPollutant());
            efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmissions);
            efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
            efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, controlStrategy.getInventoryYear());
            efficiencyRecords = effRecordUtil.filterByConstraints(controlStrategy.getConstraint(), costYearTable, efficiencyRecords, 
                invenControlEfficiency, invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);
            if (efficiencyRecords.length > 0) {
                measureEffRecordList.add(controlMeasures[i]);
            }
        }

        return (ControlMeasure[])measureEffRecordList.toArray();
    }
}
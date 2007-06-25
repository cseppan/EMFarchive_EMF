package gov.epa.emissions.framework.services.cost.analysis.applySuitableMeasuresInSeries;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.EfficiencyRecordUtil;
import gov.epa.emissions.framework.services.cost.analysis.common.RetrieveBestEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RetrieveBestMeasureEffRecords {

    private ControlStrategy controlStrategy;

    private CostYearTable costYearTable;
    
    private RetrieveBestEffRecord retrieveBestEffRecord;
    
    private EfficiencyRecordUtil effRecordUtil;
    
    public RetrieveBestMeasureEffRecords(ControlStrategy controlStrategy, CostYearTable costYearTable) {
        this.controlStrategy = controlStrategy;
        this.costYearTable = costYearTable;
        this.retrieveBestEffRecord = new RetrieveBestEffRecord(costYearTable);
        this.effRecordUtil = new EfficiencyRecordUtil();
    }

    //get the best measures map for TARGET POLLUTANT
    //Also, sort the returned list in order for processing the inventory
    //Sort by Least Cost first - we want to treat with the cheapest measure to
    //the most expensive....
    public List<BestMeasureEffRecord> findTargetPollutantBestMeasureEffRecords(ControlMeasure[] controlMeasures, String fips, 
            double invenControlEfficiency, double invenRulePenetration, 
            double invenRuleEffectiveness, double invenAnnualEmissions) throws EmfException {
//        BestMeasureEffRecordMap measureEffRecordMap = new BestMeasureEffRecordMap(costYearTable);
        List<BestMeasureEffRecord> measureEffRecordList = new ArrayList<BestMeasureEffRecord>();
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord record = findTargetPollutantBestEffRecord(controlMeasures[i], fips, 
                    controlStrategy.getInventoryYear(), invenControlEfficiency, 
                    invenRulePenetration, invenRuleEffectiveness, 
                    invenAnnualEmissions);
            if (record != null) {
//                measureEffRecordList.add(controlMeasures[i], record);
                measureEffRecordList.add(new BestMeasureEffRecord(controlMeasures[i], record, costYearTable));
            }
        }

        //Sort the list correctly, by cheapest to most expensive...
        sortMeasureEffRecord(measureEffRecordList);

        return measureEffRecordList;
    }

    //get the best measures map for COBENEFIT POLLUTANTs
    //also, sort the returned list in order for processing the inventory
    //  Sort by Least Cost first
    public List<BestMeasureEffRecord> findCobenefitPollutantBestMeasureEffRecords(ControlMeasure[] controlMeasures, String fips, Pollutant pollutant, 
            double invenAnnualEmissions) throws EmfException {
//        BestMeasureEffRecordMap measureEffRecordMap = new BestMeasureEffRecordMap(costYearTable);
        List<BestMeasureEffRecord> measureEffRecordList = new ArrayList<BestMeasureEffRecord>();
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord record = findCobenefitPollutantBestEffRecord(controlMeasures[i], fips, pollutant,
                    controlStrategy.getInventoryYear(), invenAnnualEmissions);
            if (record != null) {
//                measureEffRecordList.add(controlMeasures[i], record);
                measureEffRecordList.add(new BestMeasureEffRecord(controlMeasures[i], record, costYearTable));
            }
        }

        //Sort the list correctly, by cheapest to most expensive...
        sortMeasureEffRecord(measureEffRecordList);

        return measureEffRecordList;
    }

    private EfficiencyRecord findTargetPollutantBestEffRecord(ControlMeasure measure, String fips, 
            int inventoryYear, double invenControlEfficiency, 
            double invenRulePenetration, double invenRuleEffectiveness, 
            double invenAnnualEmissions) throws EmfException {
        EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(measure.getEfficiencyRecords(), controlStrategy.getTargetPollutant());
        efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmissions);
        efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, inventoryYear);
        //apply this additional filter ONLY for TARGET POLLUTANTS...
        efficiencyRecords = effRecordUtil.filterByConstraints(controlStrategy.getConstraint(), costYearTable, efficiencyRecords, 
                invenControlEfficiency, invenRulePenetration, invenRuleEffectiveness, 
                invenAnnualEmissions);

        return retrieveBestEffRecord.findBestEfficiencyRecord(efficiencyRecords);
    }

    private EfficiencyRecord findCobenefitPollutantBestEffRecord(ControlMeasure measure, String fips, Pollutant pollutant,
            int inventoryYear, double invenAnnualEmissions) throws EmfException {
        EfficiencyRecord[] efficiencyRecords = effRecordUtil.pollutantFilter(measure.getEfficiencyRecords(), pollutant);
        efficiencyRecords = effRecordUtil.minMaxEmisFilter(efficiencyRecords, invenAnnualEmissions);
        efficiencyRecords = effRecordUtil.localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effRecordUtil.effectiveDateFilter(efficiencyRecords, inventoryYear);

        return retrieveBestEffRecord.findBestEfficiencyRecord(efficiencyRecords);
    }
    
    private void sortMeasureEffRecord(List<BestMeasureEffRecord> measureEffRecordList) {
        //Sort the list correctly, by cheapest to most expensive...
        Collections.sort(measureEffRecordList, new Comparator<BestMeasureEffRecord>() {
            public int compare(BestMeasureEffRecord o1, BestMeasureEffRecord o2) {
                try {
                    return (int)((o1.adjustedCostPerTon() != null ? o1.adjustedCostPerTon() : 0) 
                            - (o2.adjustedCostPerTon() != null ? o2.adjustedCostPerTon() : 0));
                } catch (EmfException e) {
                    return 0;
                }
            }
        });
    }
}
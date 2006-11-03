package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CalMaxEmsRedEfficiencyRecord {

    private Map map;

    private EfficiencyRecordUtil efficiencyRecordUtil;

    private double tollerance;

    private CostYearTable costYearTable;

    public CalMaxEmsRedEfficiencyRecord(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.map = new HashMap();
        this.efficiencyRecordUtil = new EfficiencyRecordUtil();
        this.tollerance = 1e-5;
    }

    public void add(ControlMeasure measure, EfficiencyRecord record) {
        map.put(record, measure);
    }

    public MaxControlEffControlMeasure maxEmsReductionMeasure() throws EmfException {
        if (map.size() == 0)
            return null;// FIXME: do we have to warn or error

        Iterator iterator = map.keySet().iterator();
        EfficiencyRecord maxRecord = (EfficiencyRecord) iterator.next();

        while (iterator.hasNext()) {
            EfficiencyRecord record = (EfficiencyRecord) iterator.next();
            maxRecord = findMax(record, maxRecord);
        }

        ControlMeasure controlMeasure = (ControlMeasure) map.get(maxRecord);
        MaxControlEffControlMeasure maxMeasure = new MaxControlEffControlMeasure(controlMeasure, maxRecord, costYearTable);
        return maxMeasure;

    }

    private EfficiencyRecord findMax(EfficiencyRecord record, EfficiencyRecord maxRecord) throws EmfException {
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
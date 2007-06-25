package gov.epa.emissions.framework.services.cost.analysis.applySuitableMeasuresInSeries;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.HashMap;
import java.util.Map;

public class BestMeasureEffRecordMap {

    private Map<ControlMeasure,BestMeasureEffRecord> map;

    private CostYearTable costYearTable;
    
    public BestMeasureEffRecordMap(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.map = new HashMap<ControlMeasure,BestMeasureEffRecord>();
    }

    public void add(ControlMeasure measure, EfficiencyRecord record) {
        map.put(measure, new BestMeasureEffRecord(measure, record, costYearTable));
    }
}
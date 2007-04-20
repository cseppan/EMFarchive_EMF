package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MaxEmsRedControlMeasureMap {

    private Map map;

    private CostYearTable costYearTable;
    
    private RetrieveMaxEmsRedEfficiencyRecord retrieveMaxEmsRedEfficiencyRecord;

    public MaxEmsRedControlMeasureMap(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.retrieveMaxEmsRedEfficiencyRecord = new RetrieveMaxEmsRedEfficiencyRecord(costYearTable);
        this.map = new HashMap();
    }

    public void add(ControlMeasure measure, EfficiencyRecord record) {
        map.put(record, measure);
    }

    public MaxEmsRedControlMeasure findBestMeasure() throws EmfException {
        if (map.size() == 0)
            return null;// FIXME: do we have to warn or error

        Iterator iterator = map.keySet().iterator();
        EfficiencyRecord maxRecord = (EfficiencyRecord) iterator.next();

        while (iterator.hasNext()) {
            EfficiencyRecord record = (EfficiencyRecord) iterator.next();
            maxRecord = retrieveMaxEmsRedEfficiencyRecord.findBestEfficiencyRecord(record, maxRecord);
        }

        ControlMeasure controlMeasure = (ControlMeasure) map.get(maxRecord);
        MaxEmsRedControlMeasure maxMeasure = new MaxEmsRedControlMeasure(controlMeasure, maxRecord, costYearTable);
        return maxMeasure;

    }
}
package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.analysis.common.BestMeasureEffRecord;
import gov.epa.emissions.framework.services.cost.analysis.common.RetrieveBestEffRecord;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MaxEmsRedControlMeasureMap {

    private Map map;

    private CostYearTable costYearTable;
    
    private RetrieveBestEffRecord retrieveBestEffRecord;

    public MaxEmsRedControlMeasureMap(CostYearTable costYearTable) {
        this.costYearTable = costYearTable;
        this.retrieveBestEffRecord = new RetrieveBestEffRecord(costYearTable);
        this.map = new HashMap();
    }

    public void add(ControlMeasure measure, EfficiencyRecord record) {
        map.put(record, measure);
    }

    public BestMeasureEffRecord findBestMeasure() throws EmfException {
        if (map.size() == 0)
            return null;// FIXME: do we have to warn or error

        Iterator iterator = map.keySet().iterator();
        EfficiencyRecord maxRecord = (EfficiencyRecord) iterator.next();

        while (iterator.hasNext()) {
            EfficiencyRecord record = (EfficiencyRecord) iterator.next();
            maxRecord = retrieveBestEffRecord.findBestEfficiencyRecord(record, maxRecord);
        }

        ControlMeasure controlMeasure = (ControlMeasure) map.get(maxRecord);
        BestMeasureEffRecord maxMeasure = new BestMeasureEffRecord(controlMeasure, maxRecord, costYearTable);
        return maxMeasure;

    }
}
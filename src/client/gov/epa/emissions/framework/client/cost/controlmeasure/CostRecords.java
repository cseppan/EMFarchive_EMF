package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;

import java.util.Arrays;

public class CostRecords {
    private CostRecord[] costRecords;
    
    public CostRecords(CostRecord[] records) {
        this.costRecords = records;
    }
    
    public CostRecord[] sortByOrder() {
        Arrays.sort(costRecords, new CostRecordsComparator());
        return costRecords;
    }
}

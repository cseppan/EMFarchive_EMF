package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;

import java.util.Comparator;

public class CostRecordsComparator implements Comparator {
    public int compare(Object record1, Object record2) {
        float order1 = ((CostRecord) record1).getOrder();
        float order2 = ((CostRecord) record2).getOrder();

        //Note: this comparator imposes orderings that are inconsistent with equals
        if (order1 > order2)
            return 1;
        
        if(order1 == order2)
            return 0;
        
        return -1;
      }
}

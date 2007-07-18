package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;

import java.util.Comparator;

public class BestMeasureEffRecordComparatorByApplyOrderAndCost implements Comparator<BestMeasureEffRecord> {

    public int compare(BestMeasureEffRecord o1, BestMeasureEffRecord o2) {
        try {
            return (
                    signum((o1.measure().getApplyOrder() != null ? o1.measure().getApplyOrder() : 0)
                            - (o2.measure().getApplyOrder() != null ? o2.measure().getApplyOrder() : 0))
                    +
                    signum((o1.adjustedCostPerTon() != null ? o1.adjustedCostPerTon() : 0) 
                    - (o2.adjustedCostPerTon() != null ? o2.adjustedCostPerTon() : 0))
                    );
        } catch (EmfException e) {
            return 0;
        }
    }
    /**
     * Collapse number down to +1 0 or -1 depending on sign.
     * Typically used in compare routines to collapse a difference
     * of two longs to an int.
     *
     * @param diff usually represents the difference of two long.
     *
     * @return signum of diff, +1, 0 or -1.
     */
     public int signum(double diff) {
        if ( diff > 0 ) return 1;
        if ( diff < 0 ) return -1;
        return 0;
     }
}
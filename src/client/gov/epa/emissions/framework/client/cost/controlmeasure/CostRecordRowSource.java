package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.RowSource;

public class CostRecordRowSource implements RowSource {

    private CostRecord source;

    public CostRecordRowSource(CostRecord source) {
        this.source = source;
    }

    public Object[] values() {
        return new Object[] { source.getPollutant(), 
                new Integer(source.getCostYear()), new Float(source.getDiscountRate()),
                new Float(source.getA()), new Float(source.getB()) };
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {
        // NOTE no validation needed
    }

    public void setValueAt(int column, Object val) {
        // NOTE Auto-generated method stub
    }

}

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

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 1:
            source.setPollutant((String) val);
            break;
        case 2:
            source.setCostYear(Integer.parseInt(val.toString()));
            break;
        case 3:
            source.setDiscountRate(((Float) val).floatValue());
            break;
        case 4:
            source.setA(((Float) val).floatValue());
        case 5:
            source.setB(((Float) val).floatValue());
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {
        // NOTE no validation needed

    }

}

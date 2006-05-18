package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.RowSource;

public class CostRecordRowSource implements RowSource {

    private CostRecord source;

    private Boolean selected;

    public CostRecordRowSource(CostRecord source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, new Integer(source.getId()), source.getPollutant(), 
                new Integer(source.getCostYear()), new Float(source.getDiscountRate()),
                new Float(source.getA()), new Float(source.getB()) };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            break;
        case 2:
            source.setPollutant((String) val);
            break;
        case 3:
            source.setCostYear(Integer.parseInt(val.toString()));
            break;
        case 4:
            source.setDiscountRate(((Float) val).floatValue());
            break;
        case 5:
            source.setA(((Float) val).floatValue());
        case 6:
            source.setB(((Float) val).floatValue());
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        // NOTE no validation needed

    }

}

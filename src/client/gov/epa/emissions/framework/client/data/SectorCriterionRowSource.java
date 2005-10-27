package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.ui.RowSource;

public class SectorCriterionRowSource implements RowSource {

    private SectorCriteria criterion;

    private Boolean selected;

    public SectorCriterionRowSource(SectorCriteria criterion) {
        this.criterion = criterion;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, criterion.getType(), criterion.getCriteria() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            criterion.setType((String) val);
            break;
        case 2:
            criterion.setCriteria((String) val);
            break;

        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return criterion;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }
}
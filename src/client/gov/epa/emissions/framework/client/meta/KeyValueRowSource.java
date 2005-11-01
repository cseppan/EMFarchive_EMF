package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfKeyVal;
import gov.epa.emissions.framework.ui.RowSource;

public class KeyValueRowSource implements RowSource {

    private EmfKeyVal source;

    private Boolean selected;

    public KeyValueRowSource(EmfKeyVal source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getKeyword(), source.getValue() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setKeyword((String) val);
            break;
        case 2:
            source.setValue((String) val);
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
}
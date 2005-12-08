package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.ui.RowSource;

public class VersionRowSource implements RowSource {

    private Version source;

    private Boolean selected;

    public VersionRowSource(Version source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), new Integer(source.getVersion()), new Long(source.getBase()),
                source.getDate() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        default:
            throw new RuntimeException("cannot edit column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }
}
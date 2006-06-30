package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionRowSource implements RowSource {

    private Version source;

    private Boolean selected;

    private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    public VersionRowSource(Version source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), new Integer(source.getVersion()), new Long(source.getBase()),
                Boolean.valueOf(source.isFinalVersion()), format(source.getDate()) };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : dateFormat.format(date);
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

    public void validate(int rowNumber) {
        //FIXME: validate row source
        
    }
}
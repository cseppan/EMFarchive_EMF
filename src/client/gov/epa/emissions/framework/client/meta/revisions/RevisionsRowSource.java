package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RevisionsRowSource implements RowSource {

    private Revision revision;

    private DateFormat dateFormat;

    public RevisionsRowSource(Revision revision) {
        this.revision = revision;
        dateFormat = new SimpleDateFormat(EmfDateFormat.format());
    }

    public Object[] values() {
        return new Object[] { revision.getWhat(), revision.getWhy(), revision.getReferences(),
                new Long(revision.getVersion()), revision.getCreator().getName(), format(revision.getDate()) };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : dateFormat.format(date);
    }

    public Object source() {
        return revision;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}
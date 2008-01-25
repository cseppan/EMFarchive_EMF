package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.Date;

public class RevisionsRowSource implements RowSource {

    private Revision revision;

    public RevisionsRowSource(Revision revision) {
        this.revision = revision;
    }

    public Object[] values() {
        return new Object[] { revision.getWhat(), revision.getWhy(), revision.getReferences(),
                new Long(revision.getVersion()), revision.getCreator().getName(), format(revision.getDate()) };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public Object source() {
        return revision;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}
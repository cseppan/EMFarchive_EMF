package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesRowSource implements RowSource {

    private Note note;

    private DateFormat dateFormat;

    public NotesRowSource(Note source) {
        this.note = source;
        dateFormat = new SimpleDateFormat(EmfDateFormat.format());
    }

    public Object[] values() {
        return new Object[] { new Long(note.getId()), note.getName(), note.getNoteType().getType(),
                new Long(note.getVersion()), note.getCreator().getName(), format(note.getDate()), note.getReferences(),
                note.getDetails() };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : dateFormat.format(date);
    }

    public Object source() {
        return note;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}
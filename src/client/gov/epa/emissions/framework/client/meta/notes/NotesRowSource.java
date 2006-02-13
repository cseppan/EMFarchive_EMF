package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesRowSource implements RowSource {

    private Note note;

    private DateFormat dateFormat;

    public NotesRowSource(Note source) {
        this.note = source;
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    public Object[] values() {
        return new Object[] { note.getName(), note.getNoteType().getType(), note.getDetails(), note.getReferences(),
                note.getCreator().getName(), format(note.getDate()) };
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
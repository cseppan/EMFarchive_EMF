package gov.epa.emissions.framework.client.meta.notes;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import junit.framework.TestCase;

public class NotesRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        Note note = new Note();
        note.setName("note0");
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type0"));

        NotesRowSource source = new NotesRowSource(note);

        Object[] values = source.values();
        assertEquals(6, values.length);
        assertEquals(note.getName(), values[0]);
        assertEquals(note.getNoteType().getType(), values[1]);
        assertEquals(note.getDetails(), values[2]);
        assertEquals(note.getReferences(), values[3]);
        assertEquals(note.getCreator().getName(), values[4]);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(note.getDate()), values[5]);
    }

    public void testShouldTrackOriginalSource() {
        Note note = new Note();
        NotesRowSource rowSource = new NotesRowSource(note);

        assertEquals(note, rowSource.source());
    }
}

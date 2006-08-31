package gov.epa.emissions.framework.client.meta.notes;

import java.text.SimpleDateFormat;
import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import junit.framework.TestCase;

public class NotesRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        Note note = new Note();
        note.setId(45);
        note.setName("note0");
        note.setVersion(2);
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type0"));

        NotesRowSource source = new NotesRowSource(note);

        Object[] values = source.values();
        assertEquals(8, values.length);
        assertEquals(new Long(note.getId()), values[0]);
        assertEquals(note.getName(), values[1]);
        assertEquals(note.getNoteType().getType(), values[2]);
        assertEquals(note.getVersion(), ((Long)values[3]).longValue());
        assertEquals(note.getCreator().getName(), values[4]);

        SimpleDateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        assertEquals(dateFormat.format(note.getDate()), values[5]);

        assertEquals(note.getReferences(), values[6]);
        assertEquals(note.getDetails(), values[7]);
    }

    public void testShouldTrackOriginalSource() {
        Note note = new Note();
        NotesRowSource rowSource = new NotesRowSource(note);

        assertEquals(note, rowSource.source());
    }
}

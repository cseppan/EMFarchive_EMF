package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.ui.Row;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class NotesTableDataTest extends TestCase {

    private NotesTableData data;

    private Note note0;

    private Note note1;

    protected void setUp() {
        note0 = new Note();
        note0.setName("note0");
        note0.setCreator(new User());
        note0.setDate(new Date());
        note0.setNoteType(new NoteType("type0"));

        note1 = new Note();
        note1.setName("note1");
        note1.setCreator(new User());
        note1.setDate(new Date(note0.getDate().getTime() + 12000));
        note1.setNoteType(new NoteType("type1"));

        data = new NotesTableData(new Note[] { note0, note1 });
    }

    public void testShouldHaveSevenColumns() {
        String[] columns = data.columns();
        assertEquals(7, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Type", columns[2]);
        assertEquals("Details", columns[3]);
        assertEquals("References", columns[4]);
        assertEquals("Creator", columns[5]);
        assertEquals("Date", columns[6]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectColDateForDateColAndStringForAllOtherCols() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(String.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(Date.class, data.getColumnClass(6));
    }

    public void testExceptForSelectAllOtherColumnsShouldBeUneditable() {
        assertTrue("Select column should be editable", data.isEditable(0));
        for (int i = 1; i < 7; i++) {
            assertFalse("All cells (except Select) should be uneditable", data.isEditable(1));
        }
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(Boolean.FALSE, row.getValueAt(0));
        assertEquals(note0.getName(), row.getValueAt(1));
        assertEquals(note0.getNoteType().getType(), row.getValueAt(2));
        assertEquals(note0.getDetails(), row.getValueAt(3));
        assertEquals(note0.getReferences(), row.getValueAt(4));
        assertEquals(note0.getCreator().getName(), row.getValueAt(5));
        
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(note0.getDate()), row.getValueAt(6));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(note0, data.element(0));
        assertEquals(note1, data.element(1));
    }

    public void testShouldReturnSelectedNotes() {
        List rows = data.rows();
        Row row = (Row) rows.get(1);
        row.setValueAt(Boolean.TRUE, 0);

        Note[] notes = data.selected();
        assertEquals(1, notes.length);
        assertSame(note1, notes[0]);
    }

    public void testShouldAddRowOnAddingNewNote() {
        int count = data.rows().size();
        
        Note note = new Note();
        note.setName("note");
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type"));
        
        data.add(note);
        assertEquals(count + 1, data.rows().size());
    }
}

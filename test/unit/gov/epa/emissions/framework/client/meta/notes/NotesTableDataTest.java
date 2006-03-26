package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.DefaultChangeables;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.ui.Row;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.jmock.Mock;

public class NotesTableDataTest extends EmfMockObjectTestCase {

    private NotesTableData data;

    private Note note0;

    private Note note1;

    protected void setUp() {
        note0 = new Note();
        note0.setId(234);
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
        assertEquals(8, columns.length);
        assertEquals("Id", columns[0]);
        assertEquals("Summary", columns[1]);
        assertEquals("Type", columns[2]);
        assertEquals("Version", columns[3]);
        assertEquals("Creator", columns[4]);
        assertEquals("Date", columns[5]);
        assertEquals("References", columns[6]);
        assertEquals("Details", columns[7]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectColDateForDateColAndStringForAllOtherCols() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Long.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(3));
        assertEquals(Date.class, data.getColumnClass(4));
        assertEquals(String.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
    }

    public void testAllColumnsShouldBeUneditable() {
        for (int i = 0; i < 7; i++)
            assertFalse("All cells should be editable", data.isEditable(1));
    }

    public void testShouldReturnTheRowsCorrespondingToTotalCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals(new Long(note0.getId()), row.getValueAt(0));
        assertEquals(note0.getName(), row.getValueAt(1));
        assertEquals(note0.getNoteType().getType(), row.getValueAt(2));
        assertEquals(note0.getVersion(), ((Long)row.getValueAt(3)).longValue());
        assertEquals(note0.getCreator().getName(), row.getValueAt(4));

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        assertEquals(dateFormat.format(note0.getDate()), row.getValueAt(5));

        assertEquals(note0.getDetails(), row.getValueAt(6));
        assertEquals(note0.getReferences(), row.getValueAt(7));
    }

    public void testShouldReturnARowRepresentingANoteEntry() {
        assertEquals(note0, data.element(0));
        assertEquals(note1, data.element(1));
    }

    public void testShouldAddRowOnAddingNewNote() {
        int count = data.rows().size();

        Note note = new Note();
        note.setName("note");
        note.setCreator(new User());
        note.setDate(new Date());
        note.setNoteType(new NoteType("type"));

        Mock observer = mock(ChangeObserver.class);
        expects(observer, 2, "signalSaved");
        Changeables changeablesList = new DefaultChangeables((ChangeObserver) observer.proxy());
        
        data.observe(changeablesList);
        data.add(note);
        data.add(note);

        assertEquals(count + 2, data.rows().size());
        assertEquals(2, data.additions().length);
    }
}

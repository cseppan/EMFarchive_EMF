package gov.epa.emissions.framework.client.editor;

import java.util.List;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.Row;
import junit.framework.TestCase;

public class EditablePageTableDataTest extends TestCase {

    private EditablePageTableData data;

    private String[] cols;

    protected void setUp() {
        cols = new String[] { "col1", "col2", "col3" };
        data = new EditablePageTableData(cols, new Page());
    }

    public void testShouldHaveThreeColumns() {
        String[] columns = data.columns();
        assertEquals(4, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("col1", columns[1]);
        assertEquals("col2", columns[2]);
        assertEquals("col3", columns[3]);
    }

    public void testShouldHaveStringColumnClassForAllColumns() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        for (int i = 1; i < data.columns().length; i++)
            assertEquals(String.class, data.getColumnClass(i));
    }

    public void testAllColumnsShouldBeEditable() {
        for (int i = 0; i < data.columns().length; i++)
            assertTrue("All cells should be editable", data.isEditable(i));
    }

    public void testRowsShouldContainDataValuesOfRecords() {
        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        data = new EditablePageTableData(cols, page);

        List rows = data.rows();

        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());

        Row row1 = (Row) rows.get(0);
        assertEquals(record1.token(0), row1.getValueAt(1));
        assertEquals(record1.token(1), row1.getValueAt(2));

        Row row2 = (Row) rows.get(1);
        assertEquals(record2.token(0), row2.getValueAt(1));
        assertEquals(record2.token(1), row2.getValueAt(2));
    }

}

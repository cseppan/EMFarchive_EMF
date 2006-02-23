package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.client.data.viewer.ViewablePage;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class NonEditablePageDataTest extends TestCase {

    public void testShouldReturnStringAsColumnClassForAllColumns() {
        ViewablePage data = new ViewablePage(new Page(), null);

        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
    }

    public void testShouldDisplayAllColumns() {
        String[] cols = new String[] { "col1", "col2", "col3" };

        ViewablePage data = new ViewablePage(new Page(), cols);

        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals(cols[0], columns[0]);
        assertEquals(cols[1], columns[1]);
        assertEquals(cols[2], columns[2]);
    }

    public void testShouldMarkAllColumnsAsNotEditable() {
        String[] cols = new String[] { "col1", "col2", "col3" };

        ViewablePage data = new ViewablePage(new Page(), cols);

        assertFalse("All columns should not be editable", data.isEditable(0));
        assertFalse("All columns should not be editable", data.isEditable(1));
        assertFalse("All columns should not be editable", data.isEditable(2));
    }

    public void testShouldReturnTheRowsCorrespondingToRecordsInSpecifiedPage() {
        String[] cols = new String[] { "col1", "col2", "col3", "col4", "col5", "col6", "col7" };

        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        ViewablePage data = new ViewablePage(page, cols);

        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testRowsShouldContainDataValuesOfRecords() {
        String[] cols = new String[] { "col1", "col2", "col3", "col4", "col5", "col6", "col7" };

        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        ViewablePage data = new ViewablePage(page, cols);

        List rows = data.rows();

        Row row1 = (Row) rows.get(0);
        assertEquals(record1.token(0), row1.getValueAt(0));
        assertEquals(record1.token(1), row1.getValueAt(1));

        Row row2 = (Row) rows.get(1);
        assertEquals(record2.token(0), row2.getValueAt(0));
        assertEquals(record2.token(1), row2.getValueAt(1));
    }
}

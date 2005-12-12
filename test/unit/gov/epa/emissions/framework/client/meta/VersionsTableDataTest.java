package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class VersionsTableDataTest extends TestCase {

    private VersionsTableData data;

    private Version version0;

    private Version version1;

    protected void setUp() {
        version0 = new Version();
        version0.setName("ver0");
        version0.setPath("");
        version0.setVersion(0);
        version0.setDate(new Date());

        version1 = new Version();
        version1.setName("ver1");
        version1.setPath("0");
        version1.setVersion(1);
        version1.setDate(new Date(version0.getDate().getTime() + 12000));

        data = new VersionsTableData(new Version[] { version0, version1 });
    }

    public void testShouldHaveThreeColumns() {
        String[] columns = data.columns();
        assertEquals(5, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Name", columns[1]);
        assertEquals("Version", columns[2]);
        assertEquals("Base", columns[3]);
        assertEquals("Date", columns[4]);
    }

    public void testShouldReturnBooleanAsColumnClassForSelectColumnAndStringForAllOtherColumns() {
        assertEquals(Boolean.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(String.class, data.getColumnClass(2));
    }

    public void testExceptForSelectAllOtherColumnsShouldBeUneditable() {
        assertTrue("Select column should be editable", data.isEditable(0));
        assertFalse("All cells should be uneditable", data.isEditable(1));
        assertFalse("All cells should be uneditable", data.isEditable(2));
        assertFalse("All cells should be uneditable", data.isEditable(3));
        assertFalse("All cells should be uneditable", data.isEditable(4));
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
        assertEquals(version0.getName(), row.getValueAt(1));
        assertEquals(version0.getVersion(), ((Integer) row.getValueAt(2)).intValue());
        assertEquals(version0.getBase(), ((Long) row.getValueAt(3)).longValue());
        assertEquals(version0.getDate(), row.getValueAt(4));
    }

    public void testShouldReturnARowRepresentingAVersionEntry() {
        assertEquals(version0, data.element(0));
        assertEquals(version1, data.element(1));
    }

    public void testShouldReturnSelectedVersions() {
        List rows = data.rows();
        EditableRow row = (EditableRow) rows.get(1);
        row.setValueAt(Boolean.TRUE, 0);

        Version[] versions = data.selected();
        assertEquals(1, versions.length);
        assertSame(version1, versions[0]);
    }

    public void testShouldAddRowOnAddingNewVersion() {
        int count = data.rows().size();

        data.add(new Version());
        
        assertEquals(count + 1, data.rows().size());
    }
}

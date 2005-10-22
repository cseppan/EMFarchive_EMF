package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class DatasetTypesTableDataTest extends TestCase {

    private DatasetTypesTableData data;

    private DatasetType type1;

    private DatasetType type2;

    protected void setUp() {
        type1 = new DatasetType();
        type1.setName("name1");
        type1.setDescription("desc1");
        type1.setMinfiles(1);
        type1.setMaxfiles(3);
        type1.setMinColumns(3);
        type1.setMaxColumns(5);

        type2 = new DatasetType();
        type2.setName("name2");
        type2.setDescription("desc2");
        type2.setMinfiles(3);
        type2.setMaxfiles(3);
        type2.setMinColumns(5);
        type2.setMaxColumns(5);

        data = new DatasetTypesTableData(new DatasetType[] { type1, type2 });
    }

    public void testShouldHaveFiveColumns() {
        String[] columns = data.columns();
        assertEquals(6, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Description", columns[1]);
        assertEquals("Min Files", columns[2]);
        assertEquals("Max Files", columns[3]);
        assertEquals("Min Cols", columns[4]);
        assertEquals("Max Cols", columns[5]);
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be uneditable", data.isEditable(0));
        assertTrue("All cells should be uneditable", data.isEditable(1));
        assertTrue("All cells should be uneditable", data.isEditable(2));
        assertTrue("All cells should be uneditable", data.isEditable(3));
        assertTrue("All cells should be uneditable", data.isEditable(4));
    }

    public void testShouldReturnTheRowsCorrespondingToDatasetTypesCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("name1", row.getValueAt(0));
        assertEquals("desc1", row.getValueAt(1));
        assertEquals(new Integer(1), row.getValueAt(2));
        assertEquals(new Integer(3), row.getValueAt(3));
        assertEquals(new Integer(3), row.getValueAt(4));
        assertEquals(new Integer(5), row.getValueAt(5));
    }

    public void testShouldReturnARowRepresentingADatasetTypeEntry() {
        assertEquals(type1, data.element(0));
        assertEquals(type2, data.element(1));
    }
}

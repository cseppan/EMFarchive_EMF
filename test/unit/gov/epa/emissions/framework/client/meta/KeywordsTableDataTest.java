package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class KeywordsTableDataTest extends TestCase {

    private KeywordsTableData data;

    private KeyVal val1;

    private KeyVal val2;

    protected void setUp() {
        val1 = new KeyVal();
        val1.setId(1);
        val1.setKeyword("key1");
        val1.setValue("val1");

        val2 = new KeyVal();
        val2.setId(2);
        val2.setKeyword("key2");
        val2.setValue("val2");

        data = new KeywordsTableData(new KeyVal[] { val1, val2 });
    }

    public void testShouldHaveThreeColumns() {
        String[] columns = data.columns();
        assertEquals(3, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Keyword", columns[1]);
        assertEquals("Value", columns[2]);
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be editable", data.isEditable(0));
        assertTrue("All cells should be editable", data.isEditable(1));
        assertTrue("All cells should be editable", data.isEditable(2));
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
        assertEquals("key1", row.getValueAt(1));
        assertEquals("val1", row.getValueAt(2));
    }

    public void testShouldReturnARowRepresentingAKeyValEntry() {
        assertEquals(val1, data.element(0));
        assertEquals(val2, data.element(1));
    }

    public void testShouldRemoveRowOnRemove() {
        data.remove(val1);
        assertEquals(1, data.rows().size());

        data.remove(new KeyVal());
        assertEquals(1, data.rows().size());
    }

    public void testShouldRemoveSelectedOnRemove() {
        assertEquals(2, data.rows().size());

        data.setValueAt(Boolean.TRUE, 0, 0);
        data.removeSelected();

        assertEquals(1, data.rows().size());
    }

    public void testShouldAddEntryOnAdd() {
        KeyVal val = new KeyVal();
        val.setId(3);

        data.add(val);

        assertEquals(3, data.rows().size());
    }

    public void testShouldAddBlankEntry() {
        data.addBlankRow();

        List rows = data.rows();
        assertEquals(3, rows.size());
        Row blankRow = (Row) rows.get(2);
        KeyVal blankSource = ((KeyVal) blankRow.source());
        assertEquals("", blankSource.getKeyword());
        assertEquals("", blankSource.getValue());
    }

    public void testShouldReturnCurrentlyHeldKeyVal() {
        KeyVal criterion = new KeyVal();
        criterion.setId(3);

        data.add(criterion);

        KeyVal[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals(val1, sources[0]);
        assertEquals(val2, sources[1]);
        assertEquals(criterion, sources[2]);
    }

}

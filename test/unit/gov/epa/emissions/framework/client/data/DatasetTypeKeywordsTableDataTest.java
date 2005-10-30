package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class DatasetTypeKeywordsTableDataTest extends TestCase {

    private DatasetTypeKeywordsTableData data;

    protected void setUp() {
        data = new DatasetTypeKeywordsTableData(new String[] { "keyword1", "keyword2" });
    }

    public void testShouldHaveTwoColumn() {
        String[] columns = data.columns();
        assertEquals(2, columns.length);
        assertEquals("Select", columns[0]);
        assertEquals("Keyword", columns[1]);
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be editable", data.isEditable(0));
        assertTrue("All cells should be editable", data.isEditable(1));
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
        assertEquals("keyword1", row.getValueAt(1));
    }

    public void testShouldReturnARowRepresentingAKeywordEntry() {
        assertEquals("keyword1", data.element(0));
        assertEquals("keyword2", data.element(1));
    }

    public void testShouldRemoveKeywordOnRemove() {
        data.remove("keyword1");
        assertEquals(1, data.rows().size());

        data.remove("non-existent keyword");
        assertEquals(1, data.rows().size());
    }

    public void testShouldAddKeywordOnAdd() {
        data.add("keyword3");

        assertEquals(3, data.rows().size());
    }

    public void testShouldReturnCurrentlyHeldKeyword() {
        data.add("keyword3");

        String[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals("keyword1", sources[0]);
        assertEquals("keyword2", sources[1]);
        assertEquals("keyword3", sources[2]);
    }

}

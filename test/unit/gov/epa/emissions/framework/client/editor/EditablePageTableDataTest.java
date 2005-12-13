package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class EditablePageTableDataTest extends TestCase {

    private EditablePageTableData data;

    private String[] cols;

    private VersionedRecord record1;

    private VersionedRecord record2;

    private Page page;

    private int datasetId;

    private int version;

    protected void setUp() {
        page = new Page();

        record1 = new VersionedRecord();
        record1.setTokens(new String[] { "1", "2", "3" });
        page.add(record1);

        record2 = new VersionedRecord();
        record2.setTokens(new String[] { "11", "12", "13" });
        page.add(record2);

        cols = new String[] { "col1", "col2", "col3" };
        datasetId = 2;
        version = 34;
        data = new EditablePageTableData(datasetId, version, page, cols);
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

    public void testRowsShouldContainDataValuesOfRecord() {
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

    public void testShouldReturnARowRepresentingARecordEntry() {
        assertEquals(record1, data.element(0));
        assertEquals(record2, data.element(1));
    }

    public void testShouldRemoveRowOnRemove() {
        data.remove(record1);
        assertEquals(1, data.rows().size());

        data.remove(new VersionedRecord());
        assertEquals(1, data.rows().size());
    }

    public void testShouldRemoveSelectedOnRemove() {
        assertEquals(2, data.rows().size());

        data.setValueAt(Boolean.TRUE, 0, 0);
        data.removeSelected();

        assertEquals(1, data.rows().size());
    }

    public void testShouldAddEntryOnAdd() {
        data.addBlankRow();

        assertEquals(3, data.rows().size());
    }

    public void testShouldAddBlankEntry() {
        data.addBlankRow();

        List rows = data.rows();
        assertEquals(3, rows.size());
        Row blankRow = (Row) rows.get(2);

        VersionedRecord blankRecord = (VersionedRecord) blankRow.source();
        assertEquals(datasetId, blankRecord.getDatasetId());
        assertEquals(version, blankRecord.getVersion());
        assertEquals("", blankRecord.getDeleteVersions());
        assertEquals(cols.length, blankRecord.tokens().size());
        for (int i = 0; i < cols.length; i++)
            assertEquals("", blankRecord.token(i));
    }

    public void testShouldReturnCurrentlyHeldRecords() {
        data.addBlankRow();

        VersionedRecord[] sources = data.sources();
        assertEquals(3, sources.length);
        assertEquals(record1, sources[0]);
        assertEquals(record2, sources[1]);
        assertEquals(datasetId, sources[2].getDatasetId());
    }
}

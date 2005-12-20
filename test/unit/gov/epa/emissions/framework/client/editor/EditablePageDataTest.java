package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class EditablePageDataTest extends TestCase {

    private EditablePageData data;

    private String[] cols;

    private VersionedRecord record1;

    private VersionedRecord record2;

    private Page page;

    private int datasetId;

    private Version version;

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
        version = new Version();
        version.setVersion(34);
        data = new EditablePageData(datasetId, version, page, cols);
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

        ChangeSet changeset = data.changeset();
        assertEquals(0, changeset.getNewRecords().length);
        assertEquals(1, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
        assertSame(record1, changeset.getDeletedRecords()[0]);
    }

    public void testShouldAddPreExistingRecordsThatAreModifiedToChangeSet() {
        data.setValueAt("modified-1", 0, 1);
        data.setValueAt("modified-2", 0, 2);
        data.setValueAt("modified-12", 1, 2);

        assertEquals(2, data.rows().size());

        ChangeSet changeset = data.changeset();
        assertEquals(0, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(2, changeset.getUpdatedRecords().length);

        VersionedRecord updated1 = changeset.getUpdatedRecords()[0];
        assertEquals("modified-1", updated1.token(0));
        assertEquals("modified-2", updated1.token(1));

        VersionedRecord updated2 = changeset.getUpdatedRecords()[1];
        assertEquals("modified-12", updated2.token(1));
    }

    public void testModificationsToBlankRowShouldNotImpactChangeSet() {
        data.addBlankRow();
        assertEquals(3, data.rows().size());

        // modify newly added (blank) row
        data.setValueAt("31", 2, 1);
        data.setValueAt("32", 2, 2);

        ChangeSet changeset = data.changeset();
        assertEquals(1, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);

        VersionedRecord newRecord = changeset.getNewRecords()[0];
        assertEquals("31", newRecord.token(0));
        assertEquals("32", newRecord.token(1));
    }

    public void testChangeSetShouldIgnoreChangesToSelectCol() {
        data.addBlankRow();
        data.addBlankRow();

        data.setValueAt(Boolean.TRUE, 1, 0);
        data.setValueAt(Boolean.TRUE, 3, 0);
        data.setValueAt(Boolean.FALSE, 1, 0);

        assertEquals(4, data.rows().size());

        ChangeSet changeset = data.changeset();
        assertEquals(2, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
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
        assertEquals(version.getVersion(), blankRecord.getVersion());
        assertEquals("", blankRecord.getDeleteVersions());
        assertEquals(cols.length, blankRecord.tokens().size());
        for (int i = 0; i < cols.length - 1; i++)
            assertEquals("", blankRecord.token(i));
        assertEquals("!", blankRecord.token(cols.length - 1));

        ChangeSet changeset = data.changeset();
        assertEquals(1, changeset.getNewRecords().length);
        assertEquals(0, changeset.getDeletedRecords().length);
        assertEquals(0, changeset.getUpdatedRecords().length);
        assertSame(blankRecord, changeset.getNewRecords()[0]);
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

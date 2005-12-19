package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.VersionedRecord;

import org.jmock.cglib.MockObjectTestCase;

public class EditablePageDataRowSourceTest extends MockObjectTestCase {

    private VersionedRecord record;

    private EditablePageDataRowSource row;

    protected void setUp() {
        record = new VersionedRecord();
        String[] values = { "1", "2", "3" };
        record.setTokens(values);

        row = new EditablePageDataRowSource(record);
    }

    public void testShouldReplaceValueAtSpecificPostionWhenModified() {
        row.setValueAt(1, "modified-1");// 1 is first col. 0 - reserved for 'Select'

        VersionedRecord result = (VersionedRecord) row.source();
        assertEquals("modified-1", result.token(0));
    }

    public void testShouldMarkSelectedWhenFirstColumnIsSetToTrue() {
        row.setValueAt(0, Boolean.TRUE);

        assertTrue(row.isSelected());
    }

    public void testShouldReturnSelectedAlongWithTokensAsValues() {
        Object[] results = row.values();

        assertEquals(4, results.length);
        assertEquals(Boolean.FALSE, results[0]);
        assertEquals(record.token(0), results[1]);
        assertEquals(record.token(1), results[2]);
        assertEquals(record.token(2), results[3]);
    }
}

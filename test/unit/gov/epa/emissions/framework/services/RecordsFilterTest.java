package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.framework.services.editor.RecordsFilter;
import junit.framework.TestCase;

public class RecordsFilterTest extends TestCase {

    public void testShouldAddRecordsToPageWhenChangeSetContainsNewRecords() {
        RecordsFilter filter = new RecordsFilter();

        VersionedRecord record1 = new VersionedRecord(1);
        VersionedRecord record2 = new VersionedRecord(2);
        VersionedRecord[] records = { record1, record2 };
        Page page = new Page();
        page.setRecords(records);

        ChangeSet changeset = new ChangeSet();
        VersionedRecord newRecord = new VersionedRecord(3);
        changeset.addNew(newRecord);

        Page resultsPage = filter.filter(page, changeset);
        VersionedRecord[] results = resultsPage.getRecords();

        assertEquals(3, results.length);
        assertSame(records[0], results[0]);
        assertSame(records[1], results[1]);
        assertSame(newRecord, results[2]);
    }

    public void testShouldAddRecordsWhenChangeSetContainsNewRecords() {
        RecordsFilter filter = new RecordsFilter();

        VersionedRecord record1 = new VersionedRecord(1);
        VersionedRecord record2 = new VersionedRecord(2);
        VersionedRecord[] records = { record1, record2 };

        ChangeSet changeset = new ChangeSet();
        VersionedRecord newRecord = new VersionedRecord(3);
        changeset.addNew(newRecord);

        VersionedRecord[] results = filter.filter(records, changeset);

        assertEquals(3, results.length);
        assertSame(records[0], results[0]);
        assertSame(records[1], results[1]);
        assertSame(newRecord, results[2]);
    }

    public void testShouldRemoveRecordsWhenChangeSetContainsDeletedRecords() {
        RecordsFilter filter = new RecordsFilter();

        VersionedRecord record1 = new VersionedRecord(1);
        VersionedRecord record2 = new VersionedRecord(2);
        VersionedRecord[] records = { record1, record2 };

        ChangeSet changeset = new ChangeSet();
        VersionedRecord deletedRecord = new VersionedRecord(2);
        changeset.addDeleted(deletedRecord);

        VersionedRecord[] results = filter.filter(records, changeset);

        assertEquals(1, results.length);
        assertSame(records[0], results[0]);
    }

    public void testShouldRemoveOldRecordAndAddNewRecordWhenChangeSetContainsUpdatedRecord() {
        RecordsFilter filter = new RecordsFilter();

        VersionedRecord record1 = new VersionedRecord(1);
        VersionedRecord record2 = new VersionedRecord(2);
        VersionedRecord record3 = new VersionedRecord(3);
        VersionedRecord[] records = { record1, record2, record3 };

        ChangeSet changeset = new ChangeSet();
        VersionedRecord record4 = new VersionedRecord(2);
        changeset.addUpdated(record4);

        VersionedRecord[] results = filter.filter(records, changeset);

        assertEquals(3, results.length);
        assertSame(records[0], results[0]);
        assertSame(record4, results[1]);
        assertSame(records[2], results[2]);
    }
}

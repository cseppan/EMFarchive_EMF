package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Date;
import java.util.Random;

public abstract class DataEditorServiceTestCase extends ServicesTestCase {

    private DataEditorService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    protected void setUpService(DataEditorService service) throws Exception {
        this.service = service;
        datasource = emissions();

        dataset = new EmfDataset();
        table = "test";
        dataset.setName(table);
        setTestValues(dataset);

        File file = new File("test/data/orl/nc", "midsize-nonpoint.txt");
        ORLNonPointImporter importer = new ORLNonPointImporter(file, dataset, datasource, dataTypes());

        importer.run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setDatasetid(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void tearDown() throws Exception {
        DbUpdate dbUpdate = new DbUpdate(datasource.getConnection());
        dbUpdate.dropTable(datasource.getName(), dataset.getName());

        DataModifier modifier = datasource.dataModifier();
        modifier.dropAll("versions");
        super.tearDown();
    }

    public void testShouldReturnExactlyTenPages() throws EmfException {
        EditToken token = editToken();
        assertEquals(10, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(20, page.count());
        VersionedRecord[] records = page.getRecords();
        assertEquals(page.count(), records.length);
        for (int i = 0; i < records.length; i++) {
            assertEquals(token.datasetId(), records[i].getDatasetId());
            assertEquals(0, records[i].getVersion());
        }

        int recordId = records[0].getRecordId();
        for (int i = 1; i < records.length; i++) {
            assertEquals(++recordId, records[i].getRecordId());
        }
    }

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        int numberOfRecords = service.getTotalRecords(editToken());
        assertTrue(numberOfRecords >= 1);
    }

    public void testShouldReturnAtLeastOnePage() throws EmfException {
        int numberOfPages = service.getPageCount(editToken());
        assertTrue(numberOfPages >= 1);
    }

    private EditToken editToken() {
        Version version = new Version();
        version.setDatasetId(dataset.getDatasetid());
        version.setVersion(0);
        version.setName("v0");
        version.setPath("");
        version.markFinal();

        return new EditToken(version, dataset.getName());
    }

    /**
     * This test gets a page using an integer record ID. The resulting collection that is acquired from the page should
     * contain the record with record id that was supplied.
     */
    public void testShouldReturnOnlyOnePage() throws EmfException {
        EditToken token = editToken();
        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(token, numberOfRecords - 1);
        VersionedRecord[] allRecs = page.getRecords();
        boolean found = false;

        for (int i = 0; i < allRecs.length; i++) {
            if (allRecs[i].getRecordId() == numberOfRecords - 1) {
                found = true;
            }
        }
        assertTrue(found);
    }

    public void testShouldReturnNoPage() throws EmfException {
        EditToken token = editToken();
        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(token, numberOfRecords + 1);
        VersionedRecord[] allRecs = page.getRecords();
        boolean found = false;

        for (int i = 0; i < allRecs.length; i++) {
            if (allRecs[i].getRecordId() == numberOfRecords + 1) {
                found = true;
            }
        }
        assertTrue(!found);
    }

    public void testShouldHaveVersionZeroAfterDatasetImport() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        assertNotNull("Should return versions of imported dataset", versions);
        assertEquals(1, versions.length);

        Version versionZero = versions[0];
        assertEquals(0, versionZero.getVersion());
        assertEquals(dataset.getDatasetid(), versionZero.getDatasetId());
    }

    public void testShouldDeriveVersionFromAFinalVersion() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version derived = service.derive(versionZero, "v 1");

        assertNotNull("Should be able to derive from a Final version", derived);
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals(1, derived.getVersion());
        assertEquals("0", derived.getPath());
        assertFalse("Derived version should be non-final", derived.isFinalVersion());
    }

    public void testShouldBeAbleToMarkADerivedVersionAsFinal() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version derived = service.derive(versionZero, "v 1");
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals("v 1", derived.getName());

        Version finalVersion = service.markFinal(derived);

        assertNotNull("Should be able to mark a 'derived' as a Final version", derived);
        assertEquals(derived.getDatasetId(), finalVersion.getDatasetId());
        assertEquals(derived.getVersion(), finalVersion.getVersion());
        assertEquals("0", finalVersion.getPath());
        assertTrue("Derived version should be final on being marked 'final'", finalVersion.isFinalVersion());
    }

    public void testChangeSetWithNewRecordsResultsInNewVersion() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record7);

        EditToken token = new EditToken(versionOne, table);
        service.submit(token, changeset, 1);
        service.save(token);

        VersionedRecordsReader reader = new VersionedRecordsReader(datasource);
        int versionZeroRecordsCount = reader.fetchAll(versionZero, dataset.getName()).length;

        VersionedRecord[] records = reader.fetchAll(versionOne, dataset.getName());
        assertEquals(versionZeroRecordsCount + 2, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++) {
            assertEquals(++init, records[i].getRecordId());
        }
    }

    public void testShouldBeAbleToSubmitMultipleChangeSetsForSameVersion() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        changeset2.setVersion(versionOne);
        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId((int) dataset.getDatasetid());
        changeset2.addNew(record7);
        service.submit(token, changeset2, 1);

        service.save(token);

        VersionedRecordsReader reader = new VersionedRecordsReader(datasource);
        int versionZeroRecordsCount = reader.fetchAll(versionZero, dataset.getName()).length;

        VersionedRecord[] records = reader.fetchAll(versionOne, dataset.getName());
        assertEquals(versionZeroRecordsCount + 2, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++)
            assertEquals(++init, records[i].getRecordId());
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        service.discard(token);

        VersionedRecordsReader reader = new VersionedRecordsReader(datasource);

        VersionedRecord[] versionZeroRecords = reader.fetchAll(versionZero, dataset.getName());
        VersionedRecord[] versionOneRecords = reader.fetchAll(versionOne, dataset.getName());

        assertEquals(versionZeroRecords.length, versionOneRecords.length);
        for (int i = 0; i < versionOneRecords.length; i++)
            assertEquals(versionZeroRecords[i].getRecordId(), versionOneRecords[i].getRecordId());
    }

    public void testShouldAddNewRecordsInChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);
        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(10, service.getPageCount(token));
        service.getPage(token, 6);
        service.getPage(token, 3);
        service.getPage(token, 9);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count() + 1, records.length);

        VersionedRecord[] page1Records = page.getRecords();
        for (int i = 0; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());
    }

    public void testShouldApplyChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);
        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record6);

        VersionedRecord[] page1Records = page.getRecords();
        changeset.addDeleted(page1Records[2]);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(10, service.getPageCount(token));
        service.getPage(token, 5);
        service.getPage(token, 6);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count(), records.length);

        assertEquals(page1Records[0].getRecordId(), records[0].getRecordId());
        assertEquals(page1Records[1].getRecordId(), records[1].getRecordId());
        // record 2 deleted from Page 1
        for (int i = 3; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i - 1].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());
    }

    public void testShouldApplyChangeSetToMultiplePages() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId((int) dataset.getDatasetid());
        page1ChangeSet.addNew(record6);
        Page page1 = service.getPage(token, 1);
        service.submit(token, page1ChangeSet, 1);

        // random page browsing
        assertEquals(10, service.getPageCount(token));
        service.getPage(token, 5);
        service.getPage(token, 6);

        // page 4 changes
        Page page4 = service.getPage(token, 4);
        VersionedRecord[] page4Records = page4.getRecords();
        ChangeSet page4ChangeSet = new ChangeSet();
        page4ChangeSet.setVersion(versionOne);
        page4ChangeSet.addDeleted(page4Records[2]);
        service.submit(token, page4ChangeSet, 4);

        Page page1AfterChanges = service.getPage(token, 1);
        assertEquals(page1.count() + 1, page1AfterChanges.count());

        Page page4AfterChanges = service.getPage(token, 4);
        assertEquals(page4.count() - 1, page4AfterChanges.count());
    }

    public void testShouldSaveChangeSetAndRegeneratePagesAfterSave() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = new EditToken(versionOne, table);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(versionOne);
        VersionedRecord newRecord = new VersionedRecord(10);
        newRecord.setDatasetId((int) dataset.getDatasetid());
        page1ChangeSet.addNew(newRecord);
        service.submit(token, page1ChangeSet, 1);

        // random page browsing
        service.getPage(token, 5);
        service.getPage(token, 6);

        int recordsBeforeSave = service.getTotalRecords(token);
        service.save(token);

        assertEquals(recordsBeforeSave + 1, service.getTotalRecords(token));
    }

    public void testShouldMarkFinalAndRetrieveVersions() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version derived = service.derive(versionZero, "v 1");
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals("v 1", derived.getName());

        Version finalVersion = service.markFinal(derived);

        assertNotNull("Should be able to mark a 'derived' as a Final version", derived);
        assertEquals(derived.getDatasetId(), finalVersion.getDatasetId());
        assertEquals(derived.getVersion(), finalVersion.getVersion());
        assertEquals("0", finalVersion.getPath());
        assertTrue("Derived version should be final on being marked 'final'", finalVersion.isFinalVersion());
    }
}

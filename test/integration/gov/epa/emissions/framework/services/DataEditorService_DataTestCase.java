package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Date;
import java.util.Random;

public abstract class DataEditorService_DataTestCase extends ServicesTestCase {

    private DataEditorService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private EditToken token;

    protected void setUpService(DataEditorService service) throws Exception {
        this.service = service;
        datasource = emissions();

        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        dataset.setName(table);
        setTestValues(dataset);

        doImport();

        token = editToken();
        service.openSession(token, 4);
    }

    private void doImport() throws ImporterException {
        File file = new File("test/data/orl/nc", "very-small-nonpoint.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(0);
        ORLNonPointImporter importer = new ORLNonPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, dbServer(), dataTypes(), formatFactory);
        new VersionedImporter(importer, dataset, dbServer()).run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setDatasetid(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void doTearDown() throws Exception {
        service.closeSession(token);

        DbUpdate dbUpdate = new PostgresDbUpdate(datasource.getConnection());
        dbUpdate.dropTable(datasource.getName(), dataset.getName());

        DataModifier modifier = datasource.dataModifier();
        modifier.dropAll("versions");
    }

    public void testShouldReturnExactlyTenPages() throws EmfException {
        assertEquals(5, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(4, page.count());
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
        Version version = versionZero();
        return editToken(version);
    }

    private EditToken editToken(Version version) {
        return editToken(version, dataset.getName());
    }

    private EditToken editToken(Version version, String table) {
        EditToken result = new EditToken(version, table);

        return result;
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getDatasetid(), 0, session);
    }

    /**
     * This test gets a page using an integer record ID. The resulting collection that is acquired from the page should
     * contain the record with record id that was supplied.
     */
    public void testShouldReturnOnlyOnePage() throws EmfException {
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

    public void FIXME_testChangeSetWithNewRecordsResultsInNewVersion() throws Exception {
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

        EditToken token = editToken(versionOne, table);
        EditToken locked = service.openSession(token, 4);

        service.submit(locked, changeset, 1);
        service.save(locked);

        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        int versionZeroRecordsCount = reader.fetchAll(versionZero, dataset.getName(), session).length;

        VersionedRecord[] records = reader.fetchAll(versionOne, dataset.getName(), session);
        assertEquals(versionZeroRecordsCount + 2, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++)
            assertEquals(++init, records[i].getRecordId());

        service.closeSession(locked);
    }

    public void FIXME_testShouldBeAbleToSubmitMultipleChangeSetsForSameVersion() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = editToken(versionOne, table);
        EditToken locked = service.openSession(token, 4);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset1.addNew(record6);
        service.submit(locked, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        changeset2.setVersion(versionOne);
        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId((int) dataset.getDatasetid());
        changeset2.addNew(record7);
        service.submit(locked, changeset2, 1);

        service.save(locked);

        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        int versionZeroRecordsCount = reader.fetchAll(versionZero, dataset.getName(), session).length;

        VersionedRecord[] records = reader.fetchAll(versionOne, dataset.getName(), session);
        assertEquals(versionZeroRecordsCount + 2, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++)
            assertEquals(++init, records[i].getRecordId());

        service.closeSession(locked);
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = editToken(versionOne, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        service.discard(token);

        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);

        VersionedRecord[] versionZeroRecords = reader.fetchAll(versionZero, dataset.getName(), session);
        VersionedRecord[] versionOneRecords = reader.fetchAll(versionOne, dataset.getName(), session);

        assertEquals(versionZeroRecords.length, versionOneRecords.length);
        for (int i = 0; i < versionOneRecords.length; i++)
            assertEquals(versionZeroRecords[i].getRecordId(), versionOneRecords[i].getRecordId());
    }

    public void testShouldAddNewRecordsInChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = editToken(versionOne, table);
        EditToken locked = service.openSession(token, 4);
        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(5, service.getPageCount(token));
        service.getPage(token, 2);
        service.getPage(token, 1);
        service.getPage(token, 4);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count() + 1, records.length);

        VersionedRecord[] page1Records = page.getRecords();
        for (int i = 0; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());

        service.closeSession(locked);
    }

    public void testShouldApplyChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = editToken(versionOne, table);
        EditToken locked = service.openSession(token, 4);

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
        assertEquals(5, service.getPageCount(token));
        service.getPage(token, 4);
        service.getPage(token, 2);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count(), records.length);

        assertEquals(page1Records[0].getRecordId(), records[0].getRecordId());
        assertEquals(page1Records[1].getRecordId(), records[1].getRecordId());
        // record 2 deleted from Page 1
        for (int i = 3; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i - 1].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());

        service.closeSession(locked);
    }

    public void testShouldApplyChangeSetToMultiplePages() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version versionOne = service.derive(versionZero, "v 1");

        EditToken token = editToken(versionOne, table);
        service.openSession(token, 4);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId((int) dataset.getDatasetid());
        page1ChangeSet.addNew(record6);
        Page page1 = service.getPage(token, 1);
        service.submit(token, page1ChangeSet, 1);

        // random page browsing
        assertEquals(5, service.getPageCount(token));
        service.getPage(token, 3);
        service.getPage(token, 1);

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

        EditToken token = editToken(versionOne, table);
        service.openSession(token, 4);

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

}

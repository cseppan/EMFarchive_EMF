package gov.epa.emissions.framework.client.data;

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
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
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
        dataset.setDatasetid(Math.abs(new Random().nextInt()));

        ORLNonPointImporter importer = new ORLNonPointImporter(dataset, datasource, dataTypes());

        importer.preCondition(new File("test/data/orl/nc"), "small-nonpoint.txt");
        importer.run(dataset);
    }

    protected void tearDown() throws Exception {
        DbUpdate dbUpdate = new DbUpdate(datasource.getConnection());
        dbUpdate.dropTable(datasource.getName(), dataset.getName());

        DataModifier modifier = datasource.dataModifier();
        modifier.dropAll("versions");

        super.tearDown();
    }

    public void testShouldReturnExactlyOnePage() throws EmfException {
        Page page = service.getPage(dataset.getName(), 1);
        assertTrue(page != null);
    }

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        EditToken token = new EditToken(dataset.getDatasetid(), 0, dataset.getName());

        int numberOfRecords = service.getTotalRecords(token);
        assertTrue(numberOfRecords >= 1);
    }

    public void testShouldReturnAtLeastOnePage() throws EmfException {
        int numberOfPages = service.getPageCount(dataset.getName());
        assertTrue(numberOfPages >= 1);
    }

    /**
     * This test gets a page using an integer record ID. The resulting
     * collection that is acquired from the page should contain the record with
     * record id that was supplied.
     */
    public void testShouldReturnOnlyOnePage() throws EmfException {
        EditToken token = new EditToken(dataset.getDatasetid(), 0, dataset.getName());

        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(dataset.getName(), numberOfRecords - 1);
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
        EditToken token = new EditToken(dataset.getDatasetid(), 0, dataset.getName());

        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(dataset.getName(), numberOfRecords + 1);
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
        Version derived = service.derive(versionZero);

        assertNotNull("Should be able to derive from a Final version", derived);
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals(1, derived.getVersion());
        assertEquals("0", derived.getPath());
        assertFalse("Derived version should be non-final", derived.isFinalVersion());
    }

    public void testShouldBeAbleToMarkADerivedVersionAsFinal() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        Version versionZero = versions[0];
        Version derived = service.derive(versionZero);

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
        Version versionOne = service.derive(versionZero);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId((int) dataset.getDatasetid());
        changeset.addNew(record7);

        EditToken token = new EditToken(versionOne, table);
        service.submit(token, changeset);

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
        Version versionOne = service.derive(versionZero);

        EditToken token = new EditToken(versionOne, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(versionOne);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId((int) dataset.getDatasetid());
        changeset1.addNew(record6);

        service.submit(token, changeset1);

        ChangeSet changeset2 = new ChangeSet();
        changeset2.setVersion(versionOne);
        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId((int) dataset.getDatasetid());
        changeset2.addNew(record7);

        service.submit(token, changeset2);

        VersionedRecordsReader reader = new VersionedRecordsReader(datasource);
        int versionZeroRecordsCount = reader.fetchAll(versionZero, dataset.getName()).length;

        VersionedRecord[] records = reader.fetchAll(versionOne, dataset.getName());
        assertEquals(versionZeroRecordsCount + 2, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++) {
            assertEquals(++init, records[i].getRecordId());
        }
    }
}

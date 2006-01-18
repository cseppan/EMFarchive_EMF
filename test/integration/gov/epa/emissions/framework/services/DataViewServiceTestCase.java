package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Date;
import java.util.Random;

public abstract class DataViewServiceTestCase extends ServicesTestCase {

    private DataViewService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    protected void setUpService(DataViewService service) throws Exception {
        this.service = service;
        datasource = emissions();

        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        dataset.setName(table);
        setTestValues(dataset);

        doImport();

        token = editToken();
        service.openSession(token);
    }

    private void doImport() throws ImporterException {
        File file = new File("test/data/orl/nc", "midsize-onroad.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(0);
        ORLOnRoadImporter importer = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() },
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
        assertEquals(2, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(100, page.count());
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

    private DataAccessToken editToken() {
        Version version = versionZero();
        return editToken(version);
    }

    private DataAccessToken editToken(Version version) {
        return editToken(version, dataset.getName());
    }

    private DataAccessToken editToken(Version version, String table) {
        DataAccessToken result = new DataAccessToken(version, table);

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

}

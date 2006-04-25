package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.io.File;
import java.util.Date;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataEditorService_DataTest extends ServiceTestCase {

    private DataEditorServiceImpl service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    private User user;

    protected void doSetUp() throws Exception {
        service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory(configFile()));
        UserServiceImpl userService = new UserServiceImpl(sessionFactory(configFile()));

        datasource = emissions();
        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        dataset.setName(table);
        setTestValues(dataset);
        addDataset();

        doImport(dataset);

        Versions versions = new Versions();
        Version v1 = versions.derive(versionZero(), "v1", session);
        openSession(userService, v1);
    }

    private void openSession(UserService userService, Version v1) throws EmfException {
        token = token(v1);
        user = userService.getUser("emf");
        token = service.openSession(user, token, 5);
    }

    private void doImport(EmfDataset dataset) throws ImporterException {
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "onroad-15records.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version);
        Importer importer = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
                dbServer(), sqlDataTypes(), formatFactory);
        new VersionedImporter(importer, dataset, dbServer()).run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void doTearDown() throws Exception {
        service.closeSession(token);
        dropTable(dataset.getName(), datasource);
        dropData("versions", datasource);
    }

    public void testTokenContainLockStartAndEndInfoOnOpeningSession() {
        assertNotNull("Lock Start Date should be set on opening of session", token.lockStart());
        assertNotNull("Lock End Date should be set on opening of session", token.lockEnd());

        Date expectedStart = token.getVersion().getLockDate();
        assertEquals(expectedStart, token.lockStart());

        EmfProperty timeInterval = new EmfPropertiesDAO().getProperty("lock.time-interval", session);
        Date expectedEnd = new Date(expectedStart.getTime() + Long.parseLong(timeInterval.getValue()));
        assertEquals(expectedEnd, token.lockEnd());
    }

    public void testShouldReturnExactlyThreePages() throws EmfException {
        assertEquals(3, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(5, page.count());
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

    public void testShouldFailWhenAttemptingToEditAFinalVersion() throws Exception {
        Version v0 = versionZero();
        DataAccessToken token = token(v0);
        try {
            service.openSession(user, token);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if attempting to edit a final version");
    }

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        int numberOfRecords = service.getTotalRecords(token);
        assertTrue(numberOfRecords >= 1);
    }

    public void testShouldReturnAtLeastOnePage() throws EmfException {
        int numberOfPages = service.getPageCount(token);
        assertTrue(numberOfPages >= 1);
    }

    public void testShouldApplyConstraintsAndReturnFirstPage() throws Exception {
        String rowFilter = "POLL = '100414'";
        String sortOrder = "POLL, ANN_EMIS";
        Page page = service.applyConstraints(token, rowFilter, sortOrder);

        assertEquals(1, page.getNumber());
        assertEquals(2, page.count());
    }

    private DataAccessToken token(Version version) {
        return token(version, dataset.getName());
    }

    private DataAccessToken token(Version version, String table) {
        DataAccessToken result = new DataAccessToken(version, table);

        return result;
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getId(), 0, session);
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
            int recordId = allRecs[i].getRecordId();
            if (recordId == numberOfRecords - 1) {
                found = true;
            }
        }
        assertTrue("Could not look up Page by Record Number", found);
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

    public void testNewRecordsAreSavedOnSave() throws Exception {
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(dataset.getId());
        changeset.addNew(record7);

        VersionedRecord record8 = new VersionedRecord();
        record8.setDatasetId(dataset.getId());
        changeset.addNew(record8);

        service.submit(token, changeset, 1);
        service.save(token, dataset);

        DefaultVersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        int v0RecordsCount = reader.fetch(versionZero(), dataset.getName(), session).total();

        assertEquals(v0RecordsCount + 3, reader.fetch(v1, dataset.getName(), session).total());
    }

    public void testDatasetModifiedDateUpdatedOnSave() throws Exception {
        Date originalTimestamp = dataset.getModifiedDateTime();
        Date expectedTimestamp = new Date(dataset.getModifiedDateTime().getTime() + 4500);
        dataset.setModifiedDateTime(expectedTimestamp);
        service.save(token, dataset);

        EmfDataset modified = load(dataset);
        Date modifiedTimestamp = modified.getModifiedDateTime();
        assertTrue("Modified Timestamp should be update on save", originalTimestamp.getTime() < modifiedTimestamp
                .getTime());
    }

    private void addDataset() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);
        dataset.setCreator(owner.getUsername());

        dataset.setModifiedDateTime(new Date());
        add(dataset);
    }

    private EmfDataset load(EmfDataset dataset) {
        session.clear();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", dataset.getName()));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void testLockRenewedOnSave() throws Exception {
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);
        DataAccessToken saved = service.save(token, dataset);
        assertTrue("Should renew lock on save", saved.isLocked(user));
    }

    private Version versionOne() throws EmfException {
        Version[] versions = service.getVersions(dataset.getId());
        return versions[1];
    }

    public void testShouldBeAbleToSubmitMultipleChangeSetsForSameVersion() throws Exception {
        Version v1 = versionOne();

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        changeset2.setVersion(v1);
        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(dataset.getId());
        changeset2.addNew(record7);
        service.submit(token, changeset2, 1);

        service.save(token, dataset);

        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        int v0RecordsCount = reader.fetch(versionZero(), dataset.getName(), session).total();

        int v1Count = reader.fetch(v1, dataset.getName(), session).total();
        assertEquals(v0RecordsCount + 2, v1Count);
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version v1 = versionOne();

        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        int v1Count = reader.fetch(v1, dataset.getName(), session).total();

        DataAccessToken token = token(v1, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        service.discard(token);

        int postDiscardCount = reader.fetch(v1, dataset.getName(), session).total();
        assertEquals(v1Count, postDiscardCount);
    }

    public void testShouldConfirmWithYesIfChangesExist() throws Exception {
        Version v1 = versionOne();
        DataAccessToken token = token(v1, table);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);
        service.submit(token, changeset, 1);

        assertTrue("Should confirm with Yes if session contains changes", service.hasChanges(token));
    }

    public void testShouldConfirmWithNoIfChangesDontExist() throws Exception {
        Version v1 = versionOne();
        DataAccessToken token = token(v1, table);

        assertFalse("Should confirm with No if session does not contains changes", service.hasChanges(token));
    }

    public void testShouldAddNewRecordsInChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version v1 = versionOne();

        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 2);
        service.getPage(token, 1);
        service.getPage(token, 2);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count() + 1, records.length);

        VersionedRecord[] page1Records = page.getRecords();
        for (int i = 0; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());
    }

    public void testTotalRecordsShouldIncludeUncommitedChanges() throws Exception {
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        int totalPriorToChanges = service.getTotalRecords(token);
        service.submit(token, changeset, 1);

        assertEquals(totalPriorToChanges + 1, service.getTotalRecords(token));
    }

    public void testShouldApplyChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version v1 = versionOne();

        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        VersionedRecord[] page1Records = page.getRecords();
        changeset.addDeleted(page1Records[2]);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 2);
        service.getPage(token, 0);
        service.getPage(token, 1);

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
        Version v1 = versionOne();

        DataAccessToken token = token(v1, table);
        service.openSession(user, token);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        page1ChangeSet.addNew(record6);
        Page page1 = service.getPage(token, 1);
        service.submit(token, page1ChangeSet, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 0);
        service.getPage(token, 1);

        // page 0 changes
        Page page2 = service.getPage(token, 2);
        VersionedRecord[] page4Records = page2.getRecords();
        ChangeSet page2ChangeSet = new ChangeSet();
        page2ChangeSet.setVersion(v1);
        page2ChangeSet.addDeleted(page4Records[2]);
        service.submit(token, page2ChangeSet, 2);

        Page page1AfterChanges = service.getPage(token, 1);
        assertEquals(page1.count() + 1, page1AfterChanges.count());

        Page page2AfterChanges = service.getPage(token, 2);
        assertEquals(page2.count() - 1, page2AfterChanges.count());
    }

    public void FIXME_testShouldSaveChangeSetAndRegeneratePagesAfterSave() throws Exception {
        Version v1 = versionOne();

        DataAccessToken token = token(v1, table);
        service.openSession(user, token);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(v1);
        VersionedRecord newRecord = new VersionedRecord(10);
        newRecord.setDatasetId(dataset.getId());
        page1ChangeSet.addNew(newRecord);
        service.submit(token, page1ChangeSet, 1);

        int recordsBeforeSave = service.getTotalRecords(token);
        service.save(token, dataset);

        assertEquals(recordsBeforeSave, service.getTotalRecords(token));
    }

}

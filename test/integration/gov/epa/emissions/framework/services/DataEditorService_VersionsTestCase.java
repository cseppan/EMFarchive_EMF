package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.io.File;
import java.util.Date;
import java.util.Random;

public abstract class DataEditorService_VersionsTestCase extends ServicesTestCase {

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

        Version[] updated = service.getVersions(dataset.getDatasetid());
        assertEquals(2, updated.length);
        assertEquals("v 1", updated[1].getName());
        assertTrue("Derived version (loaded from db) should be final on being marked 'final'", updated[1]
                .isFinalVersion());
    }

    public void testShouldMarkFinalAndRetrieveVersions() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());
        Version versionZero = versions[0];
        Version derived = service.derive(versionZero, "v 1");
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals("v 1", derived.getName());

        Version finalVersion = service.markFinal(derived);
        assertEquals(derived.getDatasetId(), finalVersion.getDatasetId());
        assertEquals(derived.getVersion(), finalVersion.getVersion());

        Version[] updatedVersions = service.getVersions(dataset.getDatasetid());
        assertEquals(versionZero.getVersion(), updatedVersions[0].getVersion());
        assertEquals(finalVersion.getVersion(), updatedVersions[1].getVersion());
    }
}

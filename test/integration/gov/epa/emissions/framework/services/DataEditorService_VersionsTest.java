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
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataEditorService_VersionsTest extends ServicesTestCase {

    private DataEditorService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    protected void doSetUp() throws Exception {
        service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory());
        UserServiceImpl userService = new UserServiceImpl(sessionFactory());

        datasource = emissions();

        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        dataset.setName(table);
        setTestValues(dataset);

        doImport();

        Version v1 = new Versions().derive(versionZero(), "v1", session);
        token = token(v1);
        service.openSession(userService.getUser("emf"), token);
    }

    private void doImport() throws ImporterException {
        File file = new File("test/data/orl/nc", "very-small-nonpoint.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(0);
        ORLNonPointImporter importer = new ORLNonPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, dbServer(), sqlDataTypes(), formatFactory);
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

    private DataAccessToken token(Version version) {
        return token(version, dataset.getName());
    }

    private DataAccessToken token(Version version, String table) {
        DataAccessToken result = new DataAccessToken(version, table);

        return result;
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getDatasetid(), 0, session);
    }

    public void testShouldHaveVersionZeroAfterDatasetImport() throws Exception {
        Version[] versions = service.getVersions(dataset.getDatasetid());

        assertNotNull("Should return versions of imported dataset", versions);
        assertEquals(2, versions.length);

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
        assertEquals(2, derived.getVersion());
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
        assertEquals(3, updated.length);
        assertEquals("v 1", updated[2].getName());
        assertTrue("Derived version (loaded from db) should be final on being marked 'final'", updated[2]
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
        assertEquals(finalVersion.getVersion(), updatedVersions[2].getVersion());
    }
}

package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.dao.UserDAO;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public class DataAccessServiceTest extends ServicesTestCase {

    private DataAccessServiceImpl service;

    public Datasource datasource;

    private User owner;

    private DataAccessToken token;

    protected void doSetUp() throws Exception {
        datasource = emissions();
        service = createService();

        owner = new UserDAO().get("emf", session);
        token = new DataAccessToken();
        Version version = versionZero();
        token.setVersion(version);

        createDataTable("dataccess_test");
        token.setTable("dataccess_test");
    }

    private DataAccessServiceImpl createService() throws Exception {
        DefaultVersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        DataAccessCache cache = new DataAccessCache(reader, writerFactory, datasource, sqlDataTypes());

        return new DataAccessServiceImpl(cache, sessionFactory());
    }

    protected void doTearDown() throws Exception {
        dropData("versions", datasource);
        dropTable("dataccess_test", datasource);

        service.shutdown();
    }

    private void createDataTable(String table) throws Exception {
        Column col1 = new Column("p1", sqlDataTypes().text());
        createVersionedTable(table, emissions(), new Column[] { col1 });
    }

    private void insertVersionZero(Datasource datasource, String table) throws Exception {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, (new String[] { null, "1", "0", "version zero", "", "true" }));
    }

    private Version versionZero() throws Exception {
        insertVersionZero(datasource, "versions");

        Versions versions = new Versions();
        return versions.get(1, session)[0];
    }

    public void testOpeningEditSessionShouldObtainLock() throws Exception {
        DataAccessToken result = service.openEditSession(owner, token);
        assertTrue("Should have obtained lock on opening edit session", result.isLocked(owner));
    }

    public void testClosingEditSessionShouldReleaseLock() throws Exception {
        DataAccessToken locked = service.openEditSession(owner, token);

        DataAccessToken result = service.closeEditSession(locked);
        assertFalse("Should be unlocked on close", result.isLocked(owner));
    }
}

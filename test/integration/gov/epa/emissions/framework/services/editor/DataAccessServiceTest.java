package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbColumn;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableDefinition;
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

    private Datasource datasource;

    protected void doSetUp() throws Exception {
        datasource = emissions();
        DefaultVersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        DataAccessCache cache = new DataAccessCache(reader, writerFactory, datasource, sqlDataTypes());

        service = new DataAccessServiceImpl(cache, sessionFactory());
    }

    protected void doTearDown() throws Exception {
        dropData("versions", datasource);
        dropTable("dataccess_test", datasource);

        service.shutdown();
    }

    public void testOpeningEditSessionShouldObtainLock() throws Exception {
        User owner = new UserDAO().get("emf", session);
        DataAccessToken token = new DataAccessToken();
        Version version = versionZero();
        token.setVersion(version);

        createSampleTable("dataccess_test");
        token.setTable("dataccess_test");

        DataAccessToken result = service.openEditSession(owner, token);
        assertTrue(result.isLocked(owner));
    }

    private void createSampleTable(String table) throws Exception {
        TableDefinition def = datasource.tableDefinition();
        SqlDataTypes types = sqlDataTypes();
        DbColumn[] cols = { new Column("p1", types.text()), new Column("p2", types.text()) };
        def.createTable(table, cols);
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
}

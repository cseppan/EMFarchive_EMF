package gov.epa.emissions.framework;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.framework.db.PostgresDbUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

public abstract class PersistenceTestCase extends TestCase {

    private DatabaseSetup dbSetup;

    protected void setUp() throws Exception {
        String folder = "test";
        File conf = new File(folder, "test.conf");

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + folder + "), name it commons.conf, configure " + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        dbSetup = new DatabaseSetup(properties);
    }

    protected void tearDown() throws Exception {
        dbSetup.tearDown();
    }

    protected Datasource emissions() {
        return dbServer().getEmissionsDatasource();
    }

    private DbServer dbServer() {
        return dbSetup.getDbServer();
    }

    protected SqlDataTypes dataTypes() {
        return dbServer().getDataType();
    }

    protected void dropTable(Datasource datasource, String table) throws Exception, SQLException {
        PostgresDbUpdate dbUpdate = new PostgresDbUpdate();
        dbUpdate.dropTable(datasource.getName(), table);
    }
}

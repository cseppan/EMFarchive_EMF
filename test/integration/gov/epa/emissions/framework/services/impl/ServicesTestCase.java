package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.PostgresDbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.db.LocalHibernateConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;

public abstract class ServicesTestCase extends TestCase {

    private DatabaseSetup dbSetup;

    final protected void setUp() throws Exception {
        dbSetup = new DatabaseSetup(config());
        doSetUp();
    }

    abstract protected void doSetUp() throws Exception;

    protected Properties config() throws IOException, FileNotFoundException {
        String folder = "test";
        File conf = new File(folder, "postgres.conf");

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + folder + "), name it " + conf.getName() + ", configure " + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        return properties;
    }

    final protected void tearDown() throws Exception {
        doTearDown();
        dbSetup.tearDown();
    }

    abstract protected void doTearDown() throws Exception;

    protected Datasource emissions() {
        return dbServer().getEmissionsDatasource();
    }

    protected DbServer dbServer() {
        return dbSetup.getDbServer();
    }

    protected SqlDataTypes dataTypes() {
        return dbServer().getSqlDataTypes();
    }

    protected void dropTable(Datasource datasource, String table) throws Exception, SQLException {
        PostgresDbUpdate dbUpdate = new PostgresDbUpdate();
        dbUpdate.dropTable(datasource.getName(), table);
    }

    protected ServiceLocator serviceLocator() throws Exception {
        Properties config = config();
        String baseUrl = config.getProperty("emf.services.url");
        return new RemoteServiceLocator(baseUrl);
    }

    protected SessionFactory sessionFactory() throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration();
        return config.factory();
    }

    public void deleteDatasets() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();
    }
}

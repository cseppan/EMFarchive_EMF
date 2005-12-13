package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.db.LocalHibernateConfiguration;
import gov.epa.emissions.framework.db.PostgresDbUpdate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.SessionFactory;

import junit.framework.TestCase;

public abstract class ServicesTestCase extends TestCase {

    private DatabaseSetup dbSetup;

    protected void setUp() throws Exception {
        dbSetup = new DatabaseSetup(config());
    }

    protected Properties config() throws IOException, FileNotFoundException {
        String folder = "test";
        File conf = new File(folder, "test.conf");

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + folder + "), name it test.conf, configure " + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        return properties;
    }

    protected void tearDown() throws Exception {
        dbSetup.tearDown();
    }

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

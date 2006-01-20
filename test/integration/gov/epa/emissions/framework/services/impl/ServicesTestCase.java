package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.EmfDatabaseSetup;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.db.LocalHibernateConfiguration;
import gov.epa.emissions.framework.db.VersionedTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hibernate.Session;

public abstract class ServicesTestCase extends TestCase {

    private EmfDatabaseSetup dbSetup;

    protected Session session;

    final protected void setUp() throws Exception {
        dbSetup = new EmfDatabaseSetup(config());
        session = sessionFactory().getSession();

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
        session.close();
        dbSetup.tearDown();
    }

    abstract protected void doTearDown() throws Exception;

    protected Datasource emissions() {
        return dbServer().getEmissionsDatasource();
    }

    protected DataSource emf() {
        return dbSetup.emfDatasource();
    }

    protected DbServer dbServer() {
        return dbSetup.getDbServer();
    }

    protected SqlDataTypes sqlDataTypes() {
        return dbServer().getSqlDataTypes();
    }

    protected void dropTable(String table, Datasource datasource) throws Exception, SQLException {
        PostgresDbUpdate dbUpdate = new PostgresDbUpdate();
        dbUpdate.dropTable(datasource.getName(), table);
    }

    protected void dropData(String table, Datasource datasource) throws Exception, SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.dropAll(table);
    }

    protected ServiceLocator serviceLocator() throws Exception {
        Properties config = config();
        String baseUrl = config.getProperty("emf.services.url");
        return new RemoteServiceLocator(baseUrl);
    }

    protected HibernateSessionFactory sessionFactory() throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration();
        return new HibernateSessionFactory(config.factory());
    }

    public void deleteDatasets() throws Exception {
        ExImDbUpdate dbUpdate = new ExImDbUpdate();
        dbUpdate.deleteAllDatasets();
    }

    protected void createVersionedTable(String table, Datasource datasource, Column[] cols) throws Exception {
        new VersionedTable(datasource, sqlDataTypes()).create(table, cols);
    }

}

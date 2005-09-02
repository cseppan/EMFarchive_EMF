package gov.epa.emissions.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

public class EmfDatabaseTestCase extends DatabaseTestCase {

    protected IDatabaseConnection connection;

    protected IDataSet getDataSet() throws Exception {
        return null;
    }

    protected void setUp() throws Exception {
        connection = getConnection();
    }

    protected IDatabaseConnection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        //FIXME: read the settings from a config file
        Connection jdbcConnection = DriverManager.getConnection("jdbc:postgresql://ben.cep.unc.edu/EMF.raghu", "emf",
                "emf");

        return new DatabaseConnection(jdbcConnection);
    }

    protected void closeConnection(IDatabaseConnection connection) throws Exception {
        // do not close connection on every test
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.REFRESH;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

}

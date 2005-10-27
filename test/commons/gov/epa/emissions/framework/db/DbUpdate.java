package gov.epa.emissions.framework.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

public class DbUpdate {

    protected DatabaseConnection connection;

    public DbUpdate() throws Exception {
        this(new Config("test/uat/uat.conf"));
    }
    
    public DbUpdate(Config config) throws Exception {
        connection = connection(config);
    }

    private DatabaseConnection connection(Config config) throws Exception {
        Class.forName(config.driver());
        Connection jdbcConnection = DriverManager.getConnection(config.url(), config.username(), config.password());

        DatabaseConnection dbUnitConnection = new DatabaseConnection(jdbcConnection);
        DatabaseConfig dbUnitConfig = dbUnitConnection.getConfig();
        dbUnitConfig.setFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
        
        return dbUnitConnection;
    }

    public void deleteAll(String table) throws DatabaseUnitException, SQLException {
        IDataSet dataset = new DefaultDataSet(new DefaultTable(table));
        DatabaseOperation.DELETE_ALL.execute(connection, dataset);
    }

    protected void doDelete(IDataSet dataset) throws DatabaseUnitException, SQLException {
        DatabaseOperation.DELETE.execute(connection, dataset);
    }

    // DELETE from table where name=value
    public void delete(String table, String name, String value) throws SQLException, DatabaseUnitException {
        QueryDataSet dataset = new QueryDataSet(connection);
        dataset.addTable(table, "SELECT * from " + table + " WHERE " + name + " ='" + value + "'");

        doDelete(dataset);
    }

    public void delete(String table, String name, int value) throws SQLException, DatabaseUnitException {
        delete(table, name, value + "");
    }

}

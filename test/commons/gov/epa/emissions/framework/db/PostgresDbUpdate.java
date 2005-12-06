package gov.epa.emissions.framework.db;

import gov.epa.emissions.commons.db.PostgresDbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

public class PostgresDbUpdate {

    protected DatabaseConnection connection;

    public PostgresDbUpdate() throws Exception {
        this(new PostgresDbConfig("test/test.conf"));
    }

    public PostgresDbUpdate(PostgresDbConfig config) throws Exception {
        connection = connection(config);
        System.out.println("IN CONFIG: " + config.url());
    }

    private DatabaseConnection connection(PostgresDbConfig config) throws Exception {
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

    public void dropTable(String schema, String table) throws SQLException {
        Connection jdbcConnection = connection.getConnection();
        Statement stmt = jdbcConnection.createStatement();
        stmt.execute("DROP TABLE " + schema + "." + table);

        // FIXME: use dbUnit to drop table
    }
}

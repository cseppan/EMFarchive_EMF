package gov.epa.emissions.framework.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

public class DbUpdate {

    protected DatabaseConnection connection;

    public DbUpdate() throws Exception {
        connection = connection(new Config("test/uat/uat.conf"));
    }

    private DatabaseConnection connection(Config config) throws Exception {
        Class.forName(config.driver());
        Connection jdbcConnection = DriverManager.getConnection(config.url(), config.username(), config.password());

        return new DatabaseConnection(jdbcConnection);
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

}

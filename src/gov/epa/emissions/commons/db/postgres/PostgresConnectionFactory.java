package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.ConnectionParams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnectionFactory {

    private ConnectionParams params;

    public PostgresConnectionFactory(ConnectionParams params) {
        this.params = params;
    }

    private Connection createConnection(String host, String port, String dbName, String user, String password)
            throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfx) {
            throw new SQLException("Can't load JDBC driver!");
        }

        String url = "jdbc:postgresql://" + host + ((port != null) ? (":" + port) : "") + "/" + dbName;

        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        return createConnection(params.getHost(), params.getPort(), params.getDbName(), params.getUsername(), params
                .getPassword());
    }
}

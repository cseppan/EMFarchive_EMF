package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Note: Emissions & Reference are two schemas in a single database i.e. share a connection
public class PostgresDbServer implements DbServer {

    private Connection connection;

    private SqlTypeMapper typeMapper;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public PostgresDbServer(ConnectionParams params, String referenceDatasourceName, String emissionsDatasourceName)
            throws SQLException {
        this.typeMapper = new PostgresSqlTypeMapper();

        connection = createConnection(params.getHost(), params.getPort(), params.getDbName(), params.getUsername(),
                params.getPassword());

        referenceDatasource = createDatasource(referenceDatasourceName, connection);
        emissionsDatasource = createDatasource(emissionsDatasourceName, connection);
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    private Datasource createDatasource(String datasourceName, Connection connection) {
        return new PostgresDatasource(datasourceName, connection);
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

    public SqlTypeMapper getTypeMapper() {
        return typeMapper;
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        StringBuffer precisionBuf = new StringBuffer();
        for (int i = 0; i < precision; i++) {
            precisionBuf.append('9');
        }

        return "to_number(" + asciiColumn + ", '" + precisionBuf.toString() + "')";
    }
}

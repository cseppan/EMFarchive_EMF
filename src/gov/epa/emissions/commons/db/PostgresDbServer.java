package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresDbServer implements DbServer {

    private Connection sharedConnection;

    private SqlTypeMapper typeMapper;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public PostgresDbServer(ConnectionParams emissionsParams, ConnectionParams referenceParams) throws SQLException {
        this.typeMapper = new PostgresSqlTypeMapper();

        emissionsDatasource = createDatasourceWithSharedConnection(emissionsParams);
        referenceDatasource = createDatasourceWithSharedConnection(referenceParams);
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    // Note: Emissions & Reference are two schemas in a single database. So,
    // they share a connection
    private Datasource createDatasourceWithSharedConnection(ConnectionParams params) throws SQLException {
        if (sharedConnection == null)
            sharedConnection = createConnection(params.getHost(), params.getPort(), params.getDbName(), params
                    .getUsername(), params.getPassword());

        return new PostgresDatasource(params, sharedConnection);
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

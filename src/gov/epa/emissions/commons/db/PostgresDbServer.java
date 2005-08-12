package gov.epa.emissions.commons.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresDbServer implements DbServer {

    private Connection sharedConnection;

    private ConnectionParams analysisParams;

    private SqlTypeMapper typeMapper;

    private ConnectionParams emissionsParams;

    private ConnectionParams referenceParams;

    private PostgresDatasource analysisDatasource;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public PostgresDbServer(ConnectionParams analysisParams, ConnectionParams emissionsDatasourceParams,
            ConnectionParams referenceParams) {
        this.analysisParams = analysisParams;
        this.emissionsParams = emissionsDatasourceParams;
        this.referenceParams = referenceParams;
        this.typeMapper = new PostgresSqlTypeMapper();
    }

    // TODO: verify if schema exists. If not, create it (and create tables as
    // needed - reference datasource)
    public void createAnalysisDatasource() throws SQLException {
        Connection connection = createConnection(analysisParams.getHost(), analysisParams.getPort(), analysisParams
                .getDbName(), analysisParams.getUsername(), analysisParams.getPassword());

       analysisDatasource = new PostgresDatasource(analysisParams, connection);
    }

    public void createEmissionsDatasource() throws SQLException {
        emissionsDatasource = createDatasourceWithSharedConnection(emissionsParams);
    }

    public void createReferenceDatasource() throws SQLException {
        referenceDatasource = createDatasourceWithSharedConnection(referenceParams);
    }

    public Datasource getAnalysisDatasource() {        
        return analysisDatasource;
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource()  {
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

package gov.epa.emissions.commons.io.importer.orl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MySqlDbServer implements DbServer {

    private ConnectionParams analysisParams;

    private SqlTypeMapper typeMapper;

    private ConnectionParams emissionsParams;

    private ConnectionParams referenceParams;

    private Datasource analysisDatasource;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    private Properties appProps;

    public MySqlDbServer(ConnectionParams analysisParams, ConnectionParams emissionsParams,
            ConnectionParams referenceParams, Properties appProps) {
        this.analysisParams = analysisParams;
        this.emissionsParams = emissionsParams;
        this.referenceParams = referenceParams;
        this.appProps = appProps;

        this.typeMapper = new MySqlTypeMapper();
    }

    public void createAnalysisDatasource() throws SQLException {
        analysisDatasource = createDatasource(analysisParams);
        if (!doesDatabaseExist(analysisParams, analysisDatasource.getConnection()))
            createDatabase(analysisParams, analysisDatasource.getConnection());
    }

    public void createEmissionsDatasource() throws SQLException {
        emissionsDatasource = createDatasource(emissionsParams);
        if (!doesDatabaseExist(emissionsParams, emissionsDatasource.getConnection()))
            createDatabase(emissionsParams, emissionsDatasource.getConnection());
    }

    public void createReferenceDatasource() throws SQLException {
        referenceDatasource = createDatasource(referenceParams);
        if (!doesDatabaseExist(referenceParams, referenceDatasource.getConnection())) {
            createDatabase(referenceParams, referenceDatasource.getConnection());
            createReferenceTables();
        }
    }

    private void createReferenceTables() throws SQLException {
        File fieldDefsFile = new File((String) appProps.get("DATASET_NIF_FIELD_DEFS"));
        File referenceFilesDir = new File((String) appProps.get("REFERENCE_FILE_BASE_DIR"));

        try {
            // TODO: a better way to access props and create Reference Importer
            ReferenceImporter importer = new ReferenceImporter(this, fieldDefsFile, referenceFilesDir, false);
            importer.createReferenceTables();
            ReferenceTables tables = new ReferenceTables(null, getTypeMapper());
            tables.createAdditionRefTables(referenceDatasource);
        } catch (Exception e) {
            throw new SQLException("could not create reference tables. Reason: " + e.getMessage());
        }
    }

    public Datasource getAnalysisDatasource() {
        return analysisDatasource;
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    // Note: Emissions & Reference are two schemas in a single database. So,
    // they share a connection
    private Datasource createDatasource(ConnectionParams params) throws SQLException {
        Connection connection = createConnection(params.getHost(), params.getPort(), params.getDatasource(), params
                .getUsername(), params.getPassword());

        return new MySqlDatasource(params, connection);
    }

    private void createDatabase(ConnectionParams params, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("CREATE DATABASE " + params.getDbName());
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    private boolean doesDatabaseExist(ConnectionParams params, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW databases");
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (dbName.equalsIgnoreCase(params.getDbName()))
                    return true;
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }

        return false;
    }

    private Connection createConnection(String host, String port, String dbName, String user, String password)
            throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException cnfx) {
            throw new SQLException("Can't load JDBC driver!");
        }

        String url = "jdbc:mysql://" + host + ((port != null) ? (":" + port) : "") + "/" + dbName;

        return DriverManager.getConnection(url, user, password);
    }

    public SqlTypeMapper getTypeMapper() {
        return typeMapper;
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        return asciiColumn;
    }

}

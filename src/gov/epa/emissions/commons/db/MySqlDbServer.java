package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.importer.ReferenceImporter;
import gov.epa.emissions.commons.io.importer.ReferenceTables;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Note: Emissions & Reference are two schemas in a single database i.e. share a
 * connection. A datasource is represented by a schema in MySql, and Database ==
 * Schema
 */
public class MySqlDbServer implements DbServer {

    private SqlTypeMapper typeMapper;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public MySqlDbServer(ConnectionParams params, String referenceDatasourceName, String emissionsDatasourceName,
            File fieldDefsFile, File referenceFilesDir) throws SQLException {
        this.typeMapper = new MySqlTypeMapper();

        createEmissionsDatasource(params, emissionsDatasourceName);
        createReferenceDatasource(params, referenceDatasourceName, fieldDefsFile, referenceFilesDir);
    }

    private void createEmissionsDatasource(ConnectionParams params, String datasourceName) throws SQLException {
        emissionsDatasource = createDatasource(params, datasourceName);
        if (!doesSchemaExist(datasourceName, emissionsDatasource.getConnection()))
            createSchema(datasourceName, emissionsDatasource.getConnection());
    }

    private void createReferenceDatasource(ConnectionParams referenceParams, String datasourceName, File fieldDefsFile,
            File referenceFilesDir) throws SQLException {
        referenceDatasource = createDatasource(referenceParams, datasourceName);
        if (!doesSchemaExist(datasourceName, referenceDatasource.getConnection())) {
            createSchema(datasourceName, referenceDatasource.getConnection());
            createReferenceTables(fieldDefsFile, referenceFilesDir);
        }
    }

    private Datasource createDatasource(ConnectionParams params, String datasourceName) throws SQLException {
        Connection connection = createConnection(params.getHost(), params.getPort(), params.getUsername(), params
                .getPassword());

        return new MySqlDatasource(datasourceName, connection);
    }

    private void createReferenceTables(File fieldDefsFile, File referenceFilesDir) throws SQLException {
        try {
            ReferenceImporter importer = new ReferenceImporter(this, fieldDefsFile, referenceFilesDir, false);
            importer.createReferenceTables();
            ReferenceTables tables = new ReferenceTables(null, getTypeMapper());
            tables.createAdditionRefTables(referenceDatasource);
        } catch (Exception e) {
            throw new SQLException("could not create reference tables. Reason: " + e.getMessage());
        }
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    private void createSchema(String datasourceName, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("CREATE DATABASE " + datasourceName);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    private boolean doesSchemaExist(String datasourceName, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW databases");
            while (rs.next()) {
                String aDatasourceName = rs.getString(1);
                if (aDatasourceName.equalsIgnoreCase(datasourceName))
                    return true;
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }

        return false;
    }

    private Connection createConnection(String host, String port, String user, String password) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException cnfx) {
            throw new SQLException("Can't load JDBC driver!");
        }

        String url = "jdbc:mysql://" + host + ((port != null) ? (":" + port) : "");

        return DriverManager.getConnection(url + "/reference", user, password);
    }

    public SqlTypeMapper getTypeMapper() {
        return typeMapper;
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        return asciiColumn;
    }

}

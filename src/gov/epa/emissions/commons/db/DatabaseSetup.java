package gov.epa.emissions.commons.db;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseSetup {

    private DbServer dbServer;

    public DatabaseSetup(Properties pref) throws SQLException {
        String dbType = pref.getProperty("database.type");

        String emissionsDatasource = pref.getProperty("datasource.emissions.name");
        String referenceDatasource = pref.getProperty("datasource.reference.name");

        String dbName = pref.getProperty("database.name");
        String host = pref.getProperty("database.host");
        String port = pref.getProperty("database.port");
        String username = pref.getProperty("database.username");
        String password = pref.getProperty("database.password");

        ConnectionParams params = new ConnectionParams(dbName, host, port, username, password);

        if (dbType.equals("mysql")) {
            File fieldDefsFile = new File((String) pref.get("DATASET_NIF_FIELD_DEFS"));
            File referenceFilesDir = new File((String) pref.get("REFERENCE_FILE_BASE_DIR"));
            dbServer = new MySqlDbServer(params, referenceDatasource, emissionsDatasource, fieldDefsFile, referenceFilesDir);
        }
        else
            dbServer = new PostgresDbServer(params, referenceDatasource, emissionsDatasource);
    }

    public DbServer getDbServer() {
        return dbServer;
    }

}

package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.mysql.MySqlConnectionFactory;
import gov.epa.emissions.commons.db.mysql.MySqlDbServer;
import gov.epa.emissions.commons.db.postgres.PostgresConnectionFactory;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;

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

        

        if (dbType.equals("mysql")) {
            //Note: use reference schema as the default one to connect 
            ConnectionParams params = new ConnectionParams(referenceDatasource, host, port, username, password);
            createMySqlDbServer(pref, emissionsDatasource, referenceDatasource, params);
        }
        else {
            ConnectionParams params = new ConnectionParams(dbName, host, port, username, password);
            createPostgresDbServer(emissionsDatasource, referenceDatasource, params);
        }
    }

    private void createPostgresDbServer(String emissionsDatasource, String referenceDatasource, ConnectionParams params)
            throws SQLException {
        PostgresConnectionFactory factory = new PostgresConnectionFactory(params);
        dbServer = new PostgresDbServer(factory.getConnection(), referenceDatasource, emissionsDatasource);
    }

    private void createMySqlDbServer(Properties pref, String emissionsDatasource, String referenceDatasource,
            ConnectionParams params) throws SQLException {
        File fieldDefsFile = new File((String) pref.get("DATASET_NIF_FIELD_DEFS"));
        File referenceFilesDir = new File((String) pref.get("REFERENCE_FILE_BASE_DIR"));        
        MySqlConnectionFactory factory = new MySqlConnectionFactory(params);
        
        dbServer = new MySqlDbServer(factory.getConnection(), referenceDatasource, emissionsDatasource, fieldDefsFile,
                referenceFilesDir);
    }

    public DbServer getDbServer() {
        return dbServer;
    }

}

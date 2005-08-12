package gov.epa.emissions.commons.db;


import java.sql.SQLException;
import java.util.Properties;

public class DatabaseSetup {

    private DbServer dbServer;

    private ConnectionParams analysisParams;

    private ConnectionParams emissionsParams;

    private ConnectionParams referenceParams;

    private String dbType;

    // TODO: export connections params to User Preferences file, after setup
    public DatabaseSetup(Properties pref) {
        this.dbType = pref.getProperty("database.type");
        this.dbServer = createDbServer(dbType, pref);
    }

    private DbServer createDbServer(String dbType, Properties pref) {
        if (dbType.equals("mysql"))
            return createMySqlDbServer(pref);

        return createPostgresDbServer(pref);
    }

    private DbServer createMySqlDbServer(Properties pref) {
        analysisParams = createConnectionParams(pref.getProperty("datasource.analysis.name"), pref
                .getProperty("datasource.analysis.name"), pref.getProperty("database.analysis.username"), pref
                .getProperty("database.analysis.password"), pref.getProperty("database.analysis.host"), pref
                .getProperty("database.analysis.port"));

        emissionsParams = createConnectionParams(pref.getProperty("datasource.emissions.name"), pref
                .getProperty("datasource.emissions.name"), pref.getProperty("database.emissions.username"), pref
                .getProperty("database.emissions.password"), pref.getProperty("database.emissions.host"), pref
                .getProperty("database.emissions.port"));

        referenceParams = createConnectionParams(pref.getProperty("datasource.reference.name"), pref
                .getProperty("datasource.reference.name"), pref.getProperty("database.reference.username"), pref
                .getProperty("database.reference.password"), pref.getProperty("database.reference.host"), pref
                .getProperty("database.reference.port"));

        return new MySqlDbServer(analysisParams, emissionsParams, referenceParams, pref);
    }

    private DbServer createPostgresDbServer(Properties pref) {
        analysisParams = createConnectionParams(pref.getProperty("database.analysis.name"), pref
                .getProperty("database.analysis.name"), pref.getProperty("database.analysis.username"), pref
                .getProperty("database.analysis.password"), pref.getProperty("database.analysis.host"), pref
                .getProperty("database.analysis.port"));

        String emissionsDatasource = pref.getProperty("datasource.emissions.name");
        String referenceDatasource = pref.getProperty("datasource.reference.name");

        String dbName = pref.getProperty("database.shared.name");
        String host = pref.getProperty("database.shared.host");
        String port = pref.getProperty("database.shared.port");
        String username = pref.getProperty("database.shared.username");
        String password = pref.getProperty("database.shared.password");

        emissionsParams = new ConnectionParams(dbName, emissionsDatasource, host, port, username, password);
        referenceParams = new ConnectionParams(dbName, referenceDatasource, host, port, username, password);

        return new PostgresDbServer(analysisParams, emissionsParams, referenceParams);
    }

    private ConnectionParams createConnectionParams(String dbName, String datasource, String username, String password,
            String host, String port) {
        return new ConnectionParams(dbName, datasource, host, port, username, password);
    }

    public void init() throws SQLException {
        dbServer.createAnalysisDatasource();
        dbServer.createEmissionsDatasource();
        dbServer.createReferenceDatasource();
    }

    public DbServer getDbServer() {
        return dbServer;
    }

}

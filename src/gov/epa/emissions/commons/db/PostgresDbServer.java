package gov.epa.emissions.commons.db;

import java.sql.Connection;
import java.sql.SQLException;

//Note: Emissions & Reference are two schemas in a single database i.e. share a connection
public class PostgresDbServer implements DbServer {

    private SqlTypeMapper typeMapper;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public PostgresDbServer(Connection connection, String referenceDatasourceName, String emissionsDatasourceName)
            throws SQLException {
        this.typeMapper = new PostgresSqlTypeMapper();

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

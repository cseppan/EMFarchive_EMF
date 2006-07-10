package gov.epa.emissions.framework.services;

import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;

import javax.sql.DataSource;

public class EmfDbServer implements DbServer {

    public static final String EMF_EMISSIONS_SCHEMA = "emissions";

    public static final String EMF_REFERENCE_SCHEMA = "reference";

    private PostgresDbServer dbServer;

    public EmfDbServer() throws Exception {
        DataSource datasource = new DataSourceFactory().get();
        dbServer = new PostgresDbServer(datasource.getConnection(), EmfDbServer.EMF_REFERENCE_SCHEMA,
                EmfDbServer.EMF_EMISSIONS_SCHEMA);
    }


    public Datasource getEmissionsDatasource() {
        return dbServer.getEmissionsDatasource();
    }

    public Datasource getReferenceDatasource() {
        return dbServer.getReferenceDatasource();
    }

    public SqlDataTypes getSqlDataTypes() {
        return dbServer.getSqlDataTypes();
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        return dbServer.asciiToNumber(asciiColumn,precision);
    }
    
    public void disconnect() throws SQLException {
        dbServer.disconnect();
    }


}

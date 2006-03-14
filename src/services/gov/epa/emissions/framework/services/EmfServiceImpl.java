package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;

import javax.sql.DataSource;

public abstract class EmfServiceImpl {

    protected DbServer dbServer;

    protected DataSource datasource;

    public static final String EMF_EMISSIONS_SCHEMA = "emissions";

    public static final String EMF_REFERENCE_SCHEMA = "reference";

    public EmfServiceImpl() throws Exception {
        datasource = new DataSourceFactory().get();

        // FIXME: we should not hard-code the db server. Also, read the
        // datasource names from properties
        dbServer = new PostgresDbServer(datasource.getConnection(), EmfServiceImpl.EMF_REFERENCE_SCHEMA,
                EmfServiceImpl.EMF_EMISSIONS_SCHEMA);
    }

    public EmfServiceImpl(DataSource datasource, DbServer dbServer) {
        this.datasource = datasource;
        this.dbServer = dbServer;
    }

}

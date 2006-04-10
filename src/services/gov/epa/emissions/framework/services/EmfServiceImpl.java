package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class EmfServiceImpl {
    private static Log LOG = LogFactory.getLog(EmfServiceImpl.class);

    protected DbServer dbServer;

    protected DataSource datasource;

    private String name;

    public static final String EMF_EMISSIONS_SCHEMA = "emissions";

    public static final String EMF_REFERENCE_SCHEMA = "reference";

    public EmfServiceImpl(String name) throws Exception {
        this.name = name;
        datasource = new DataSourceFactory().get();

        // FIXME: we should not hard-code the db server. Also, read the
        // datasource names from properties
        dbServer = new PostgresDbServer(datasource.getConnection(), EmfServiceImpl.EMF_REFERENCE_SCHEMA,
                EmfServiceImpl.EMF_EMISSIONS_SCHEMA);
        LOG.debug("Starting  " + name + "(" + this.hashCode() + ")");
    }

    public EmfServiceImpl(DataSource datasource, DbServer dbServer) {
        this.datasource = datasource;
        this.dbServer = dbServer;
    }

    protected void finalize() throws Throwable {
        dbServer.disconnect();
        new PerformanceMetrics().gc("Shutting down " + name + "(" + this.hashCode() + ")");
        super.finalize();
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.dao.DataSourceFactory;
import gov.epa.emissions.framework.services.EMFConstants;

import javax.sql.DataSource;

public abstract class EmfServiceImpl {

    protected DbServer dbServer;

    protected DataSource datasource;

    public EmfServiceImpl() throws Exception {
        datasource = new DataSourceFactory().get();

        // FIXME: we should not hard-code the db server. Also, read the
        // datasource names from properties
        dbServer = new PostgresDbServer(datasource.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                EMFConstants.EMF_EMISSIONS_SCHEMA);
    }

    public EmfServiceImpl(DataSource datasource, DbServer dbServer) {
        this.datasource = datasource;
        this.dbServer = dbServer;
    }

}

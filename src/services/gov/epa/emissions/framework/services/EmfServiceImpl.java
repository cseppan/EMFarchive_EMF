package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class EmfServiceImpl {
    private static Log LOG = LogFactory.getLog(EmfServiceImpl.class);

    protected DbServer dbServer;

    protected DataSource datasource;

    private String name;

    public EmfServiceImpl(String name) throws Exception {

        this.name = name;
        datasource = new DataSourceFactory().get();

        dbServer = new EmfDbServer();
        LOG.debug("Starting  " + name + "(" + this.hashCode() + ")");
        System.out.println("Starting  " + name + "(" + this.hashCode() + "): "
                + EmfDateFormat.format_YYYY_MM_DD_HH_MM(new Date()));
    }

    public EmfServiceImpl(DataSource datasource, DbServer dbServer) {
        this.datasource = datasource;
        this.dbServer = dbServer;
    }

    protected void finalize() throws Throwable {
        dbServer.disconnect();
        new PerformanceMetrics().gc("Shutting down " + name + "(" + this.hashCode() + ")");
        if (DebugLevels.DEBUG_0)
            System.out.println("Shutting down  " + name + "(" + this.hashCode() + "): "
                    + EmfDateFormat.format_YYYY_MM_DD_HH_MM(new Date()));
        super.finalize();
    }

}

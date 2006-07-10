package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class EmfServiceImpl {
    private static Log LOG = LogFactory.getLog(EmfServiceImpl.class);

    protected DbServer dbServer;

    protected DataSource datasource;

    private String name;

    private SimpleDateFormat dateFormat;

    public EmfServiceImpl(String name) throws Exception {
        this.name = name;
        datasource = new DataSourceFactory().get();
        
        this.dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        dbServer = new EmfDbServer();
        LOG.debug("Starting  " + name + "(" + this.hashCode() + ")");
        System.out.println("Starting  " + name + "(" + this.hashCode() + "): "+dateFormat.format(new Date()));
    }

    public EmfServiceImpl(DataSource datasource, DbServer dbServer) {
        this.datasource = datasource;
        this.dbServer = dbServer;
    }

    protected void finalize() throws Throwable {
        dbServer.disconnect();
        new PerformanceMetrics().gc("Shutting down " + name + "(" + this.hashCode() + ")");
        System.out.println("Shutting down  " + name + "(" + this.hashCode() + "): "+dateFormat.format(new Date()));
        super.finalize();
    }

}

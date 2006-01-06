package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExportTask implements Runnable {
    private static Log log = LogFactory.getLog(ExportTask.class);

    private User user;

    private File file;

    private DataService dataServices;

    private StatusServiceImpl statusServices;

    private LoggingServiceImpl loggingServices;

    private EmfDataset dataset;

    private Exporter exporter;

    private AccessLog accesslog;

    protected ExportTask(User user, File file, EmfDataset dataset, Services svcHolder, AccessLog accesslog,
            Exporter exporter) {
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.statusServices = svcHolder.getStatus();
        this.loggingServices = svcHolder.getLoggingService();
        this.dataServices = svcHolder.getData();
        this.exporter = exporter;
        this.accesslog = accesslog;
    }

    public void run() {
        log.info("starting export - file: " + file.getName() + " of type: " + dataset.getDatasetTypeName());
        try {
            setStartStatus();
            exporter.export(file);

            loggingServices.setAccessLog(accesslog);
            dataServices.updateDatasetWithoutLock(dataset);
            setStatus("Completed export for " + dataset.getName() + ":" + file.getName());
        } catch (Exception e) {
            log.error("Problem on attempting to run ExIm on file : " + file, e);
            setStatus("Export failure. Reason: " + e.getMessage());
        }

        log.info("exporting of file: " + file.getName() + " of type: " + dataset.getName() + " complete");
    }

    private void setStartStatus() {
        setStatus("Started export for " + dataset.getName() + ":" + file.getName());
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setMessageType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.AccessLog;
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

    private DataServiceImpl dataService;

    private StatusServiceImpl statusServices;

    private LoggingServiceImpl loggingService;

    private EmfDataset dataset;

    private Exporter exporter;

    private AccessLog accesslog;

    protected ExportTask(User user, File file, EmfDataset dataset, Services svcHolder, AccessLog accesslog,
            Exporter exporter) {
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.statusServices = svcHolder.getStatus();
        this.loggingService = svcHolder.getLoggingService();
        this.dataService = svcHolder.getData();
        this.exporter = exporter;
        this.accesslog = accesslog;
    }

    public void run() {
        try {
            setStartStatus();
            exporter.export(file);

            loggingService.setAccessLog(accesslog);
            dataService.updateDatasetWithoutLock(dataset);
            setStatus("Completed export for " + dataset.getName() + ":" + file.getName());
        } catch (Exception e) {
            log.error("Problem on attempting to run Export on file : " + file, e);
            setStatus("Export failure. Reason: " + e.getMessage());
        }
    }

    private void setStartStatus() {
        setStatus("Started export for " + dataset.getName() + ":" + file.getName());
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

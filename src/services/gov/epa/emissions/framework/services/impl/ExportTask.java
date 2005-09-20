/*
 * Creation on Sep 1, 2005
 * Eclipse Project Name: EMF
 * File Name: ExportTask.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.exporter.Exporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

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
    private StatusServices statusServices = null;
	private LoggingServices loggingServices = null;
    private EmfDataset dataset;
	private Exporter exporter;
	private AccessLog accesslog=null;

    /**
     * 
     * @param user
     * @param file
     * @param dataset
     * @param statusSvc
     * @param exporter
     */
	protected ExportTask(User user, File file, EmfDataset dataset, ServicesHolder  svcHolder, AccessLog accesslog, Exporter exporter) {
		this.user=user;
		this.file=file;
		this.dataset=dataset;
		this.statusServices=svcHolder.getStatusSvc();
		this.loggingServices =svcHolder.getLogSvc();
		this.exporter=exporter;
		this.accesslog=accesslog;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
    public void run() {
        log.info("starting export - file: " + file.getName() + " of type: " + dataset.getDatasetType());
        try {
            setStartStatus();
            exporter.run( dataset, file);
            //update access logs
            loggingServices.setAccessLog(accesslog);
            setStatus(EMFConstants.END_EXPORT_MESSAGE_Prefix + dataset.getName() + ":" + file.getName());
        } catch (Exception e) {
            log.error("Problem on attempting to run ExIm on file : " + file, e);
            try {
                setStatus("Export failure. Reason: " + e.getMessage());
            } catch (EmfException e1) {
                log.error("Problem attempting to post 'end status' using Status Service for file : " + file, e1);
            }
        }

        log.info("exporting of file: " + file.getName() + " of type: " + dataset.getName() + " complete");
    }

    private void setStartStatus() throws EmfException {
        setStatus(EMFConstants.START_EXPORT_MESSAGE_Prefix + dataset.getName() + ":" + file.getName());
    }

    private void setStatus(String message) throws EmfException {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setMessageType(EMFConstants.EXPORT_MESSAGE_TYPE);
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.setStatus(endStatus);
    }

}

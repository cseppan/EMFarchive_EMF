/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;
import gov.epa.emissions.framework.services.User;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportTask implements Runnable {

    private static Log log = LogFactory.getLog(ImportTask.class);

    private User user;

    private StatusService statusServices = null;

    private DataServiceImpl dataServices = null;

    private Importer importer;

    private EmfDataset dataset;

    private String fileName;

    public ImportTask(User user, String fileName, EmfDataset dataset, ServicesHolder svcHolder, Importer importer) {
        this.user = user;
        this.fileName = fileName;
        this.dataset = dataset;
        this.dataServices = svcHolder.getDataServices();
        this.statusServices = svcHolder.getStatusSvc();

        this.importer = importer;
    }

    public void run() {
        log.info("starting import - file: " + fileName + " of type: " + dataset.getDatasetTypeName());

        try {
            setStartStatus();
            importer.run(dataset);

            // if no errors then insert the dataset into the database
            dataset.setStatus(DatasetStatus.IMPORTED);
            dataServices.insertDataset(dataset);

            setStatus(EMFConstants.END_IMPORT_MESSAGE_Prefix + dataset.getDatasetTypeName() + ":" + fileName);
        } catch (Exception e) {
            log.error("Problem on attempting to run ExIm on file : " + fileName, e);
            try {
                setStatus("Import failure. Reason: " + e.getMessage());
            } catch (EmfException e1) {
                log.error("Problem attempting to post 'end status' using Status Service for file : " + fileName, e1);
            }
        }

        log.info("importing of file: " + fileName + " of type: " + dataset.getDatasetTypeName() + " complete");
    }

    private void setStartStatus() throws EmfException {
        setStatus(EMFConstants.START_IMPORT_MESSAGE_Prefix + dataset.getDatasetTypeName());
    }

    private void setStatus(String message) throws EmfException {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setMessageType(EMFConstants.IMPORT_MESSAGE_TYPE);
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.setStatus(endStatus);
    }

}

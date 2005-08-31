/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.DatasetTypes;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ORLTableType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportTask implements Runnable {

    private static Log log = LogFactory.getLog(ImportTask.class);

    private User user;

    private File file;

    private StatusServices statusServices = null;

    private DatasetType datasetType;

    private Importer importer;

    public ImportTask(User user, File file, DatasetType datasetType, StatusServices statusServices, Importer importer) {
        this.user = user;
        this.file = file;
        this.statusServices = statusServices;
        this.datasetType = datasetType;

        this.importer = importer;
    }

    public void run() {
        log.info("starting import - file: " + file.getName() + " of type: " + datasetType.getName());

        try {
            setStartStatus();

            Dataset dataset = createOrlDataset();

            importer.run(new File[] { file }, dataset, true);

            setStatus(EMFConstants.END_IMPORT_MESSAGE_Prefix + datasetType.getName() + ":" + file.getName());
        } catch (Exception e) {
            log.error("Problem on attempting to run ExIm on file : " + file, e);
            try {
                setStatus("Import failure. Reason: " + e.getMessage());
            } catch (EmfException e1) {
                log.error("Problem attempting to post 'end status' using Status Service for file : " + file, e1);
            }
        }

        log.info("importing of file: " + file.getName() + " of type: " + datasetType.getName() + " complete");
    }

    private Dataset createOrlDataset() {
        Dataset dataset = new EmfDataset();

        // FIXME: why hard code the table type ?
        String filename = file.getName();
        String tablename = filename.substring(0, filename.length() - 4).replace('.', '_');

        dataset.setDatasetType(datasetType.getName());
        dataset.addTable(new Table(ORLTableType.ORL_AREA_NONPOINT_TOXICS.baseTypes()[0], tablename));
        String summaryTableType = DatasetTypes.getSummaryTableType(datasetType.getName());
        dataset.addTable(new Table(summaryTableType, tablename + "_summary"));

        return dataset;
    }

    private void setStartStatus() throws EmfException {
        setStatus(EMFConstants.START_IMPORT_MESSAGE_Prefix + datasetType.getName() + ":" + file.getName());
    }

    private void setStatus(String message) throws EmfException {
        Status endStatus = new Status();
        endStatus.setUserName(user.getUserName());
        endStatus.setMessageType(EMFConstants.IMPORT_MESSAGE_TYPE);
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.setStatus(endStatus);
    }

}

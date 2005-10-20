/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: ImporterService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.DefaultORLDatasetTypesFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ORLTableType;
import gov.epa.emissions.commons.io.importer.ORLTableTypes;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
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

    private File[] files;

    private StatusServices statusServices = null;

    private DataServices dataServices = null;

    private Importer importer;

    private EmfDataset dataset;

	private String fileName;

    public ImportTask(User user, File[] files, String fileName, EmfDataset dataset, ServicesHolder svcHolder, Importer importer) {
        this.user = user;
        this.files = files;
        this.fileName = fileName;
        this.dataset = dataset;
        this.dataServices = svcHolder.getDataSvc();
        this.statusServices = svcHolder.getStatusSvc();

        this.importer = importer;
    }

    public void run() {
        log.info("starting import - file: " + fileName + " of type: " + dataset.getDatasetTypeName());

        try {
            setStartStatus();

            if (dataset.getDatasetTypeName().indexOf("ORL")>=0){
            	log.debug("updating ORL dataset");
            	updateOrlDataset(dataset,files);
            }

            importer.run(files, dataset, true);

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

	private void updateOrlDataset(EmfDataset dataset, File[] files) {
        // FIXME: why hard code the table type ?
        String filename = files[0].getName();
        String tablename = filename.substring(0, filename.length() - 4).replace('.', '_');

        //FIXME: What the heck is this next line?  Did it come in through refactoring/change?
        //dataset.setDatasetType(dataset.getDatasetType());

        ORLTableTypes tableTypes = new ORLTableTypes(new DefaultORLDatasetTypesFactory());
        ORLTableType tableType = tableTypes.type(dataset.getDatasetType());
        dataset.addTable(new Table(tablename, tableType.base()));
        
        //Need to add data to the InternalSource object for each file?
        InternalSource is = null;
        if (files.length>0){
        	is = new InternalSource();
        	
        }
    }

    private void setStartStatus() throws EmfException {
        setStatus(EMFConstants.START_IMPORT_MESSAGE_Prefix + dataset.getDatasetTypeName() + ":" + files[0].getName());
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

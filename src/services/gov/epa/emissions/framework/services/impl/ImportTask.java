package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportTask implements Runnable {

    private static Log log = LogFactory.getLog(ImportTask.class);

    private User user;

    private StatusServiceImpl statusServices = null;

    private DataServiceImpl dataServices = null;

    private Importer importer;

    private EmfDataset dataset;

    private String fileName;

    public ImportTask(User user, String fileName, EmfDataset dataset, Services services, Importer importer) {
        this.user = user;
        this.fileName = fileName;
        this.dataset = dataset;
        this.dataServices = services.getData();
        this.statusServices = services.getStatus();

        this.importer = importer;
    }

    public void run() {
        log.info("starting import - file: " + fileName + " of type: " + dataset.getDatasetTypeName());

        try {
            setStartStatus();
            dataServices.addDataset(dataset);
            dataset.setStatus(EMFConstants.DATASET_STATUS_START_IMPORT);
            importer.run();

            // if no errors then insert the dataset into the database
            dataset.setStatus(EMFConstants.DATASET_STATUS_IMPORTED);
            dataServices.updateDatasetWithoutLock(dataset);
            
            setStatus("Completed import for " + dataset.getDatasetTypeName() + ":" + fileName);
        } catch (Exception e) {
            log.error("Problem on attempting to run ExIm on file : " + fileName, e);
            setStatus("Import failure. Reason: " + e.getMessage());
            try {
                dataServices.removeDataset(dataset);
            } catch (EmfException e1) {
                log.error("Problem removing inserted dataset for failed import : " + dataset.getName(), e);
            }
        }

        log.info("importing of file: " + fileName + " of type: " + dataset.getDatasetTypeName() + " complete");
    }

    private void setStartStatus() {
        setStatus("Started import for " + dataset.getDatasetTypeName());
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setMessageType("Import");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImportTask implements Runnable {

    private static Log log = LogFactory.getLog(ImportTask.class);

    private User user;

    private StatusServiceImpl statusServices = null;

    private DataServiceImpl dataService = null;

    private Importer importer;

    private EmfDataset dataset;

    private String fileName;

    public ImportTask(User user, String fileName, EmfDataset dataset, Services services, Importer importer) {
        this.user = user;
        this.fileName = fileName;
        this.dataset = dataset;
        this.dataService = services.getData();
        this.statusServices = services.getStatus();

        this.importer = importer;
    }

    public void run() {
        log.info("starting import - file: " + fileName + " of type: " + dataset.getDatasetTypeName());

        try {
            setStartStatus();
            dataService.addDataset(dataset);
            dataset.setStatus("Start Import");
            importer.run();

            // if no errors then insert the dataset into the database
            dataset.setStatus("Imported");
            dataService.updateDatasetWithoutLock(dataset);
            
            setStatus("Completed import for " + dataset.getDatasetTypeName() + ":" + fileName);
        } catch (ImporterException e) {
            log.error("Problem attempting to run ExIm on file : " + fileName, e);
            setStatus("Import failure: " + e.getMessage());
            try {
                dataService.removeDataset(dataset);
            } catch (RuntimeException e1) {
                log.error("Problem removing inserted dataset for failed import : " + dataset.getName(), e);
            } catch (EmfException ex) {
                log.error("Problem attempting to run ExIm on file : " + fileName, ex);
            }
        } catch (EmfException e) {
            log.error("Problem attempting to run ExIm on file : " + fileName, e);
        }

        log.info("importing of file: " + fileName + " of type: " + dataset.getDatasetTypeName() + " complete");
    }

    private void setStartStatus() {
        setStatus("Started import for " + dataset.getDatasetTypeName());
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

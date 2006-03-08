package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDao;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ImportService {
    private static Log log = LogFactory.getLog(ImportService.class);

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    private ImporterFactory importerFactory;

    public final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmmss");

    private Services services;

    public ImportService(ImporterFactory importerFactory, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
        this.importerFactory = importerFactory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.services = services();
    }

    private Services services() {
        Services services = new Services();
        services.setLogSvc(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusServiceImpl(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private void isUnique(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDao dao = new DatasetDao();
        boolean nameUsed = dao.nameUsed(dataset.getName(), EmfDataset.class, session);
        session.flush();
        session.close();

        if (nameUsed) {
            log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
    }

    void importSingleDataset(User user, String folderPath, String[] fileNames, EmfDataset dataset) throws EmfException {
        try {
            File path = validatePath(folderPath);

            isUnique(dataset);
            Importer importer = importerFactory.createVersioned(dataset, path, fileNames);
            ImportTask eximTask = new ImportTask(dataset, fileNames, importer, user, services, sessionFactory);

            threadPool.execute(eximTask);
        } catch (Exception e) {
            log.error("Exception attempting to start import of file: " + fileNames[0], e);
            throw new EmfException("Import failed: " + e.getMessage());
        }
    }

    public void importDatasets(User user, String folderPath, String[] filenames, DatasetType datasetType) {
        showMultipleDatasets(user, filenames);

        for (int i = 0; i < filenames.length; i++) {
            String datasetName = filenames[i] + "_" + DATE_FORMATTER.format(new Date());
            EmfDataset dataset = createDataset(datasetName, user, datasetType);
            try {
                importSingleDataset(user, folderPath, new String[] { filenames[i] }, dataset);
            } catch (EmfException e) {
                addFailureStatus(dataset, e.getMessage(), user);
            }
        }
    }

    private void addFailureStatus(EmfDataset dataset, String errorMessage, User user) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage("Import of Dataset " + dataset.getName() + " failed. Reason: " + errorMessage);
        endStatus.setTimestamp(new Date());

        services.getStatus().create(endStatus);
    }

    private EmfDataset createDataset(String datasetName, User user, DatasetType datasetType) {
        EmfDataset dataset = new EmfDataset();

        dataset.setName(datasetName);
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(datasetType);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());

        return dataset;
    }

    public void importDataset(User user, String folderPath, String[] filenames, DatasetType datasetType,
            String datasetName) throws EmfException {
        EmfDataset dataset = createDataset(datasetName, user, datasetType);
        importSingleDataset(user, folderPath, filenames, dataset);
    }

    public String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        try {
            File directory = new File(folder);
            FilePatternMatcher fpm = new FilePatternMatcher(directory, pattern);
            String[] allFilesInFolder = directory.list();
            String[] fileNamesForImport = fpm.matchingNames(allFilesInFolder);
            if (fileNamesForImport.length > 0)
                return fileNamesForImport;

            throw new EmfException("No files found for pattern '" + pattern + "'");
        } catch (ImporterException e) {
            throw new EmfException("Cannot apply pattern.");
        }
    }

    private void showMultipleDatasets(User user, String[] filenames) {
        StatusServiceImpl status = services().getStatus();
        int filecount = filenames.length;
        String message = "***IMPORT MULTIPLE DATASETS (" + filecount + " in total): ";
        for (int i = 0; i < filecount; i++) {
            if (i == filecount - 1)
                message += filenames[i];
            else
                message += filenames[i] + ", ";
        }
        setStatus(message, user, status);
    }

    private void setStatus(String message, User user, StatusServiceImpl statusServices) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ImportClientSubmitter;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedImportService {
    private static Log log = LogFactory.getLog(ManagedImportService.class);

    private HibernateSessionFactory sessionFactory;

    private ImporterFactory importerFactory;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmmss");

    private TaskSubmitter importTaskSubmitter = null;

    private ArrayList<Runnable> importTasks = new ArrayList<Runnable>();

    private static int svcCount = 0;

    private String svcLabel = null;
    
    private DbServerFactory dbServerFactory;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    public ManagedImportService(ImporterFactory importerFactory, HibernateSessionFactory sessionFactory) {
        this(null, importerFactory, sessionFactory);
    }

    public ManagedImportService(DbServerFactory dbServerFactory, ImporterFactory importerFactory, HibernateSessionFactory sessionFactory) {
        this.dbServerFactory = dbServerFactory;
        this.importerFactory = importerFactory;
        this.sessionFactory = sessionFactory;
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
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

    private synchronized void isNameUnique(String name) throws Exception {
        DatasetDAO dao = new DatasetDAO(dbServerFactory);
        boolean nameUsed = dao.datasetNameUsed(name);

        if (nameUsed) {
            // AME: no need to log this as an error
            // log.error("Dataset name " + dataset.getName() + " is already used");
            throw new EmfException("Dataset name is already used");
        }
    }

    public synchronized String importDatasetsForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType) throws EmfException {
        registerSubmitter(folderPath, filenames);
        File path = validatePath(folderPath);
        Services services = services();

        try {
            for (int i = 0; i < filenames.length; i++)
                addTasks(folderPath, path, filenames[i], filenames[i], user, datasetType, services);

            addTasksToSubmitter();
        } catch (Exception e) {
            setErrorMsgs(folderPath, e);
        }

        return importTaskSubmitter.getSubmitterId();
    }
    
    public synchronized String importDatasetForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType, String datasetName) throws EmfException {
        registerSubmitter(folderPath, filenames);
        File path = validatePath(folderPath);
        Services services = services();

        try {
            addTasks(folderPath, path, filenames[0], datasetName, user, datasetType, services);
            addTasksToSubmitter();
        } catch (Exception e) {
            setErrorMsgs(folderPath, e);
        }

        return importTaskSubmitter.getSubmitterId();
    }

    private synchronized void registerSubmitter(String folderPath, String[] filenames) {
        // The service instance (one per session) will have only one submitter for the type of service
        // Here the TaskManagerImportService has one reference to the ImportClientSubmitter
        if (importTaskSubmitter == null) {
            importTaskSubmitter = new ImportClientSubmitter();
            // importTaskSubmitter.registerTaskManager();
            TaskManagerFactory.getImportTaskManager().registerTaskSubmitter(importTaskSubmitter);
        }

        if (DebugLevels.DEBUG_9) {
            System.out.println("ManagedImportService:import() called at: " + new Date());
            System.out.println(">>## In import service:import() " + myTag() + " for datasets: " + filenames.toString());
            // FIXME: Verify at team meeting Test if subpath exists. If not create subpath
            System.out.println("FULL PATH= " + folderPath);
        }
    }
    
    private synchronized void addTasksToSubmitter() {
        if (DebugLevels.DEBUG_9)
            System.out
                    .println("Before importTaskSubmitter.addTasksToSubmitter # of elements in importTasks array= "
                            + importTasks.size());

        // All eximTasks have been created...so add to the submitter
        importTaskSubmitter.addTasksToSubmitter(importTasks);

        // now that all tasks have been submitted remove them from from eximTasks
        importTasks.removeAll(importTasks);

        log.info("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + importTaskSubmitter.getTaskCount());
        log.info("ManagedImportService:import() submitted all importTasks dropping out of loop");

        if (DebugLevels.DEBUG_9) {
            System.out
                    .println("After importTaskSubmitter.addTasksToSubmitter and importTasks cleanout # of elements in eximTasks array= "
                            + importTasks.size());
            System.out.println("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: "
                    + importTaskSubmitter.getTaskCount());
            System.out.println("ManagedImportService:import() exiting at: " + new Date());
        }
    }
    
    private synchronized void setErrorMsgs(String folderPath, Exception e) throws EmfException {
        // don't need to log messages about importing to existing file
        if (e.getMessage() != null && e.getMessage().indexOf("existing file") < 0)
            log.error("ERROR starting to import to folder: " + folderPath, e);
        e.printStackTrace();
        throw new EmfException("Import failed: " + e.getMessage());
    }

    private synchronized void addTasks(String folder, File path, String filename, String dsName, User user,
            DatasetType dsType, Services services) throws Exception {
        EmfDataset dataset = createDataset(folder, filename, dsName, user, dsType);
        isNameUnique(dataset.getName());

        Importer importer = importerFactory.createVersioned(dataset, path, new String[] { filename });
        ImportTask task = new ImportTask(dataset, new String[] { filename }, importer, user, services, dbServerFactory, sessionFactory);

        importTasks.add(task);
    }

    public synchronized String importDatasetsForCaseOutput(User user, String folderPath, String[] filenames,
            DatasetType datasetType) {
        // wait to fill
        return "Test_Import_Case_Output_Datasets_with_managed_import_service_" + new Date();
    }

    private EmfDataset createDataset(String folder, String filename, String datasetName, User user,
            DatasetType datasetType) {
        EmfDataset dataset = new EmfDataset();
        File file = new File(folder, filename);

        dataset.setName(datasetName);
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(datasetType);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(file.exists() ? new Date(file.lastModified()) : new Date());
        dataset.setAccessedDateTime(new Date());

        return dataset;
    }

    public synchronized String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
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

}

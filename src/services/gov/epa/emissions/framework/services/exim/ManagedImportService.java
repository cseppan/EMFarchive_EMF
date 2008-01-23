package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.io.importer.FilePatternMatcher;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ImportCaseOutputSubmitter;
import gov.epa.emissions.framework.tasks.ImportCaseOutputTask;
import gov.epa.emissions.framework.tasks.ImportClientSubmitter;
import gov.epa.emissions.framework.tasks.ImportSubmitter;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagedImportService {
    private static Log log = LogFactory.getLog(ManagedImportService.class);

    private HibernateSessionFactory sessionFactory;

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MMddyy_HHmmss");

    private ImportSubmitter importClientSubmitter = null;

    private ImportSubmitter importCaseOutputSubmitter = null;

    private ArrayList<Runnable> importTasks = new ArrayList<Runnable>();

    private static int svcCount = 0;

    private static final String FOR_CLIENT = "client";

    private static final String FOR_OUTPUT = "output";

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

    public ManagedImportService(HibernateSessionFactory sessionFactory) {
        this(DbServerFactory.get(), sessionFactory);
    }

    public ManagedImportService(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private synchronized File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private synchronized boolean isNameUsed(String name) throws Exception {
        DatasetDAO dao = new DatasetDAO(dbServerFactory);
        return dao.datasetNameUsed(name);
    }

    public synchronized String importDatasetsForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType) throws EmfException {
        registerSubmitter(FOR_CLIENT, folderPath, filenames); //initialies importClientSubmitter
        File path = validatePath(folderPath);
        Services services = services();

        try {
            for (int i = 0; i < filenames.length; i++)
                addTasks(folderPath, path, new String[] { filenames[i] }, filenames[i], user, datasetType, services);

            addTasksToSubmitter(importClientSubmitter);
        } catch (Exception e) {
            setErrorMsgs(folderPath, e);
        }

        return importClientSubmitter.getSubmitterId();
    }

    public synchronized String importDatasetForClient(User user, String folderPath, String[] filenames,
            DatasetType datasetType, String datasetName) throws EmfException {
        registerSubmitter(FOR_CLIENT, folderPath, filenames);
        File path = validatePath(folderPath);
        Services services = services();

        try {
            addTasks(folderPath, path, filenames, datasetName, user, datasetType, services);
            addTasksToSubmitter(importClientSubmitter);
        } catch (Exception e) {
            setErrorMsgs(folderPath, e);
        }

        return importClientSubmitter.getSubmitterId();
    }

    private synchronized void registerSubmitter(String task, String folderPath, String[] filenames) {
        // The service instance (one per session) will have only one submitter for the type of service
        // Here the TaskManagerImportService has one reference to the ImportClientSubmitter
        ImportSubmitter submitter = null;

        if (task.equals(FOR_CLIENT)) {
            if (importClientSubmitter == null)
                importClientSubmitter = new ImportClientSubmitter();

            submitter = importClientSubmitter;
        } else if (task.equals(FOR_OUTPUT)) {
            if (importCaseOutputSubmitter == null)
                importCaseOutputSubmitter = new ImportCaseOutputSubmitter();

            submitter = importCaseOutputSubmitter;
        }

        TaskManagerFactory.getImportTaskManager().registerTaskSubmitter(submitter);
        logStartMessages(task, folderPath, filenames);
    }

    private void logStartMessages(String task, String folderPath, String[] filenames) {
        if (DebugLevels.DEBUG_9) {
            System.out.println("ManagedImportService: " + task);
            System.out.println("ManagedImportService:import() called at: " + new Date());
            System.out.println(">>## In import service:import() " + myTag() + " for datasets: " + filenames.toString());
            System.out.println("FULL PATH= " + folderPath);
        }
    }

    private synchronized void addTasksToSubmitter(TaskSubmitter submitter) {
        if (DebugLevels.DEBUG_11)
            System.out.println("Before importTaskSubmitter.addTasksToSubmitter # of elements in importTasks array= "
                    + importTasks.size());

        // All eximTasks have been created...so add to the submitter
        submitter.addTasksToSubmitter(importTasks);

        // now that all tasks have been submitted remove them from from eximTasks
        importTasks.removeAll(importTasks);

        log.info("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + submitter.getTaskCount());
        log.info("ManagedImportService:import() submitted all importTasks dropping out of loop");

        if (DebugLevels.DEBUG_9) {
            System.out
                    .println("After importTaskSubmitter.addTasksToSubmitter and importTasks cleanout # of elements in eximTasks array= "
                            + importTasks.size());
            System.out.println("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + submitter.getTaskCount());
            System.out.println("ManagedImportService:import() exiting at: " + new Date());
        }
    }

    private synchronized void setErrorMsgs(String folderPath, Exception e) {
        // don't need to log messages about importing to existing file
        if (e.getMessage() != null && e.getMessage().indexOf("existing file") < 0)
            log.error("ERROR starting to import to folder: " + folderPath, e);
    }

    private synchronized void addTasks(String folder, File path, String[] filenames, String dsName, User user,
            DatasetType dsType, Services services) throws Exception {
        EmfDataset dataset = createDataset(folder, filenames[0], dsName, user, dsType);
        ImportTask task = new ImportTask(dataset, filenames, path, user, services, dbServerFactory, sessionFactory);

        importTasks.add(task);
    }

    private synchronized void addOutputTasks(User user, CaseOutput output, Services services) throws Exception {
        String folder = output.getPath();
        String pattern = output.getPattern();
        String fullPath = output.getDatasetFile();
        String datasetName = output.getDatasetName();
        String[] files = null;

        if ((folder == null || folder.trim().isEmpty()) && (fullPath == null || fullPath.trim().isEmpty()))
            throw new Exception("Error registering output: Please specify files to register case output "+
                    output.getName());


        if (fullPath != null && !fullPath.trim().isEmpty()) {
            // get folder from full path
            File singleFile = new File(fullPath);
            folder = singleFile.getParent();
            files = new String[] { singleFile.getName() };
        } else {
            // get files from pattern and folder
            files = getFilenamesFromPattern(folder, pattern);
        }

        File path = validatePath(folder);
        DatasetType type = getDsType(output.getDatasetType());

        if (files.length > 1 && !type.isExternal())
            for (int i = 0; i < files.length; i++) // here we're making multiple datasets
                createOutputTask(type, datasetName, user, output, services, new String[] { files[i] }, path);
        else  // this is to make one dataset
            createOutputTask(type, datasetName, user, output, services, files, path);
    }

    private synchronized void createOutputTask(DatasetType type, String datasetName, User user, CaseOutput output, Services services, String[] files,
            File path) throws Exception {
        if (datasetName == null || datasetName.trim().isEmpty())
            datasetName = files[0];
        
        if (files.length > type.getMaxFiles() && type.getMaxFiles() != -1)
            throw new EmfException("Error registering output: Number of files (" 
                    + files.length + ") exceeds limit for dataset type " + type.getName() + ".");
        
        CaseOutput localOuput = createNewCaseOutput(output);
        boolean nameSpecified = (localOuput.getName() != null && !localOuput.getName().trim().isEmpty());
        
        if (!nameSpecified)
            localOuput.setName(datasetName);
        
        EmfDataset dataset = createDataset(path.getAbsolutePath(), files[0], datasetName, user, type);
        
        if (DebugLevels.DEBUG_11) {
            System.out.println("Output name before create import task: " + (localOuput == null ? "" : output.getName()));
            System.out.println("Dataset name before create import task: " + dataset.getName());
        }
        
        ImportCaseOutputTask task = new ImportCaseOutputTask(localOuput, dataset, files, path, user, services,
                dbServerFactory, sessionFactory);
        
        //System.out.println("\nADDING IMPORT TASK FOR DATASET: "+datasetName+";files[0]="+files[0]);
        importTasks.add(task);
    }
    
    private synchronized CaseOutput createNewCaseOutput(CaseOutput oldOutput) {
        CaseOutput newOutput = new CaseOutput(oldOutput.getName());
        newOutput.setCaseId(oldOutput.getCaseId());
        newOutput.setJobId(oldOutput.getJobId());
        
        return newOutput;
    }

    private synchronized DatasetType getDsType(String datasetType) throws EmfException {
        DatasetTypesDAO dao = new DatasetTypesDAO();
        DatasetType type = dao.get(datasetType, sessionFactory.getSession());

        if (type == null)
            throw new EmfException("Error registering output: Dataset type '" + datasetType + "' does not exist.");

        return type;
    }

    public synchronized String[] importDatasetsForCaseOutput(User user, CaseOutput[] outputs) throws EmfException {
        List<String> submitterIds = new ArrayList<String>();
        
        // here the files and path are for informational purposes (printing) only
        String[] files = new String[] { outputs[0].getDatasetFile() };        
        registerSubmitter(FOR_OUTPUT, outputs[0].getPath(), files);
        
        Services services = services();
        
        for (CaseOutput output : outputs)
            submitterIds.add(importDatasetForCaseOutput(user, output, services));
        
        return submitterIds.toArray(new String[0]);
    }

    public synchronized String importDatasetForCaseOutput(User user, CaseOutput output, Services services) throws EmfException {
        String fileFolder = output.getPath();
 
        try {
            addOutputTasks(user, output, services);
            addTasksToSubmitter(importCaseOutputSubmitter);
        } catch (Exception e) {
            e.printStackTrace();
            setErrorMsgs(fileFolder, e);
            throw new EmfException(e.getMessage());
        }

        return importCaseOutputSubmitter.getSubmitterId();
    }

   private synchronized EmfDataset createDataset(String folder, String filename, String datasetName, User user,
            DatasetType datasetType) throws Exception {
        EmfDataset dataset = new EmfDataset();
        File file = new File(folder, filename);
        String name = getCorrectedDSName(datasetName, datasetType);
        
        if (isNameUsed(name))
            name += "_" + EmfDateFormat.format_yyyy_MM_dd_HHmmssSS(new Date());

        dataset.setName(name);
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(datasetType);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(file.exists() ? new Date(file.lastModified()) : new Date());
        dataset.setAccessedDateTime(new Date());

        return dataset;
    }

    private synchronized String getCorrectedDSName(String datasetName, DatasetType datasetType) {
        KeyVal[] keyVals =  datasetType.getKeyVals();
        
        if (keyVals == null || keyVals.length == 0)
            return datasetName;
        
        String prefix = null;
        String suffix = null;
        
        for (KeyVal keyval : keyVals) {
            if (keyval.getName().equalsIgnoreCase("EXPORT_PREFIX"))
                prefix = keyval.getValue();
            
            if (keyval.getName().equalsIgnoreCase("EXPORT_SUFFIX"))
                suffix = keyval.getValue();
            
            if (prefix != null && suffix != null)
                break;
        }
            
        if (prefix != null && datasetName.startsWith(prefix))
            datasetName = datasetName.substring(prefix.length());

        if (suffix != null && datasetName.endsWith(suffix))
            datasetName = datasetName.substring(0, datasetName.length() - suffix.length());
            
        return datasetName;
    }

    public synchronized String[] getFilenamesFromPattern(String folder, String pattern) throws EmfException {
        try {
            File directory = new File(folder);
            FilePatternMatcher fpm = new FilePatternMatcher(directory, pattern);
            String[] allFilesInFolder = directory.list();
            String[] fileNamesForImport = fpm.matchingNames(allFilesInFolder);
            
            if (fileNamesForImport.length > 0)
                return fileNamesForImport;

            if (DebugLevels.DEBUG_11) {
                System.out.println("ManagedImportService: File patterns passed: " + pattern);
                
                for (String file : fileNamesForImport)
                    System.out.println("ManagedImportService: File matched from the pattern: " + file);
            }
            
            throw new EmfException("No files found for pattern '" + pattern + "'");
        } catch (ImporterException e) {
            throw new EmfException("Cannot apply pattern.");
        }
    }
    
    public String printStatusImportTaskManager() throws EmfException {
        return TaskManagerFactory.getImportTaskManager().getStatusOfWaitAndRunTable();
    }


}

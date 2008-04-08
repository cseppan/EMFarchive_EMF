package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ExportClientSubmitter;
import gov.epa.emissions.framework.tasks.ExportJobSubmitter;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ManagedExportService {
    private static Log log = LogFactory.getLog(ManagedExportService.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private TaskSubmitter exportTaskSubmitter = null;

    private ArrayList<Runnable> eximTasks = new ArrayList<Runnable>();

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbFactory;

    public ManagedExportService(DbServerFactory dbFactory, HibernateSessionFactory sessionFactory) {
        myTag();
        if (DebugLevels.DEBUG_9)
            System.out.println(">>>> " + myTag());
        this.sessionFactory = sessionFactory;
        this.dbFactory = dbFactory;

        if (System.getProperty("IMPORT_EXPORT_TEMP_DIR") == null)
            setProperties();
    }

    private File validateExportFile(File path, String fileName, boolean overwrite) throws EmfException {
        File file = new File(path, fileName);

        if (!overwrite) {
            if (file.exists() && file.isFile()) {
                // log.error("File exists and cannot be overwritten");
                throw new EmfException("Cannot export to existing file.  Choose overwrite option");
            }
        }
        return file;
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }

    private Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Export");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

    private File validatePath(String folderPath) throws EmfException {
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + folderPath + " does not exist");
            throw new EmfException("Folder does not exist: " + folderPath);
        }
        return file;
    }

    private boolean isExportable(EmfDataset dataset, Version version, Services services, User user) {
        DatasetType datasetType = dataset.getDatasetType();
        String message = null;

        if (version.isLocked() && !version.isFinalVersion())
            message = "The dataset " + dataset.getName()
                    + " is being edited and the version is not final -- not exported.";

        if ((datasetType.getExporterClassName().equals("")) || (datasetType.getExporterClassName() == null)) {
            message = "The exporter for dataset type '" + datasetType + " is not supported";
        }

        if (message != null) {
            Status status = status(user, message);
            services.getStatus().add(status);
            return false;
        }

        return true;
    }

    public String getCleanDatasetName(EmfDataset dataset, Version version) {
        String name = dataset.getName();
        String prefix = "", suffix = "";
        // KeyVal[] keyvals = dataset.getKeyVals(); // only gets KeyVals from dataset itself
        KeyVal[] keyvals = dataset.mergeKeyVals(); // this function is not equal to getKeyVals() anymore
        String date = CustomDateFormat.format_ddMMMyyyy(version.getLastModifiedDate());

        for (int i = 0; i < keyvals.length; i++) {
            prefix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_PREFIX") ? keyvals[i].getValue() : "";
            if (!prefix.equals(""))
                break;
        }

        for (int i = 0; i < keyvals.length; i++) {
            suffix = keyvals[i].getKeyword().getName().equalsIgnoreCase("EXPORT_SUFFIX") ? keyvals[i].getValue() : "";
            if (!suffix.equals(""))
                break;
        }

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }

        String versionString = (version.isFinalVersion() ? "" : "_nf") + "_v" + +version.getVersion();

        return prefix + name + "_" + date.toLowerCase() + versionString + suffix;
    }

    public synchronized String exportForJob(User user, List<CaseInput> inputs, String cjtId, String purpose,
            CaseJob job, Case caseObj) throws EmfException {

        ExportJobSubmitter exportJobTaskSubmitter = null;

        // The service instance (one per session) will have only one submitter for the type of service
        // Here the TaskManagerExportService has one reference to the ExportJobSubmitter
        if (exportJobTaskSubmitter == null) {
            exportJobTaskSubmitter = new ExportJobSubmitter();
            // the casejobTask for these exports.
            exportJobTaskSubmitter.setCaseJobTaskId(cjtId);
            exportJobTaskSubmitter.setJobName(job.getName());
            TaskManagerFactory.getExportTaskManager().registerTaskSubmitter(exportJobTaskSubmitter);
        }

        // FIXME: Any checks for CaseInputs or Jobs needs to happen here

        // FIXME: Moved here to see if session problem is solved.
        Services services = services();

        Iterator<CaseInput> iter = inputs.iterator();

        while (iter.hasNext()) {
            CaseInput caseIp = iter.next();
            EmfDataset dataset = caseIp.getDataset();
            Version version = caseIp.getVersion();
            SubDir subdir = caseIp.getSubdirObj();

            String fullPath = getCleanDatasetName(dataset, version);

            if (DebugLevels.DEBUG_9)
                System.out.println("CleanDataset Name: " + fullPath);
            if ((subdir != null) && !(subdir.toString()).equals("")) {
                fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator") + subdir
                        + System.getProperty("file.separator");
            } else {
                fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator");
            }

            // FIXME: Verify at team meeting Test if subpath exists. If not create subpath
            File toSubDir = null;
            if (DebugLevels.DEBUG_9)
                System.out.println("FULL PATH= " + fullPath);

            toSubDir = new File(fullPath);
            
            if (!toSubDir.exists()) {
                toSubDir.mkdirs();
                toSubDir.setWritable(true, false);
            }

            if (isExportable(dataset, version, services, user)) {
                try {
                    ExportTask tsk = createExportTask(user, purpose, true, toSubDir, dataset, version);

                    // Add the newly created Export Task to the list of eximTasks
                    eximTasks.add(tsk);

                } catch (Exception e) {
                    // don't need to log messages about exporting to existing file
                    if (e.getMessage() != null && e.getMessage().indexOf("existing file") < 0)
                        log.error("ERROR starting to export to folder: " + fullPath, e);
                    e.printStackTrace();
                    throw new EmfException("Export failed: " + e.getMessage());
                }// try-catch
            }// if exportable

        }// while

        if (DebugLevels.DEBUG_9)
            System.out.println("Before exportTaskSubmitter.addTasksToSubmitter # of elements in eximTasks array= "
                    + eximTasks.size());

        // Iterator iter2 = eximTasks.iterator();
        // while (iter2.hasNext()) {
        // Task tsk = (Task) iter2.next();
        // if (DebugLevels.DEBUG_9)
        // System.out
        // .println("&&&&& In ManagedExportService::exportForJob the types of TASK objects in eximTasks: "
        // + tsk.getClass().getName());
        // }

        // All eximTasks have been created...so add to the submitter
        exportJobTaskSubmitter.addTasksToSubmitter(eximTasks);

        // now that all tasks have been submitted remove them from from eximTasks
        eximTasks.removeAll(eximTasks);
        if (DebugLevels.DEBUG_9)
            System.out
                    .println("After exportTaskSubmitter.addTasksToSubmitter and eximTasks cleanout # of elements in eximTasks array= "
                            + eximTasks.size());

        if (DebugLevels.DEBUG_9)
            System.out.println("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: "
                    + exportJobTaskSubmitter.getTaskCount());

        log.info("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + exportJobTaskSubmitter.getTaskCount());
        log.info("ManagedExportService:export() submitted all exportTasks dropping out of loop");

        if (DebugLevels.DEBUG_9)
            System.out.println("ManagedExportService:export() exiting at: " + new Date());

        return exportJobTaskSubmitter.getSubmitterId();
    }

    public synchronized String exportForClient(User user, EmfDataset[] datasets, Version[] versions, String dirName,
            String purpose, boolean overwrite) throws EmfException {

        // FIXME: always overwrite
        // FIXME: hardcode overwite=true until verified with Alison
        overwrite = true;
        if (DebugLevels.DEBUG_9)
            System.out.println("ManagedExportService:export() called at: " + new Date());

        if (DebugLevels.DEBUG_9)
            System.out.println(">>## In export service:export() " + myTag() + " for datasets: " + datasets.toString());

        // The service instance (one per session) will have only one submitter for the type of service
        // Here the TaskManagerExportService has one reference to the ExportClientSubmitter
        if (exportTaskSubmitter == null) {
            exportTaskSubmitter = new ExportClientSubmitter();
            // exportTaskSubmitter.registerTaskManager();
            TaskManagerFactory.getExportTaskManager().registerTaskSubmitter(exportTaskSubmitter);
        }

        // FIXME: Verify at team meeting Test if subpath exists. If not create subpath
        // File toSubDir = null;
        if (DebugLevels.DEBUG_9)
            System.out.println("FULL PATH= " + dirName);

        // toSubDir = new File(dirName);
        // if (!toSubDir.exists()) {
        // toSubDir.mkdirs();
        // }

        File path = validatePath(dirName);

        if (datasets.length != versions.length) {
            log.error("Export failed: version numbers do not match those for specified datasets.");
            throw new EmfException("Export failed: version numbers do not match " + "those for specified datasets.");
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("# of datasets= " + datasets.length);

        // FIXME: Moved here to see if session problem is solved.
        Services services = services();

        try {
            for (int i = 0; i < datasets.length; i++) {
                // Services services = services();
                EmfDataset dataset = datasets[i];
                Version version = versions[i];

                // FIXME: Investigate if services reference needs to be unique for each dataset in this call
                if (isExportable(dataset, version, services, user)) {
                    ExportTask tsk = createExportTask(user, purpose, overwrite, path, dataset, version);

                    eximTasks.add(tsk);

                }
            }

            if (DebugLevels.DEBUG_9)
                System.out.println("Before exportTaskSubmitter.addTasksToSubmitter # of elements in eximTasks array= "
                        + eximTasks.size());

            // All eximTasks have been created...so add to the submitter
            exportTaskSubmitter.addTasksToSubmitter(eximTasks);

            // now that all tasks have been submitted remove them from from eximTasks
            eximTasks.removeAll(eximTasks);
            if (DebugLevels.DEBUG_9)
                System.out
                        .println("After exportTaskSubmitter.addTasksToSubmitter and eximTasks cleanout # of elements in eximTasks array= "
                                + eximTasks.size());

            if (DebugLevels.DEBUG_9)
                System.out.println("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: "
                        + exportTaskSubmitter.getTaskCount());

            log.info("THE NUMBER OF TASKS LEFT IN SUBMITTER FOR RUN: " + exportTaskSubmitter.getTaskCount());
            log.info("ManagedExportService:export() submitted all exportTasks dropping out of loop");

            if (DebugLevels.DEBUG_9)
                System.out.println("ManagedExportService:export() exiting at: " + new Date());

        } catch (Exception e) {
            // don't need to log messages about exporting to existing file
            if (e.getMessage() != null && e.getMessage().indexOf("existing file") < 0)
                log.error("ERROR starting to export to folder: " + dirName, e);
            e.printStackTrace();
            throw new EmfException("Export failed: " + e.getMessage());
        }

        return exportTaskSubmitter.getSubmitterId();
    }

    private synchronized ExportTask createExportTask(User user, String purpose, boolean overwrite, File path,
            EmfDataset dataset, Version version) throws Exception {
        if (DebugLevels.DEBUG_9)
            System.out.println(">>## In export service:doExport() " + myTag() + " for datasetId: " + dataset.getId());

        // Match version in dataset
        if (dataset.getId() != version.getDatasetId())
            throw new EmfException("Dataset doesn't match version (dataset id=" + dataset.getId()
                    + " but version shows dataset id=" + version.getDatasetId() + ")");

        Services services = services();
        File file = validateExportFile(path, getCleanDatasetName(dataset, version), overwrite);

        AccessLog accesslog = new AccessLog(user.getUsername(), dataset.getId(), dataset.getAccessedDateTime(),
                "Version " + version.getVersion(), purpose, file.getAbsolutePath());
        accesslog.setDatasetname(dataset.getName());

        if (DebugLevels.DEBUG_9)
            System.out.println("ManagedExportService: right before creating export task: dbFactory null? "
                    + (dbFactory == null) + " dataset: " + dataset.getName());
        ExportTask eximTask = new ExportTask(user, file, dataset, services, accesslog, dbFactory, sessionFactory,
                version);
        // eximTask.setSubmitterId(exportTaskSubmitter.getSubmitterId());

        return eximTask;
    }

    public Version getVersion(Dataset dataset, int version) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(dataset.getId(), version, session);
        } catch (Exception e) {
            log.error("Retrieve version error - can't retrieve Version object for dataset: " + dataset.getName(), e);
            throw new EmfException("Retrieve version error - can't retrieve Version object for dataset: "
                    + dataset.getName() + " " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public String printStatusExportTaskManager() throws EmfException {
        return TaskManagerFactory.getExportTaskManager().getStatusOfWaitAndRunTable();
    }

    private void setProperties() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty batchSize = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            EmfProperty eximTempDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);

            if (eximTempDir != null)
                System.setProperty("IMPORT_EXPORT_TEMP_DIR", eximTempDir.getValue());

            if (batchSize != null)
                System.setProperty("EXPORT_BATCH_SIZE", batchSize.getValue());
        } finally {
            session.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        svcCount--;
        exportTaskSubmitter = null;
        if (DebugLevels.DEBUG_9)
            System.out.println(">>>> Destroying object: " + myTag());
        super.finalize();
    }
}

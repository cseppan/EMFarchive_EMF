package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ExportTaskManager;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExportTask extends Task {
    @Override
    public boolean isEquivalent(Task task) {
        ExportTask etsk = (ExportTask) task;
        boolean eq = false;

        if (this.file.getAbsolutePath().equals(etsk.getFile().getAbsolutePath())) {
            eq = true;
        }

        // NOTE Auto-generated method stub
        return eq;
    }

    private static Log log = LogFactory.getLog(ExportTask.class);

    private File file;

    private LoggingServiceImpl loggingService;

    private EmfDataset dataset;

    private DatasetType type;

    private AccessLog accesslog;

    private HibernateSessionFactory sessionFactory;

    private Version version;

    private DbServerFactory dbFactory;
    
    private int sleepAfterExport = 0;

    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version) {
        super();
        createId();
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.type = dataset.getDatasetType();
        this.statusServices = services.getStatus();
        this.loggingService = services.getLoggingService();
        this.dbFactory = dbFactory;
        this.accesslog = accesslog;
        this.sessionFactory = sessionFactory;
        this.version = version;
    }

    public void run() {
        DbServer dbServer = null;
        Session session = sessionFactory.getSession();
        this.sleepAfterExport = sleepAfterExport(session);
        
        if (DebugLevels.DEBUG_1)
            System.out.println(">>## ExportTask:run() " + createId() + " for datasetId: " + this.dataset.getId());
        if (DebugLevels.DEBUG_1)
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1)
            if (DebugLevels.DEBUG_1)
                System.out.println("Task# " + taskId + " running");

        try {
            setStartStatus();
            accesslog.setTimestamp(new Date());
            if (file.exists()) {
                setStatus("completed", "FILE EXISTS: Completed export of " + dataset.getName() + " to "
                        + file.getAbsolutePath() + " in " + accesslog.getTimereqrd() + " seconds.");

            } else {
                dbServer = this.dbFactory.getDbServer();
                VersionedExporterFactory exporterFactory = new VersionedExporterFactory(dbServer, dbServer.getSqlDataTypes(),
                        batchSize(session));
                Exporter exporter = exporterFactory.create(dataset, version);
                exporter.export(file);
                accesslog.setEnddate(new Date());
                accesslog.setLinesExported(exporter.getExportedLinesCount());
                printLogInfo(accesslog);

                if (!compareDatasetRecordsNumbers(accesslog, session, dbServer))
                    return;
                // updateDataset(dataset); //Disabled because of nothing updated during exporting

                String msghead = "Completed export of " + dataset.getName();
                String msgend = " in " + accesslog.getTimereqrd() + " seconds.";

                if (type.getExporterClassName().endsWith("ExternalFilesExporter")) {
                    setStatus("completed", msghead + msgend);
                    accesslog.setFolderPath("");
                } else
                    setStatus("completed", msghead + " to " + file.getAbsolutePath() + msgend);

                loggingService.setAccessLog(accesslog);
            }

            if (DebugLevels.DEBUG_4)
                System.out.println("#### Task #" + taskId
                        + " has completed processing making the callback to ExportTaskManager THREAD ID: "
                        + Thread.currentThread().getId());

            // FIXME: Why was the callBack method commented out?
            // ExportTaskManager.callBackFromThread(taskId, this.submitterId, "completed", "succefully in THREAD ID: "
            // + Thread.currentThread().getId());

        } catch (Exception e) {
            setErrorStatus(e, "");
        } finally {
            try {
                // check for isConnected before disconnecting
                if ((dbServer != null) && (dbServer.isConnected()))
                    dbServer.disconnect();
                
                session.close();
                
                Thread.sleep(this.sleepAfterExport * 1000);
                log.warn("ExportTask sleeps " + sleepAfterExport + " seconds after export.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printLogInfo(AccessLog log) {
        String info = "Exported dataset: " + log.getDatasetname() + "; version: " + log.getVersion() + "; start date: "
                + log.getTimestamp() + "; end date: " + log.getEnddate() + "; time required (seconds): "
                + log.getTimereqrd() + "; user: " + log.getUsername() + "; path: " + log.getFolderPath()
                + "; details: " + log.getDetails();
        System.out.println(info);
        // setStatus(info);
    }

    private boolean compareDatasetRecordsNumbers(AccessLog log, Session session, DbServer dbServer) throws Exception {
        String type = dataset.getDatasetType().getName();
        // COSTCY & A/M/PTPRO types temporarily disabled
        if (type.equalsIgnoreCase("Country, state, and county names and data (COSTCY)")
                || type.equalsIgnoreCase("Temporal Profile (A/M/PTPRO)"))
            return true;

        DatasetDAO datasetDao = new DatasetDAO();
        long records = 0;

        try {
            records = datasetDao.getDatasetRecordsNumber(dbServer, session, dataset, version);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } 

        if (records != log.getLinesExported()) {
            setErrorStatus(null, "No. of records in database: " + records + ", but" + " exported "
                    + log.getLinesExported() + " lines");
            return false;
        }

        return true;
    }

    private void setErrorStatus(Exception e, String message) {
        log.error("Problem attempting to export file : " + file + " " + message, e);
        setStatus("failed", "Export failure. " + message + ((e == null) ? "" : e.getMessage()));
    }

    private void setStartStatus() {
        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            setStatus("started", "Started exporting " + dataset.getName());
        else
            setStatus("started", "Started exporting " + dataset.getName() + " to " + file.getAbsolutePath());
    }

    private void setStatus(String status, String message) {
        ExportTaskManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(), message);
    }

    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }

    public File getFile() {
        return file;
    }

    public EmfDataset getDataset() {
        return dataset;
    }

    public Version getVersion() {
        return version;
    }

    private int batchSize(Session session) throws Exception {
        try {
            String batchSize = System.getProperty("EXPORT_BATCH_SIZE");

            if (batchSize != null)
                return Integer.parseInt(batchSize);

            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } catch (Exception e) {
            log.error("Error getting batch size for export. ", e);
            throw new Exception(e.getMessage());
        }
        
    }

    private int sleepAfterExport(Session session) {
        int value = 2;

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("SECONDS_TO_WAIT_AFTER_EXPORT", session);
            value = Integer.parseInt(property.getValue());
        } catch (Exception e) {
            return value; // Default value for maxpool and poolsize
        } 
            
        return value;
    }

}

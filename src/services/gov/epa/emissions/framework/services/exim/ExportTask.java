package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ExportTaskRunManager;
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
        
        if (this.file.getAbsolutePath().equals(etsk.getFile().getAbsolutePath())){
            eq=true;
        }
        
        // NOTE Auto-generated method stub
        return eq;
    }

    private static Log log = LogFactory.getLog(ExportTask.class);

    private File file;

    private LoggingServiceImpl loggingService;

    private EmfDataset dataset;

    private Exporter exporter;

    private AccessLog accesslog;

    private HibernateSessionFactory sessionFactory;

    private Version version;

    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            Exporter exporter, HibernateSessionFactory sessionFactory, Version version) {
        super();
        createId();
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.statusServices = services.getStatus();
        this.loggingService = services.getLoggingService();
        this.exporter = exporter;
        this.accesslog = accesslog;
        this.sessionFactory = sessionFactory;
        this.version = version;
    }

    public void run() {
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
            if (file.exists()){
                setStatus("completed", "FILE EXISTS: Completed export of " + dataset.getName() + " to " + file.getAbsolutePath()
                        + " in " + accesslog.getTimereqrd() + " seconds.");
                
            }else{
                exporter.export(file);
                accesslog.setEnddate(new Date());
                accesslog.setLinesExported(exporter.getExportedLinesCount());

                loggingService.setAccessLog(accesslog);

                printLogInfo(accesslog);
                if (!compareDatasetRecordsNumbers(accesslog))
                    return;
                // updateDataset(dataset); //Disabled because of nothing updated during exporting

                setStatus("completed", "Completed export of " + dataset.getName() + " to " + file.getAbsolutePath()
                        + " in " + accesslog.getTimereqrd() + " seconds.");

            }

            if (DebugLevels.DEBUG_4)
                System.out.println("#### Task #" + taskId
                        + " has completed processing making the callback to ExportTaskRunManager THREAD ID: "
                        + Thread.currentThread().getId());

            // ExportTaskRunManager.callBackFromThread(taskId, this.submitterId, "completed", "succefully in THREAD ID: "
            // + Thread.currentThread().getId());

        } catch (Exception e) {
            setErrorStatus(e, e.getMessage());
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

    private boolean compareDatasetRecordsNumbers(AccessLog log) throws Exception {
        String type = dataset.getDatasetType().getName();
        // COSTCY & A/M/PTPRO types temporarily disabled
        if (type.equalsIgnoreCase("Country, state, and county names and data (COSTCY)")
                || type.equalsIgnoreCase("Temporal Profile (A/M/PTPRO)"))
            return true;

        DatasetDAO datasetDao = new DatasetDAO();
        DbServer dbServer = new EmfDbServer();
        Session session = sessionFactory.getSession();

        long records = datasetDao.getDatasetRecordsNumber(dbServer, session, dataset, version);
        session.close();

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

    void updateDataset(EmfDataset dataset) throws EmfException {
        DatasetDAO dao = new DatasetDAO();
        try {
            Session session = sessionFactory.getSession();
            dao.updateWithoutLocking(dataset, session);
            session.close();
        } catch (Exception e) {
            log.error("Could not update Dataset - " + dataset.getName(), e);
            throw new EmfException("Could not update Dataset - " + dataset.getName());
        }
    }

    private void setStartStatus() {
        setStatus("started", "Started exporting " + dataset.getName() + " to " + file.getAbsolutePath());
    }

    private void setStatus(String status, String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        ExportTaskRunManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(), message);

    }

    // private void setStatus(String message) {
    // Status endStatus = new Status();
    // endStatus.setUsername(user.getUsername());
    // endStatus.setType("Export");
    // endStatus.setMessage(message);
    // endStatus.setTimestamp(new Date());
    //
    // statusServices.add(endStatus);
    // }

//    @Override
//    protected void finalize() throws Throwable {
//        taskCount--;
//        if (DebugLevels.DEBUG_1)
//            System.out.println(">>>> Destroying object: " + createId());
//        super.finalize();
//    }

    public File getFile() {
        return file;
    }

}

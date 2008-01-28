package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ImporterFactory;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ImportCaseOutputTask extends Task {

    @Override
    public boolean isEquivalent(Task task) { // NOTE: needs to verify definition of equality
        ImportCaseOutputTask importTask = (ImportCaseOutputTask) task;

        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())) {
            return true;
        }

        return false;
    }

    private static Log log = LogFactory.getLog(ImportCaseOutputTask.class);

    protected Importer importer;

    protected EmfDataset dataset;

    private CaseOutput output;

    protected String[] files;

    protected HibernateSessionFactory sessionFactory;

    protected double numSeconds;

    protected DatasetDAO datasetDao;

    private CaseDAO caseDao;

    private File path;

    private DbServerFactory dbServerFactory;

    public ImportCaseOutputTask(CaseOutput output, EmfDataset dataset, String[] files, File path, User user,
            Services services, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        super();
        createId();

        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());

        this.user = user;
        this.files = files;
        this.path = path;
        this.dataset = dataset;
        this.output = output;
        this.statusServices = services.getStatus();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.datasetDao = new DatasetDAO(dbServerFactory);
        this.caseDao = new CaseDAO(sessionFactory);
    }

    public void run() {
        if (DebugLevels.DEBUG_1)
            System.out.println(">>## ImportTask:run() " + taskId + " for dataset: " + this.dataset.getName());
        if (DebugLevels.DEBUG_1)
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1)
            if (DebugLevels.DEBUG_1)
                System.out.println("Task# " + taskId + " running");

        ImporterFactory importerFactory = new ImporterFactory(dbServerFactory);
        
        try {
            Importer importer = importerFactory.createVersioned(dataset, path, files);
            long startTime = System.currentTimeMillis();
            
            prepare();
            importer.run();
            numSeconds = (System.currentTimeMillis() - startTime) / 1000;
            complete("Imported");
        } catch (Exception e) {
            // this doesn't give the full path for some reason
            logError("Failed to import file(s) : " + filesList(), e);
            setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + e.getMessage());
            removeDataset(dataset);
            
            Session session = sessionFactory.getSession();
            
            try {
                caseDao.removeCaseOutputs(user, new CaseOutput[] { output }, true, session);
            } catch (EmfException e1) {
                e1.printStackTrace();
            } finally {
                session.close();
            }
        } finally {
            try {
                if (importerFactory != null)
                    importerFactory.closeDbConnection();
            } catch (Exception e2) {
                log.error("Error closing database connection.", e2);
            }
        }
    }

    private void prepare() throws EmfException {
        addStartStatus();
        caseDao.add(user, output);
        dataset.setStatus("Started import");
        addDataset();
    }

    private void complete(String status) {
        String message = "Case output " + output.getName() + " registered successfully.";

        dataset.setStatus(status);
        updateDataset(dataset);
        updateOutput(status, message);
        addCompletedStatus();
    }

    private void updateOutput(String status, String message) {
        Session session = sessionFactory.getSession();

        try {
            output.setDatasetId(datasetDao.getDataset(session, dataset.getName()).getId());
            output.setStatus(status);
            output.setMessage(message);
            caseDao.updateCaseOutput(output, session);
        } catch (Exception e) {
            log.error("Error updating case output " + output.getName() + ". ", e);
        } finally {
            session.close();
        }
    }

    protected String filesList() {
        StringBuffer fileList = new StringBuffer(files[0]);

        if (files.length > 1)
            for (int i = 1; i < files.length; i++)
                fileList.append(", " + files[i]);

        return fileList.toString();
    }

    protected void addDataset() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            String name = dataset.getName();

            if (datasetDao.datasetNameUsed(name)) {
                name += "_" + CustomDateFormat.format_yyyy_MM_dd_HHmmssSS(new Date());
                dataset.setName(name);
            }

            datasetDao.add(dataset, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void updateDataset(EmfDataset dataset) {
        Session session = sessionFactory.getSession();

        try {
            datasetDao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
        } finally {
            session.close();
        }
    }

    protected void removeDataset(EmfDataset dataset) {
        try {
            Session session = sessionFactory.getSession();
            datasetDao.remove(dataset, session);
            session.close();
        } catch (Exception e) {
            logError("Could not get remove Dataset - " + dataset.getName(), e);
        }
    }

    protected void addStartStatus() {
        setStatus("started", "Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "
                + files[0] + output.getName() + " " + caseDao.toString());
    }

    protected void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] "
                + " in " + numSeconds + " seconds from " + files[0]; // TODO: add batch size to message once
        // available
        setStatus("completed", message);
    }

    protected void setStatus(String status, String message) {
        output.setStatus(status);
        output.setMessage(message);

        if (DebugLevels.DEBUG_4) {
            System.out.println("ImportTaskManager = " + ImportTaskManager.getImportTaskManager());
            System.out.println("taskId = " + taskId);
            System.out.println("submitterId = " + submitterId);
            System.out.println("status = " + status);
            System.out.println("current thread = " + Thread.currentThread());
            System.out.println("message = " + message);
        }

        ImportTaskManager.callBackFromThread(taskId, this.submitterId, status, Thread.currentThread().getId(), message);
    }

    protected void logError(String messge, Exception e) {
        log.error(messge, e);
    }

    public EmfDataset getDataset() {
        return this.dataset;
    }

    public Importer getImporter() {
        return this.importer;
    }

    @Override
    protected void finalize() throws Throwable {
        taskCount--;
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> Destroying object: " + createId());
        super.finalize();
    }

}

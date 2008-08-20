package gov.epa.emissions.framework.services.exim;

import java.io.File;
import java.util.Date;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.ImportTaskManager;
import gov.epa.emissions.framework.tasks.Task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ImportTask extends Task {
    
    @Override
    public boolean isEquivalent(Task task) { //NOTE: needs to verify definition of equality
        ImportTask importTask = (ImportTask) task;
        
        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())){
            return true;
        }
        
        return false;
    }

    private static Log log = LogFactory.getLog(ImportTask.class);

    protected Importer importer;

    protected EmfDataset dataset;

    protected String[] files;

    protected HibernateSessionFactory sessionFactory;

    protected double numSeconds;
    
    protected DatasetDAO dao;

    private File path;

    private DbServerFactory dbServerFactory;

    public ImportTask(EmfDataset dataset, String[] files, File path, User user, Services services,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        super();
        createId();
        
        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + createId());
        
        this.user = user;
        this.files = files;
        this.dataset = dataset;
        this.statusServices = services.getStatus();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.dao = new DatasetDAO(dbServerFactory);
        this.path = path;
    }

    public void run() {
        if (DebugLevels.DEBUG_1)
            System.out.println(">>## ImportTask:run() " + taskId + " for dataset: " + this.dataset.getName());
        if (DebugLevels.DEBUG_1)
            System.out.println("Task#" + taskId + " RUN @@@@@ THREAD ID: " + Thread.currentThread().getId());

        if (DebugLevels.DEBUG_1)
            if (DebugLevels.DEBUG_1)
                System.out.println("Task# " + taskId + " running");
        
        Session session = null;
        DbServer dbServer = null;
        boolean isDone = false;
        String errorMsg = "";
        
        try {
            dbServer = dbServerFactory.getDbServer();
            ImporterFactory importerFactory = new ImporterFactory(dbServer);
            Importer importer = importerFactory.createVersioned(dataset, path, files);
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            session.setFlushMode(FlushMode.NEVER);
            
            prepare(session);
            importer.run();
            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            complete(session, "Imported");
            isDone = true;
        } catch (Exception e) {
            errorMsg += e.getMessage();
            // this doesn't give the full path for some reason
            logError("File(s) import failed for user (" + user.getUsername() + ") at " + new Date().toString() + "--" + filesList(), e);
            removeDataset(dataset);
        } finally {
            if (isDone) {
                addCompletedStatus();
                session.flush();
            } else 
                addFailedStatus(errorMsg);
            
            try {
                if (session != null) 
                    session.close();
                
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (HibernateException e1) {
                log.error("Error closing hibernate session.", e1);
            } catch (Exception e2) {
                log.error("Error closing database connection.", e2);
            }
        }
    }

    protected void prepare(Session session) throws EmfException {
        addStartStatus();
        dataset.setStatus("Started import");
        addDataset(dataset, session);
    }

    protected void complete(Session session, String status) {
        dataset.setStatus(status);
        updateDataset(dataset, session);
    }

    protected String filesList() {
        StringBuffer fileList = new StringBuffer(files[0]);
        fileList.append("Path: " + path.getAbsolutePath() + "; File(s): ");

        if (files.length > 1)
            for (int i = 1; i < files.length; i++)
                fileList.append(", " + files[i]);

        return fileList.toString();
    }

    protected void addDataset(EmfDataset dataset, Session session) throws EmfException {
        try {
            if (dao.datasetNameUsed(dataset.getName()))
                throw new EmfException("The selected Dataset name is already in use");
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        }

        dao.add(dataset, session);
    }

    protected void updateDataset(EmfDataset dataset, Session session) {
        try {
            dao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
        }
    }

    protected void removeDataset(EmfDataset dataset) {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(dataset, session);
            session.close();
        } catch (Exception e) {
            logError("Could not get remove Dataset - " + dataset.getName(), e);
        }
    }

    protected void addStartStatus() {
        setStatus("started", "Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "+
                files[0]);
    }

    protected void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] " 
               + " in " + numSeconds+" seconds from "+ files[0]; //TODO: add batch size to message once available
        setStatus("completed", message);
    }
    
    private void addFailedStatus(String errorMsg) {
        setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + errorMsg);
    }

    protected void setStatus(String status, String message) {
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

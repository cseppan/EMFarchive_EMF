package gov.epa.emissions.framework.tasks;

import java.io.File;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ImporterFactory;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class ImportCaseOutputTask extends Task {
    
    @Override
    public boolean isEquivalent(Task task) { //NOTE: needs to verify definition of equality
        ImportCaseOutputTask importTask = (ImportCaseOutputTask) task;
        
        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())){
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

    public ImportCaseOutputTask(CaseOutput output, EmfDataset dataset, String[] files, File path, User user, Services services,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
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
        
        Session session = null;
        ImporterFactory importerFactory = new ImporterFactory(dbServerFactory);
        try {
            Importer importer = importerFactory.createVersioned(dataset, path, files);
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            session.setFlushMode(FlushMode.NEVER);
            
            prepare(session);
            importer.run();
            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            complete(session, "Imported");
        } catch (Exception e) {
            // this doesn't give the full path for some reason
            logError("Failed to import file(s) : " + filesList(), e);
            setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + e.getMessage());
            removeDataset(dataset);
            try {
                caseDao.removeCaseOutputs(user, new CaseOutput[]{output}, true, session);
            } catch (EmfException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if ((session != null) && (session.isConnected())){
                    session.flush();
                    session.close();
                }
                
                if (importerFactory != null)
                    importerFactory.closeDbConnection();
            } catch (HibernateException e1) {
                log.error("Error closing hibernate session.", e1);
            } catch (Exception e2) {
                log.error("Error closing database connection.", e2);
            }
        }
    }

    protected void prepare(Session session) throws EmfException {
        addStartStatus();
        caseDao.add(user, output, session);
        dataset.setStatus("Started import");
        addDataset(dataset, session);
    }

    protected void complete(Session session, String status) {
        String message = "Case output " + output.getName() + " registered successfully.";
        
        dataset.setStatus(status);
        updateDataset(dataset, session);
        updateOutput(session, status, message);
        addCompletedStatus();
    }

    private void updateOutput(Session session, String status, String message) {
        output.setDatasetId(datasetDao.getDataset(session, dataset.getName()).getId());
        output.setStatus(status);
        output.setMessage(message);
        caseDao.updateCaseOutput(output, session);
    }

    protected String filesList() {
        StringBuffer fileList = new StringBuffer(files[0]);

        if (files.length > 1)
            for (int i = 1; i < files.length; i++)
                fileList.append(", " + files[i]);

        return fileList.toString();
    }

    protected void addDataset(EmfDataset dataset, Session session) throws EmfException {
        try {
            if (datasetDao.datasetNameUsed(dataset.getName()))
                throw new EmfException("The selected Dataset name is already in use");
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        }

        datasetDao.add(dataset, session);
    }

    protected void updateDataset(EmfDataset dataset, Session session) {
        try {
            datasetDao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
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
        setStatus("started", "Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "+
                files[0] + output.getName() + " " + caseDao.toString());
    }

    protected void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] " 
               + " in " + numSeconds + " seconds from "+ files[0]; //TODO: add batch size to message once available
        setStatus("completed", message);
    }

    protected void setStatus(String status, String message) {
        output.setStatus(status);
        output.setMessage(message);
        
        System.out.println("ImportTaskManager = " + ImportTaskManager.getImportTaskManager());
        System.out.println("taskId = " + taskId);
        System.out.println("submitterId = " + submitterId);
        System.out.println("status = " + status);
        System.out.println("current thread = " + Thread.currentThread());
        System.out.println("message = " + message);
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

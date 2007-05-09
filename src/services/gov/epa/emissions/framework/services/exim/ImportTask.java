package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;

public class ImportTask implements Runnable {

    private static Log log = LogFactory.getLog(ImportTask.class);

    private User user;

    private Importer importer;

    private EmfDataset dataset;

    private String[] files;

    private HibernateSessionFactory sessionFactory;

    private Services services;
    
    private double numSeconds;

    public ImportTask(EmfDataset dataset, String[] files, Importer importer, User user, Services services,
            HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.files = files;
        this.dataset = dataset;
        this.services = services;
        this.sessionFactory = sessionFactory;

        this.importer = importer;
    }

    public void run() {
        Session session = null;
        try {
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            session.setFlushMode(FlushMode.NEVER);
            
            prepare(session);
            importer.run();
            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            complete(session);
        } catch (Exception e) {
            logError("Failed to import file(s) : " + filesList(), e);
            setStatus("Failed to import dataset " + dataset.getName() + ". Reason: " + e.getMessage());
            removeDataset(dataset);
        } finally {
            if (session != null)
                session.flush();
                session.close();
        }
    }

    private void prepare(Session session) throws EmfException {
        addStartStatus();
        dataset.setStatus("Started import");
        addDataset(dataset, session);
    }

    private void complete(Session session) {
        dataset.setStatus("Imported");
        //dataset.setModifiedDateTime(new Date()); //last mod time has been set when creted

        updateDataset(dataset, session);
        addCompletedStatus();
    }

    private String filesList() {
        StringBuffer fileList = new StringBuffer(files[0]);

        if (files.length > 1)
            for (int i = 1; i < files.length; i++)
                fileList.append(", " + files[i]);

        return fileList.toString();
    }

    void addDataset(EmfDataset dataset, Session session) throws EmfException {
        DatasetDAO dao = new DatasetDAO();
        
        try {
            if (dao.datasetNameUsed(dataset.getName()))
                throw new EmfException("The selected Dataset name is already in use");
        } catch (Exception e) {
            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
        }

        dao.add(dataset, session);
    }

    void updateDataset(EmfDataset dataset, Session session) {
        DatasetDAO dao = new DatasetDAO();
        try {
            dao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            logError("Could not update Dataset - " + dataset.getName(), e);
        }
    }

    void removeDataset(EmfDataset dataset) {
        DatasetDAO dao = new DatasetDAO();
        try {
            Session session = sessionFactory.getSession();
            dao.remove(dataset, session);
            session.close();
        } catch (Exception e) {
            logError("Could not get remove Dataset - " + dataset.getName(), e);
        }
    }

    private void addStartStatus() {
        setStatus("Started import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] from "+
                files[0]);
    }

    private void addCompletedStatus() {
        String message = "Completed import of " + dataset.getName() + " [" + dataset.getDatasetTypeName() + "] " 
               + " in " + numSeconds+" seconds from "+ files[0]; //TODO: add batch size to message once available
        setStatus(message);
        System.out.println(message);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Import");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        services.getStatus().add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}

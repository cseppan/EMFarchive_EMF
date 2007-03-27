package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CMImportTask implements Runnable {

    private static Log log = LogFactory.getLog(CMImportTask.class);

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;
    
    private StatusDAO statusDao;

    public CMImportTask(File folder, String[] files, User user, HibernateSessionFactory sessionFactory, DbServer dbServer) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbServer;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        try {
            ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, files, user, sessionFactory, dbServer);
            importer.run();
            
        } catch (Exception e) {
            logError("Failed to import all control measures", e); // FIXME: report generation
            setStatus("Failed to import all control measures: " + e.getMessage());
            setDetailStatus("Failed to import all control measures: " + e.getMessage());
        } finally {
                //
        }
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void setDetailStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

}

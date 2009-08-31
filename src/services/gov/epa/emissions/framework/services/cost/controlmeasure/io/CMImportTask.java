package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import java.io.File;
import java.util.Date;

public class CMImportTask implements Runnable {

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private StatusDAO statusDao;

    public CMImportTask(File folder, String[] files, User user, HibernateSessionFactory sessionFactory,
            DbServerFactory dbServerFactory) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        try {
            ControlMeasuresImporter importer = null;
            try {
                importer = new ControlMeasuresImporter(folder, files, user, sessionFactory, dbServerFactory);
            } catch (Exception e) {
                setDetailStatus(e.getMessage());
                setStatus(e.getMessage());
            }
            if (importer != null)
                importer.run();
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }

    private void setDetailStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

}

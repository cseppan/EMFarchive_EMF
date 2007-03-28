package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;

public class CMImportTask implements Runnable {

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;
    
    public CMImportTask(File folder, String[] files, User user, HibernateSessionFactory sessionFactory, DbServer dbServer) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbServer;
    }

    public void run() {
        try {
            ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, files, user, sessionFactory, dbServer);
            importer.run();
            
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }
}

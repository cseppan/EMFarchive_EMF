package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import java.io.File;

public class CMImportTask implements Runnable {

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    public CMImportTask(File folder, String[] files, User user, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
    }

    public void run() {
        try {
            ControlMeasuresImporter importer = new ControlMeasuresImporter(folder, files, user, sessionFactory, dbServerFactory);
            importer.run();
            
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }
}

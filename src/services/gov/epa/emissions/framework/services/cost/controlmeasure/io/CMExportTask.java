package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;

public class CMExportTask implements Runnable {

    private static Log log = LogFactory.getLog(CMExportTask.class);

    private StatusDAO statusDao;

    private File folder;

    private ControlMeasure[] controlMeasures;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private ControlMeasureDAO controlMeasureDao;

    public CMExportTask(File folder, String prefix, ControlMeasure[] controlMeasures, User user, HibernateSessionFactory sessionFactory) {
        this.folder = folder;
        this.user = user;
        this.controlMeasures = controlMeasures;
        this.sessionFactory = sessionFactory;
        this.controlMeasureDao = new ControlMeasureDAO();
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        Session session = sessionFactory.getSession();
        try {
            session.setFlushMode(FlushMode.NEVER);
            prepare();
            ControlMeasuresExporter exporter = new ControlMeasuresExporter(folder, controlMeasures, user, sessionFactory);
            exporter.run();
            complete(controlMeasures, session);
        } catch (Exception e) {
            logError("Failed to export control measures", e); // FIXME: report generation
            setStatus("Failed to export all control measures: " + e.getMessage());
        } finally {
            session.flush();
            session.close();
        }
    }

    private void prepare() {
        addStartStatus();
    }

    private void complete(ControlMeasure[] measures, Session session) {
        Date date = new Date();
        List messages = new ArrayList(); 
        List addedMeasures = new ArrayList();
        int count = 0;
        for (int i = 0; i < measures.length; i++) {
            measures[i].setCreator(user);
            measures[i].setLastModifiedTime(date);
            try {
                controlMeasureDao.add(measures[i],measures[i].getSccs(), session);
                addedMeasures.add(controlMeasureDao.load(measures[i], session));
                count++;
            } catch (EmfException e) {
                messages.add(e.getMessage());
            }
        }
        addCompletedStatus(count);
    }

    private void addStartStatus() {
        setStatus("Started exporting control measures");
    }

    private void addCompletedStatus(int noOfMeasures) {
        setStatus("Completed exporting " + noOfMeasures + " control measures");
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMExport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void logError(String messge, Exception e) {
        log.error(messge, e);
    }

}

package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class RunQAStepTask implements Runnable {

    private QAProgramRunner runQAProgram;

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(RunQAStepTask.class);

    private QADAO qaDao;

    private HibernateSessionFactory sessionFactory;

    public RunQAStepTask(QAStep qaStep, User user, QAProgramRunner runQAProgram, HibernateSessionFactory sessionFactory) {
        this.qastep = qaStep;
        this.user = user;
        this.runQAProgram = runQAProgram;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.qaDao = new QADAO();
    }

    public void run() {
        try {
            prepare();
            runQAProgram.run();
            complete();
        } catch (Exception e) {
            logError("Failed to run qa step : " + qastep.getName(), e);
            setStatus("Failed to run qa step " + qastep.getName() + ". Reason: " + e.getMessage());
        }
    }

    private void prepare() {
        setStatus("Started running qa step '" + qastep.getName() + "'");

    }

    private void complete() {
        setStatus("Completed running qa step '" + qastep.getName() + "'");
        updateQAStep();

    }

    private void updateQAStep() {
        Session session = sessionFactory.getSession();
        try {
            qaDao.update(new QAStep[] { qastep }, session);
        } finally {
            session.close();
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("RunQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

}

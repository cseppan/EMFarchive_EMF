package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
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

    private EmfDbServer dbServer;

    public RunQAStepTask(QAStep qaStep, User user, QAProgramRunner runQAProgram, EmfDbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.qastep = qaStep;
        this.user = user;
        this.runQAProgram = runQAProgram;
        this.dbServer = dbServer;
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
            logError("Failed to run QA step : " + qastep.getName(), e);
            setStatus("Failed to run QA step " + qastep.getName() + ". Reason: " + e.getMessage());
        }finally{
            disconnect(dbServer);
        }
    }

    private void disconnect(EmfDbServer dbServer) {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            logError("Failed to close a connetion. " + qastep.getName(), e);
            setStatus("Failed to close a connetion. Reason: " + e.getMessage());
        }
    }

    private void prepare() {
        setStatus("Started running QA step '" + qastep.getName() + "'");

    }

    private void complete() {
        setStatus("Completed running QA step '" + qastep.getName() + "'");
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

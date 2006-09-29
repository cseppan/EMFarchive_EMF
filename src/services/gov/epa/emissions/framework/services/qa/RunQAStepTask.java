package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
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

    private HibernateSessionFactory sessionFactory;

    private EmfDbServer dbServer;

    public RunQAStepTask(QAStep qaStep, User user, QAProgramRunner runQAProgram, EmfDbServer dbServer,
            HibernateSessionFactory sessionFactory) {
        this.qastep = qaStep;
        this.user = user;
        this.runQAProgram = runQAProgram;
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        String suffix = "";
        try {
            suffix = suffix();
            prepare(suffix);
            runQAProgram.run();
            complete(suffix);
        } catch (Exception e) {
            logError("Failed to run QA step : " + qastep.getName() + suffix, e);
            setStatus("Failed to run QA step " + qastep.getName() + suffix + ". " + e.getMessage());
        } finally {
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

    private void prepare(String suffixMsg) {
        setStatus("Started running QA step '" + qastep.getName() + suffixMsg);

    }

    private void complete(String suffixMsg) {
        setStatus("Completed running QA step '" + qastep.getName() + suffixMsg);
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

    private String suffix() {
        return "' for Version '" + versionName() + "' of Dataset '" + datasetName() + "'";
    }

    private String versionName() {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName() {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }

}

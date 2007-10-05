package gov.epa.emissions.framework.services.qa;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class RunQAStep implements Runnable {

    private QAStep[] qaSteps;

    private DbServer dbServer;

    private HibernateSessionFactory sessionFactory;

    private User user;

    private Log log = LogFactory.getLog(RunQAStep.class);

    public RunQAStep(QAStep[] steps, User user, DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.qaSteps = steps;
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
    }

    public void run() {
        try {
            RunQAStepTask task = new RunQAStepTask(qaSteps, user, dbServer, sessionFactory);
            task.run();
        } catch (EmfException e) {
            logError("Could not run all QA steps", e);
        } finally {
            disconnect(dbServer);
        }
    }

    private void disconnect(DbServer dbServer) {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            logError("Failed to close a connetion.", e);
        }
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

}

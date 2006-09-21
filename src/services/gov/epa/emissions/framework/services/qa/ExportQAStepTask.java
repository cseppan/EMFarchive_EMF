package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExportQAStepTask implements Runnable {

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportQAStepTask.class);

    private Exporter exporter;

    private File file;

    private DbServer dbServer;

    public ExportQAStepTask(File file, Exporter exporter, DbServer dbServer, QAStep qaStep, User user,
            HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.exporter = exporter;
        this.dbServer = dbServer;
        this.qastep = qaStep;
        this.user = user;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        try {
            prepare();
            exporter.export(file);
            complete();
        } catch (Exception e) {
            logError("Failed to export QA step : " + qastep.getName(), e);
            setStatus("Failed to export QA step " + qastep.getName() + ". Reason: " + e.getMessage());
        } finally {
            disconnect(dbServer);
        }
    }

    private void disconnect(DbServer dbServer) {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            logError("Failed to close a connetion. " + qastep.getName(), e);
            setStatus("Failed to close a connetion. Reason: " + e.getMessage());
        }
    }

    private void prepare() {
        setStatus("Started exporting QA step '" + qastep.getName() + "'");

    }

    private void complete() {
        setStatus("Completed exporting QA step '" + qastep.getName() + "'");
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("ExportQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

}

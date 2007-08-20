package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportQAStepTask implements Runnable {

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportQAStepTask.class);

    private Exporter exporter;

    private File file;

    private DbServer dbServer;

    private HibernateSessionFactory sessionFactory;

    public ExportQAStepTask(File file, Exporter exporter, DbServer dbServer, QAStep qaStep, User user,
            HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.exporter = exporter;
        this.dbServer = dbServer;
        this.qastep = qaStep;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        String suffix = "";
        try {
            suffix = suffix();
            prepare(suffix);
            exporter.export(file);
            complete(suffix);
        } catch (Exception e) {
            logError("Failed to export QA step : " + qastep.getName() + suffix, e);
            setStatus("Failed to export QA step " + qastep.getName() + suffix + ". Reason: " + e.getMessage());
        } finally {
            disconnect(dbServer); // Note: to disconnect db server from within the exporter (not obvious).
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

    private void prepare(String suffixMsg) {
        setStatus("Started exporting QA step '" + qastep.getName() + "'" + suffixMsg);

    }

    private void complete(String suffixMsg) {
        setStatus("Completed exporting QA step '" + qastep.getName() + "'" + suffixMsg);
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

    private String suffix() {
        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to "+
        file.getAbsolutePath();
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

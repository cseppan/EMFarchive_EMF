package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDao;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Status;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExportTask implements Runnable {
    private static Log log = LogFactory.getLog(ExportTask.class);

    private User user;

    private File file;

    private StatusServiceImpl statusServices;

    private LoggingServiceImpl loggingService;

    private EmfDataset dataset;

    private Exporter exporter;

    private AccessLog accesslog;

    private HibernateSessionFactory sessionFactory;

    protected ExportTask(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            Exporter exporter, HibernateSessionFactory sessionFactory) {
        this.user = user;
        this.file = file;
        this.dataset = dataset;
        this.statusServices = services.getStatus();
        this.loggingService = services.getLoggingService();
        this.exporter = exporter;
        this.accesslog = accesslog;
        this.sessionFactory = sessionFactory;
    }

    public void run() {
        try {
            setStartStatus();
            exporter.export(file);

            loggingService.setAccessLog(accesslog);
            updateDataset(dataset);
            setStatus("Completed export for " + dataset.getName() + ":" + file.getName());
        } catch (Exception e) {
            log.error("Problem on attempting to run Export on file : " + file, e);
            setStatus("Export failure." + e.getMessage());
        }
    }

    void updateDataset(EmfDataset dataset) throws EmfException {
        DatasetDao dao = new DatasetDao();
        try {
            Session session = sessionFactory.getSession();

            if (!dao.canUpdate(dataset, session))
                throw new EmfException("Dataset name already in use");

            dao.updateWithoutLocking(dataset, session);
            session.close();
        } catch (RuntimeException e) {
            log.error("Could not update Dataset - " + dataset.getName(), e);
            throw new EmfException("Could not update Dataset - " + dataset.getName());
        }
    }

    private void setStartStatus() {
        setStatus("Started export for " + dataset.getName() + ":" + file.getName());
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Export");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusServices.create(endStatus);
    }

}

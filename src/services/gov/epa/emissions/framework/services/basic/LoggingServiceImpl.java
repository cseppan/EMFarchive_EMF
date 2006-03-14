package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LoggingDAO;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class LoggingServiceImpl implements LoggingService {
    private static Log LOG = LogFactory.getLog(LoggingServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private LoggingDAO dao;

    public LoggingServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public LoggingServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new LoggingDAO();
    }

    public void setAccessLog(AccessLog accesslog) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.insertAccessLog(accesslog,session);
            session.close();

        } catch (RuntimeException e) {
            LOG.error("Could not insert access log - " + accesslog, e);
            throw new EmfException("Could not insert access log");
        }

    }

    public AccessLog[] getAccessLogs(long datasetid) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List allLogs = dao.getAccessLogs(datasetid, session);
            session.close();

            return (AccessLog[]) allLogs.toArray(new AccessLog[allLogs.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all access logs", e);
            throw new EmfException("Could not get all access logs");
        }

    }

}

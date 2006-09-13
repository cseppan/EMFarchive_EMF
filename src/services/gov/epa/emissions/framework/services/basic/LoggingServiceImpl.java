package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

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
            throw new EmfException("Could not insert access log - " + accesslog);
        }

    }

    public AccessLog[] getAccessLogs(int datasetid) throws EmfException {
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

    public String getLastExportedFileName(int datasetId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            String fileName = dao.getLastExportedFileName(datasetId, session);
            session.close();

            return fileName;
            
        } catch(EmfException e){
            throw e;
        }catch (RuntimeException e) {
            LOG.error("Could not get Last Exported File", e);
            throw new EmfException("Could not get all access logs");
        }

    }

}

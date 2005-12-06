package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.LoggingDAO;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingService;

import java.util.List;

import org.hibernate.Session;

public class LoggingServiceImpl implements LoggingService {

    private HibernateSessionFactory sessionFactory;

    public LoggingServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public LoggingServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setAccessLog(AccessLog accesslog) {
        Session session = sessionFactory.getSession();

        LoggingDAO.insertAccessLog(accesslog, session);
        session.flush();
        session.close();
    }

    public AccessLog[] getAccessLogs(long datasetid) {
        Session session = sessionFactory.getSession();

        List allLogs = LoggingDAO.getAccessLogs(datasetid, session);
        session.flush();
        session.close();

        return (AccessLog[]) allLogs.toArray(new AccessLog[allLogs.size()]);
    }

}

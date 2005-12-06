package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.LoggingDAO;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class LoggingServiceImpl implements LoggingService {

    private static Log log = LogFactory.getLog(LoggingServiceImpl.class);

	public LoggingServiceImpl() {
		super();
	}

	public void setAccessLog(AccessLog accesslog) {
        log.debug("Dataset Access log for " + accesslog.getUsername());
        Session session = EMFHibernateUtil.getSession();
        log.debug("Dataset Access: Before insertAccessLog");

        // FIXME: replace static w/ instance methods
        LoggingDAO.insertAccessLog(accesslog, session);
        session.flush();
        session.close();
        log.debug("Dataset Access: After insertAccessLog");
        log.debug("Dataset Access: setAccessLog for " + accesslog.getUsername());
	}

    public AccessLog[] getAccessLogs(long datasetid) {
        log.debug("get all access log entries for datasetid " + datasetid);
        Session session = EMFHibernateUtil.getSession();
        List allLogs = LoggingDAO.getAccessLogs(datasetid,session);
        session.flush();
        session.close();
        log.debug("Total number of messages in the List= " + allLogs.size());
        log.debug("get all access log entries for datasetid " + datasetid);
        return (AccessLog[]) allLogs.toArray(new AccessLog[allLogs.size()]);
    }

}

/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: LoggingServicesImpl.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.LoggingDAO;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingServices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class LoggingServicesImpl implements LoggingServices {

    private static Log log = LogFactory.getLog(LoggingServicesImpl.class);

	public LoggingServicesImpl() {
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

}

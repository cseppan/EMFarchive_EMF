/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: LoggingDAO.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.AccessLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LoggingDAO {
    private static Log log = LogFactory.getLog(StatusDAO.class);

	public static void insertAccessLog(AccessLog accesslog, Session session) {
        log.debug("Logging: insertAccessLog: " + accesslog.getUsername() + "\n" + session.toString());
        Transaction tx = session.beginTransaction();
        log.debug("Logging: insertAccessLog before session.save");
        session.save(accesslog);
        tx.commit();
        log.debug("Logging: insertAccessLog after session.save");
	}

}

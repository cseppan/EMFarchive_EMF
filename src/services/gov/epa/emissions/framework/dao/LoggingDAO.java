/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: LoggingDAO.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.AccessLog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LoggingDAO {
    private static Log log = LogFactory.getLog(StatusDAO.class);

    private static final String GET_ACCESS_LOG_QUERY = "from AccessLog as alog where alog.datasetid=:datasetid";

	public static void insertAccessLog(AccessLog accesslog, Session session) {
        log.debug("Logging: insertAccessLog: " + accesslog.getUsername() + "\n" + session.toString());
        Transaction tx = session.beginTransaction();
        log.debug("Logging: insertAccessLog before session.save");
        session.save(accesslog);
        tx.commit();
        log.debug("Logging: insertAccessLog after session.save");
	}

	public static List getAccessLogs(long datasetid, Session session){
		log.debug("In get access logs for datasetid= " + datasetid);

		ArrayList allLogs= new ArrayList();

        Transaction tx = session.beginTransaction();

        allLogs = (ArrayList)session.createQuery(GET_ACCESS_LOG_QUERY).setLong("datasetid",datasetid).list();
        tx.commit();
        log.debug("after call to allLogs. size of list= " + allLogs.size());		
		return allLogs;
	}
}

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.AccessLog;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class LoggingDAO {
    private static final String GET_ACCESS_LOG_QUERY = "from AccessLog as alog where alog.datasetId=:datasetid";

    public static void insertAccessLog(AccessLog accesslog, Session session) {
        Transaction tx = session.beginTransaction();
        session.save(accesslog);
        tx.commit();
    }

    public static List getAccessLogs(long datasetid, Session session) {
        Transaction tx = session.beginTransaction();
        List allLogs = session.createQuery(GET_ACCESS_LOG_QUERY).setLong("datasetid", datasetid).list();
        tx.commit();

        return allLogs;
    }
}

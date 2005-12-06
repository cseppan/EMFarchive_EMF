package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StatusDAO {
    private static Log log = LogFactory.getLog(StatusDAO.class);

    private static final String GET_STATUS_QUERY = "select stat from Status as stat where stat.username=:username";

    private static final String GET_READ_STATUS_QUERY = "select stat from Status as stat where stat.msgRead=true and stat.username=:username";

    // FIXME: Verify if exception needs to be thrown/caught here
    public static List getMessages(String userName, Session session) {
        log.debug("In getMessages");
        deleteMessages(userName, session);
        ArrayList allStatus = new ArrayList();

        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Query query = session.createQuery(GET_STATUS_QUERY);
            query.setParameter("username", userName, Hibernate.STRING);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Status aStatus = (Status) iter.next();
                aStatus.setMsgRead();
                allStatus.add(aStatus);
            }

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        log.debug("End getMessages");
        return allStatus;
    }// getMessages(uname)

    // FIXME: Verify if exception needs to be thrown/caught here
    public static void insertStatusMessage(Status status, Session session) {
        log.debug("StatusDAO: insertStatusMessage: " + status.getUsername() + "\n" + session.toString());
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            log.debug("StatusDAO: insertStatusMessage before session.save");
            session.save(status);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        log.debug("StatusDAO: insertStatusMessage after session.save");
    }

    // FIXME: Verify if exception needs to be thrown/caught here
    private static void deleteMessages(String userName, Session session) {
        log.debug("In deleteMessages");
        Transaction tx = null;

        try {
            Query query = session.createQuery(GET_READ_STATUS_QUERY);
            query.setParameter("username", userName, Hibernate.STRING);
            tx = session.beginTransaction();

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Status aStatus = (Status) iter.next();
                session.delete(aStatus);
            }
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        log.debug("End deleteMessages");

    }// deleteMessages

    // FIXME: Verify if exception needs to be thrown/caught here
    // FIXME: CORRECT HIBERNATE QUERY FOR TYPE
    public static List getMessages(String userName, String type, Session session) {
        log.debug("In getMessages");
        deleteMessages(userName, session);
        ArrayList allStatus = new ArrayList();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Query query = session.createQuery(GET_STATUS_QUERY);
            query.setParameter("username", userName, Hibernate.STRING);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Status aStatus = (Status) iter.next();
                aStatus.setMsgRead();
                allStatus.add(aStatus);
            }
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        log.debug("End getMessages");
        return allStatus;
    }

}// StatusDAO

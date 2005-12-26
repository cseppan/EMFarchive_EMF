package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StatusDAO {
    private static final String GET_STATUS_QUERY = "select stat from Status as stat where stat.username=:username";

    private static final String GET_READ_STATUS_QUERY = "select stat from Status as stat where stat.msgRead=true and stat.username=:username";

    public List getMessages(String userName, Session session) {
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
            tx.rollback();
            throw e;
        }
        return allStatus;
    }

    public void insertStatusMessage(Status status, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(status);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void deleteMessages(String userName, Session session) {
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
            tx.rollback();
            throw e;
        }
    }

}

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.Status;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class StatusDAO {

    public List all(String username, Session session) {
        removeRead(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username));
            List all1 = crit.list();
            tx.commit();

            return all1;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(Status status, Session session) {
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

    private void removeRead(String username, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username)).add(
                    Restrictions.eq("msgRead", Boolean.TRUE));

            List read = crit.list();
            for (Iterator iter = read.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
                session.delete(element);
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}

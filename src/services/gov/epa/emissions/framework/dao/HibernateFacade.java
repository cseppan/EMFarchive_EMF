package gov.epa.emissions.framework.dao;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class HibernateFacade {

    public void add(Object obj, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(long id, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Long(id)));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object current(long id, Class clazz, Session session) {
        Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Long(id)));
        return crit.uniqueResult();
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", name));
        return crit.uniqueResult() != null;
    }

}

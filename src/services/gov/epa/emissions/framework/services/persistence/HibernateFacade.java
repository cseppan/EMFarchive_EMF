package gov.epa.emissions.framework.services.persistence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
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

    public void add(Object[] objects, Session session) {
        for (int i = 0; i < objects.length; i++)
            add(objects[i], session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Integer(id)));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(Class clazz, Session session, Criterion[] criterions) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();

            return criteria.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object current(int id, Class clazz, Session session) {
        Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Integer(id)));
        return crit.uniqueResult();
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", name));
            boolean result = crit.uniqueResult() != null;
            tx.commit();

            return result;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void update(Object object, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void update(Object[] objects, Session session) {
        for (int i = 0; i < objects.length; i++)
            update(objects[i], session);
    }

    public void remove(Object obj, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    public List getAll(Class clazz, Order order, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).addOrder(order).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getAll(Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).list();
            tx.commit();

            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(String name, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", name));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List get(Class clazz, Criterion criterion, Order order, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).add(criterion).addOrder(order).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List get(Class clazz, Criterion criterion, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).add(criterion).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object load(Class clazz, Criterion criterion, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(criterion);
            tx.commit();
            return crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}

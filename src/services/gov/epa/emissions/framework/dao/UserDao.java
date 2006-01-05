package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class UserDao {

    private LockingScheme lockingScheme;

    public UserDao() {
        lockingScheme = new LockingScheme();
    }

    public List getAll(Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List all = session.createCriteria(User.class).list();
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(User user, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void remove(User user, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(user);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void update(User user, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public User get(String username, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(User.class).add(Restrictions.eq("username", username));
            tx.commit();

            return (User) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean contains(String username, Session session) {
        return get(username, session) != null;
    }

    public User obtainLocked(User lockOwner, User user, Session session) {
        return (User) lockingScheme.getLocked(lockOwner, user, session, getAll(session));
    }
}

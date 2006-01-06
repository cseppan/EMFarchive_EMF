package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDao {

    private LockingScheme lockingScheme;

    public DatasetDao() {
        lockingScheme = new LockingScheme();
    }

    public boolean exists(String name, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", name));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void updateDefaultVersion(long datasetId, int lastFinalVersion, Session session) {
        EmfDataset dataset = get(datasetId, session);
        dataset.setDefaultVersion(lastFinalVersion);
        updateWithoutLocking(dataset, session);
    }

    public EmfDataset get(long datasetId, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(
                    Restrictions.eq("datasetid", new Long(datasetId)));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List all(Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List all = session.createCriteria(EmfDataset.class).addOrder(Order.asc("name")).list();
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(EmfDataset dataset, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void updateWithoutLocking(EmfDataset dataset, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void remove(EmfDataset dataset, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public EmfDataset obtainLocked(User user, EmfDataset dataset, Session session) {
        return (EmfDataset) lockingScheme.getLocked(user, dataset, session, all(session));
    }

    public EmfDataset releaseLocked(EmfDataset locked, Session session) throws EmfException {
        return (EmfDataset) lockingScheme.releaseLock(locked, session, all(session));
    }

    public EmfDataset update(EmfDataset locked, Session session) throws EmfException {
        return (EmfDataset) lockingScheme.update(locked, session, all(session));
    }

}

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.EmfDataset;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDao {

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
        update(dataset, session);
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

    public void update(EmfDataset dataset, Session session) {
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

}

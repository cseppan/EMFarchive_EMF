package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDao {
    private static Log log = LogFactory.getLog(DatasetDao.class);

    private static final String GET_DATASET_FOR_DATASETNAME_QUERY = "select aDset from EmfDataset as aDset where aDset.name=:datasetname";

    /**
     * This method checks if the dataset name exists in the Datasets table A dataset name is unique in the EMF system.
     * If the name is already used by another dataset record then return true else return false.
     */
    public boolean isDatasetNameUsed(String datasetName, Session session) {
        boolean dsNameExists = false;
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Query query = session.createQuery(GET_DATASET_FOR_DATASETNAME_QUERY);
            query.setParameter("datasetname", datasetName, Hibernate.STRING);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                dsNameExists = true;
                break;
            }

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        return dsNameExists;
    }// getDataset

    public void updateDefaultVersion(long datasetId, int lastFinalVersion, Session session) {
        EmfDataset dataset = get(datasetId, session);
        dataset.setDefaultVersion(lastFinalVersion);
        updateDataset(dataset, session);
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

    public void insertDataset(EmfDataset dataset, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public void updateDataset(EmfDataset dataset, Session session) {
        log.debug("updating dataset: " + dataset.getDatasetid());
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.update(dataset);
            tx.commit();
            log.debug("updating dataset: " + dataset.getDatasetid());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public void deleteDataset(EmfDataset dataset, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.delete(dataset);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

}

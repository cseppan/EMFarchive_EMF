package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDao;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataServiceImpl implements DataService {
    private static Log LOG = LogFactory.getLog(DataServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DatasetDao dao;

    public DataServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public DataServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DatasetDao();
    }

    public EmfDataset[] getDatasets() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List datasets = dao.all(session);
            session.close();

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Datasets. Reason: " + e);
            throw new EmfException("Could not get all Datasets");
        }
    }

    public void addDataset(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(dataset, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not get add Dataset - " + dataset.getName() + ". Reason: " + e);
            throw new EmfException("Could not get add Dataset - " + dataset.getName());
        }
    }

    public void updateDataset(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.updateWithoutLocking(dataset, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not get update Dataset - " + dataset.getName() + ". Reason: " + e);
            throw new EmfException("Could not get update Dataset - " + dataset.getName());
        }
    }

    public void removeDataset(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(dataset, session);
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not get remove Dataset - " + dataset.getName() + ". Reason: " + e);
            throw new EmfException("Could not get remove Dataset - " + dataset.getName());
        }
    }

    public EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            session.close();

            return locked;
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: " + owner.getUsername()
                    + ".Reason: " + e);
            throw new EmfException("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            EmfDataset released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (HibernateException e) {
            LOG.error("Could not release lock for Dataset: " + locked.getName() + " by owner: " + locked.getLockOwner()
                    + ".Reason: " + e);
            throw new EmfException("Could not release lock for Dataset: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        }
    }
}

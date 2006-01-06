package gov.epa.emissions.framework.services.impl;

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
    private static Log log = LogFactory.getLog(DataServiceImpl.class);

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
        List datasets = null;
        try {
            Session session = sessionFactory.getSession();
            datasets = dao.all(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
    }

    public void insertDataset(EmfDataset aDataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.insertDataset(aDataset, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateDataset(EmfDataset aDset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.updateDataset(aDset, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public EmfDataset getDataset(long datasetId) throws EmfException {
        EmfDataset dataset = null;
        try {
            Session session = sessionFactory.getSession();
            dataset = dao.get(datasetId, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return dataset;
    }

    public void updateDefaultVersion(long datasetId, int lastFinalVersion) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.updateDefaultVersion(datasetId, lastFinalVersion, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    public void deleteDataset(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.deleteDataset(dataset, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }
}

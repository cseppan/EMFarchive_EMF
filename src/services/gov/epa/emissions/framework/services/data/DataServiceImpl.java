package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class DataServiceImpl implements DataService {
    private static Log LOG = LogFactory.getLog(DataServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DatasetDAO dao;

    public DataServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public DataServiceImpl(HibernateSessionFactory sessionFactory) {
        this(null, sessionFactory);
    }

    public DataServiceImpl(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DatasetDAO(dbServerFactory);
    }

    public synchronized EmfDataset[] getDatasets() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List datasets = dao.allNonDeleted(session);
            session.close();

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets", e);
            throw new EmfException("Could not get all Datasets");
        }
    }

    public synchronized EmfDataset getDataset(Integer datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = dao.getDataset(session, datasetId.intValue());
            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset with id=" + datasetId.intValue(), e);
            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset getDataset(String datasetName) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfDataset dataset = dao.getDataset(session, datasetName);

            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset " + datasetName, e);
            throw new EmfException("Could not get dataset " + datasetName);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public synchronized EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            EmfDataset released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Dataset: " + locked.getName() + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Dataset: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        }
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType type = dataset.getDatasetType();

            if (!dao.canUpdate(dataset, session))
                throw new EmfException("The Dataset name " + dataset.getName() + " is already in use.");

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset released = dao.update(dataset, session);
            session.close();

            return released;
        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + dataset.getName() + " " + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + e.getMessage());
        }
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List datasets = dao.getDatasets(session, datasetType);
            session.close();

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetType, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetType);
        }
    }

    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        String prefix = "DELETED_" + new Date().getTime() + "_";

        try {
            if (isRemovable(datasets, owner)) {
                for (int i = 0; i < datasets.length; i++) {
                    if (datasets[i].getStatus().equalsIgnoreCase("Deleted"))
                        continue;
                    datasets[i].setName(prefix + datasets[i].getName());
                    datasets[i].setStatus("Deleted");
                    updateDataset(datasets[i]);
                }
            }
        } catch (RuntimeException e) {
            LOG.error("Could not delete datasets: ", e);
            throw new EmfException("Could not delete datasets- " + e.getMessage());
        }
    }

    private synchronized boolean isRemovable(EmfDataset[] datasets, User owner) throws EmfException {
        for (int i = 0; i < datasets.length; i++) {
            checkUser(datasets[i], owner);
            checkCase(datasets[i]);
            checkControlStrategy(datasets[i]);
        }

        return true;
    }

    private synchronized void checkUser(EmfDataset dataset, User owner) throws EmfException {
        if (!owner.isAdmin() && !dataset.getCreator().equalsIgnoreCase(owner.getUsername())) {
            releaseLockedDataset(dataset);
            throw new EmfException("Cannot delete \"" + dataset.getName()
                    + "\" - you are not the creator of this dataset.");

        }
    }

    private synchronized void checkCase(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        if (dao.isUsedByCases(session, dataset))
            throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is used by a case.");
    }

    private synchronized void checkControlStrategy(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        if (dao.isUsedByControlStrategies(session, dataset))
            throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is use by a control strategy.");
    }

    public synchronized String[] getDatasetValues(Integer datasetId) throws EmfException {
        EmfDataset dataset = null;
        List<String> values = new ArrayList<String>();

        if (datasetId == null || datasetId.intValue() == 0)
            dataset = new EmfDataset();
        else
            dataset = getDataset(datasetId);

        values.add("name," + (dataset.getName() == null ? "" : dataset.getName()));
        values.add("datasetType," + (dataset.getDatasetTypeName() == null ? "" : dataset.getDatasetTypeName()));
        values.add("creator," + (dataset.getCreator() == null ? "" : dataset.getCreator()));
        values.add("createdDateTime," + (dataset.getCreatedDateTime() == null ? "" : dataset.getCreatedDateTime()));
        values.add("status," + (dataset.getStatus() == null ? "" : dataset.getStatus()));

        return values.toArray(new String[0]);
    }

}

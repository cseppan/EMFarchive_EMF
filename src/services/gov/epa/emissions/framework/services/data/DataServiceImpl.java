package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
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
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.allNonDeleted(session);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets", e);
            throw new EmfException("Could not get all Datasets");
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset released = dao.releaseLocked(locked, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Dataset: " + locked.getName() + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Dataset: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetType type = dataset.getDatasetType();

            if (!dao.canUpdate(dataset, session))
                throw new EmfException("The Dataset name " + dataset.getName() + " is already in use.");

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset released = dao.update(dataset, session);

            return released;
        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + dataset.getName() + " " + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetType);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetType, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetType);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetTypeId);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetTypeId, nameContaining);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
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
        } catch (Exception e) {
            LOG.error("Could not delete datasets: ", e);
            throw new EmfException(e.getMessage());
        }
    }

    private synchronized boolean isRemovable(EmfDataset[] datasets, User owner) throws EmfException {
        int len = datasets.length;
        int[] dsIDs = new int[len];

        for (int i = 0; i < len; i++) {
            checkUser(datasets[i], owner);
            dsIDs[i] = datasets[i].getId();
        }

        checkCase(dsIDs);
        checkControlStrategy(dsIDs);

        return true;
    }

    private synchronized void checkUser(EmfDataset dataset, User owner) throws EmfException {
        if (!owner.isAdmin() && !dataset.getCreator().equalsIgnoreCase(owner.getUsername())) {
            releaseLockedDataset(dataset);
            throw new EmfException("You are not the creator of " + dataset.getName() + ".");
        }
    }

    private synchronized void checkCase(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByCases(datasetIDs, session);
        } finally {
            session.close();
        }
    }

    private synchronized void checkControlStrategy(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByStrategies(datasetIDs, session);
        } finally {
            session.close();
        }
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

    public Version obtainedLockOnVersion(User user, int id) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.obtainLockOnVersion(user, id, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void updateVersionNReleaseLock(Version locked) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.updateVersionNReleaseLock(locked, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    public void purgeDeletedDatasets(User user) throws EmfException {
        Session session = this.sessionFactory.getSession();
        DbServer dbServer = DbServerFactory.get().getDbServer();

        try {
            List<EmfDataset> list = dao.deletedDatasets(user, session);
            dao.deleteDatasets(list.toArray(new EmfDataset[0]), dbServer, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
            closeDB(dbServer);
        }
    }

    private void closeDB(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int getNumOfDeletedDatasets(User user) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.deletedDatasets(user, session).size();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

}

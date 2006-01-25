package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.LockableVersions;
import gov.epa.emissions.framework.dao.LockingScheme;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataAccessorImpl implements DataAccessor {
    private Log LOG = LogFactory.getLog(DataAccessorImpl.class);

    private DataUpdatesCache cache;

    private HibernateSessionFactory sessionFactory;

    private Versions versions;

    private LockableVersions lockableVersions;

    private PageFetch pageFetch;

    public DataAccessorImpl(DataUpdatesCache cache, HibernateSessionFactory sessionFactory) {
        this.cache = cache;
        this.sessionFactory = sessionFactory;
        versions = new Versions();
        lockableVersions = new LockableVersions(versions);
        pageFetch = new PageFetch(cache, sessionFactory);
    }

    public int defaultPageSize() {
        Session session = sessionFactory.getSession();
        int result = cache.defaultPageSize(session);
        session.close();

        return result;
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        return pageFetch.getPage(token, pageNumber);
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        return pageFetch.getPageCount(token);
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        return pageFetch.getPageWithRecord(token, record);
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        return pageFetch.getTotalRecords(token);
    }

    public Version currentVersion(Version reference) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version current = versions.current(reference, session);
            session.close();

            return current;
        } catch (HibernateException e) {
            LOG.error("Could not load current version of Dataset : " + reference.getDatasetId() + ". Reason: " + e);
            throw new EmfException("Could not load current version of Dataset : " + reference.getDatasetId());
        }
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version[] results = versions.get(datasetId, session);
            session.close();

            return results;
        } catch (HibernateException e) {
            LOG.error("Could not get all versions of Dataset : " + datasetId + ". Reason: " + e);
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        }
    }

    public void shutdown() throws EmfException {
        try {
            cache.invalidate();
        } catch (SQLException e) {
            LOG.error("Could not close DataView Service. Reason: " + e.getMessage());
            throw new EmfException("Could not close DataView Service");
        }
    }

    public DataAccessToken openSession(DataAccessToken token, int pageSize) throws Exception {
        Session session = sessionFactory.getSession();
        cache.init(token, pageSize, session);
        session.close();

        return token;
    }

    public DataAccessToken openSession(DataAccessToken token) throws Exception {
        Session session = sessionFactory.getSession();
        cache.init(token, session);
        session.close();

        return token;
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.close(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not close Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not close Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token) throws Exception {
        return openEditSession(user, token, defaultPageSize());
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token, int pageSize) throws Exception {
        obtainLock(user, token);
        if (!token.isLocked(user))
            return token;// abort

        token = openSession(token, pageSize);

        return token;
    }

    private void obtainLock(User user, DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Version current = currentVersion(token.getVersion());
            Version locked = lockableVersions.obtainLocked(user, current, session);
            token.setVersion(locked);
            token.setLockTimeInterval(lockTimeInterval(session));

            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock of Dataset " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not obtain lock of Dataset " + token.datasetId());
        }
    }

    private long lockTimeInterval(Session session) {
        return new LockingScheme().timeInterval(session);
    }

    public DataAccessToken closeEditSession(DataAccessToken token) throws EmfException {
        if (!isLockOwned(token))
            throw new EmfException("Cannot unlock unless locked");

        releaseLock(token);
        closeSession(token);

        return token;
    }

    private void releaseLock(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version unlocked = lockableVersions.releaseLocked(token.getVersion(), session);
            token.setVersion(unlocked);
            token.setLockTimeInterval(lockTimeInterval(session));

            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not release lock of Dataset " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not release lock of Dataset " + token.datasetId());
        }
    }

    public boolean isLockOwned(DataAccessToken token) throws EmfException {
        Version version = token.getVersion();
        Version current = currentVersion(version);
        return current.isLocked(version.getLockOwner());
    }

    public DataAccessToken renewLock(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version extended = lockableVersions.renewLockOnUpdate(token.getVersion(), session);
            token.setVersion(extended);

            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not extend lock of Dataset " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not extend lock of Dataset " + token.datasetId());
        }

        return token;
    }

}

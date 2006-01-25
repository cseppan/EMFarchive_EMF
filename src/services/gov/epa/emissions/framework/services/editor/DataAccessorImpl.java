package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataAccessorImpl implements DataAccessor {
    private Log LOG = LogFactory.getLog(DataAccessorImpl.class);

    private DataUpdatesCache cache;

    private PageFetch pageFetch;

    private SessionLifecycle sessionLifecycle;

    public DataAccessorImpl(DataUpdatesCache cache, HibernateSessionFactory sessionFactory) {
        this.cache = cache;
        pageFetch = new PageFetch(cache, sessionFactory);
        sessionLifecycle = new SessionLifecycle(cache, sessionFactory);
    }

    public int defaultPageSize() {
        return pageFetch.defaultPageSize();
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
        return sessionLifecycle.currentVersion(reference);
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        return sessionLifecycle.getVersions(datasetId);
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
        return sessionLifecycle.open(token, pageSize);
    }

    public DataAccessToken openSession(DataAccessToken token) throws Exception {
        return sessionLifecycle.open(token);
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        sessionLifecycle.close(token);
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token) throws Exception {
        return openEditSession(user, token, defaultPageSize());
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token, int pageSize) throws Exception {
        return sessionLifecycle.openEdit(user, token, pageSize);
    }

    public DataAccessToken closeEditSession(DataAccessToken token) throws EmfException {
        return sessionLifecycle.closeEdit(token);
    }

    public boolean isLockOwned(DataAccessToken token) throws EmfException {
        return sessionLifecycle.isLockOwned(token);
    }

    public DataAccessToken renewLock(DataAccessToken token) throws EmfException {
        return sessionLifecycle.renewLock(token);
    }

}

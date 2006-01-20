package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.LockingScheme;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataAccessServiceImpl {
    private Log LOG = LogFactory.getLog(DataAccessServiceImpl.class);

    private DataAccessCache cache;

    private HibernateSessionFactory sessionFactory;

    private Versions versions;

    public DataAccessServiceImpl(DataAccessCache cache, HibernateSessionFactory sessionFactory) {
        this.cache = cache;
        this.sessionFactory = sessionFactory;
        versions = new Versions();
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        RecordsFilter filter = new RecordsFilter();

        PageReader reader = cache.reader(token);
        try {
            Page page = reader.page(pageNumber);
            Session session = sessionFactory.getSession();
            List changesets = cache.changesets(token, pageNumber, session);
            session.close();

            return filter.filter(page, changesets);
        } catch (Exception e) {
            LOG.error("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId());
        }
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalPages();
        } catch (SQLException e) {
            LOG.error("Failed to get page count. Reason: " + e.getMessage());
            throw new EmfException("Failed to get page count");
        }
    }

    public Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            LOG.error("Could not obtain the page with Record: " + recordId + ". Reason: " + ex.getMessage());
            throw new EmfException("Could not obtain the page with Record: " + recordId);
        }
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalRecords();
        } catch (SQLException e) {
            LOG.error("Failed to get a count of total number of records. Reason: " + e.getMessage());
            throw new EmfException("Failed to get a count of total number of records");
        }
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

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.init(token, session);
            session.close();

            return token;
        } catch (SQLException e) {
            LOG.error("Could not initialize Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not initialize Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
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

    public DataAccessToken openEditSession(DataAccessToken token) throws EmfException {
        token = openSession(token);
        obtainLock(token);

        return token;
    }

    private void obtainLock(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            token.setVersion(currentVersion(token.getVersion()));
            token.setLockTimeInterval(lockTimeInterval(session));
            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock for Dataset " + token.datasetId() + ". Reason: " + e);
            throw new EmfException("Could not obtain lock for Dataset " + token.datasetId());
        }
    }

    private long lockTimeInterval(Session session) {
        return new LockingScheme().timeInterval(session);
    }
}

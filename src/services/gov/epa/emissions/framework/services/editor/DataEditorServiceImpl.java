package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.impl.EmfServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataEditorServiceImpl extends EmfServiceImpl implements DataEditorService {
    private static final Log LOG = LogFactory.getLog(DataEditorServiceImpl.class);

    private Versions versions;

    private VersionedRecordsReader reader;

    private DataAccessCache cache;

    private HibernateSessionFactory sessionFactory;

    private DataAccessor accessor;

    public DataEditorServiceImpl() throws Exception {
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("could not initialize Data Editor Service", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DataEditorServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory)
            throws Exception {
        super(datasource, dbServer);
        init(dbServer, dbServer.getEmissionsDatasource(), sessionFactory);
    }

    private void init(DbServer dbServer, Datasource datasource, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        versions = new Versions();
        reader = new DefaultVersionedRecordsReader(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        cache = new DataAccessCacheImpl(reader, writerFactory, datasource, dbServer.getSqlDataTypes());

        accessor = new DataAccessorImpl(cache, sessionFactory);
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        return accessor.getPage(token, pageNumber);
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        return accessor.getPageCount(token);
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        return accessor.getPageWithRecord(token, record);
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        return accessor.getTotalRecords(token);
    }

    public Version derive(Version base, String name) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version derived = versions.derive(base, name, session);
            session.close();

            return derived;
        } catch (HibernateException e) {
            LOG.error("Could not derive a new Version from the base Version: " + base.getVersion() + " of Dataset: "
                    + base.getDatasetId() + ". Reason: " + e);
            throw new EmfException("Could not derive a new Version from the base Version: " + base.getVersion()
                    + " of Dataset: " + base.getDatasetId());
        }
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.submitChangeSet(token, changeset, pageNumber, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not submit changes for Dataset: " + token.datasetId() + ". Version: " + token.getVersion()
                    + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not submit changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion());
        }
    }

    public void discard(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.discardChangeSets(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not discard changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage(), e);
            throw new EmfException("Could not discard changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage());
        }
    }

    public DataAccessToken save(DataAccessToken token) throws EmfException {
        if (!accessor.isLockOwned(token)) {
            Version current = accessor.currentVersion(token.getVersion());
            throw new EmfException("Cannot save as the lock is currently owned by " + current.getLockOwner()
                    + ", obtained at " + current.getDate());
        }

        DataAccessToken extended = accessor.renewLock(token);
        return doSave(extended, cache, sessionFactory);
    }

    private DataAccessToken doSave(DataAccessToken token, DataAccessCache cache,
            HibernateSessionFactory hibernateSessionFactory) throws EmfException {
        try {
            Session session = hibernateSessionFactory.getSession();
            cache.save(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion() + "\t" + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion());
        }

        return token;
    }

    Version doMarkFinal(Version derived) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version version = versions.markFinal(derived, session);
            session.close();

            return version;
        } catch (HibernateException e) {
            LOG.error("Could not mark a derived Version: " + derived.getDatasetId() + " as Final" + ". Reason: " + e);
            throw new EmfException("Could not mark a derived Version: " + derived.getDatasetId() + " as Final");
        }
    }

    public Version markFinal(DataAccessToken token) throws EmfException {
        Version derived = token.getVersion();
        Version current = accessor.currentVersion(derived);
        if (current.isLocked())
            throw new EmfException("Cannot mark Version: " + derived.getName() + " Final as it is locked by "
                    + current.getLockOwner());

        return doMarkFinal(derived);
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        return accessor.getVersions(datasetId);
    }

    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
        return openSession(user, token, accessor.defaultPageSize());
    }

    public DataAccessToken openSession(User user, DataAccessToken token, int pageSize) throws EmfException {
        Version current = accessor.currentVersion(token.getVersion());
        if (current.isFinalVersion())
            throw new EmfException("Can only edit non-final Version.");

        try {
            return accessor.openEditSession(user, token, pageSize);
        } catch (Exception e) {
            LOG.error("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        accessor.closeEditSession(token);
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        accessor.shutdown();
        super.finalize();
    }

    public boolean hasChanges(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            boolean result = cache.hasChanges(token, session);
            session.close();

            return result;
        } catch (Exception e) {
            Version version = token.getVersion();
            LOG.error("Could not confirm changes for Version: " + version.getDatasetId() + ". Reason: " + e);
            throw new EmfException("Could not confirm changes for Version: " + version.getDatasetId());
        }
    }

}

package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.InfrastructureException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataEditorServiceImpl extends EmfServiceImpl implements DataEditorService {
    private static final Log LOG = LogFactory.getLog(DataEditorServiceImpl.class);

    private Versions versions;

    private VersionedRecordsFactory factory;

    private DataAccessCache cache;

    private HibernateSessionFactory sessionFactory;

    private DataAccessor accessor;

    public DataEditorServiceImpl() throws Exception {
        super("Data Editor Service");
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("Could not initialize Data Editor Service", ex);
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
        factory = new DefaultVersionedRecordsFactory(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        cache = new DataAccessCacheImpl(factory, writerFactory, datasource, dbServer.getSqlDataTypes());

        accessor = new DataAccessorImpl(cache, sessionFactory);
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        accessor.applyConstraints(token, null, rowFilter, sortOrder);
        return getPage(token, 1);
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

    public Version derive(Version base, User user, String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Version derived = versions.derive(base, name, user, session);
            return derived;
        } catch (HibernateException e) {
            LOG.error("Could not derive a new Version from the base Version: " + base.getVersion() + " of Dataset: "
                    + base.getDatasetId(), e);
            throw new EmfException("Could not create a new Version using " + base.getVersion() + " as the base");
        }finally{
            session.close();
        }
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.submitChangeSet(token, changeset, pageNumber, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not submit changes for Dataset: " + token.datasetId() + ". Version: " + token.getVersion()
                    + "." + e.getMessage(), e);
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

    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws EmfException {
        try {
            if (!accessor.isLockOwned(token))
                return token;// abort

            DataAccessToken extended = accessor.renewLock(token);
            return doSave(extended, cache, sessionFactory, dataset);
        } catch (Exception e) {
            LOG.error("Could not save changes for Dataset: " + token.datasetId() + ". Version: " + token.getVersion()
                    + "\t" + e.getMessage(), e);
            throw new EmfException("Could not save changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage());
        }
    }

    private DataAccessToken doSave(DataAccessToken token, DataAccessCache cache,
            HibernateSessionFactory hibernateSessionFactory, EmfDataset dataset) throws EmfException {
        try {
            saveDataEditChanges(token, cache, hibernateSessionFactory);

            updateDataset(hibernateSessionFactory, dataset);

        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion() + "\t" + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion());
        }

        return token;
    }

    private void saveDataEditChanges(DataAccessToken token, DataAccessCache cache,
            HibernateSessionFactory hibernateSessionFactory) throws Exception {
        Session session = hibernateSessionFactory.getSession();
        try {
            cache.save(token, session);
        } finally {
            session.close();
        }
    }

    private void updateDataset(HibernateSessionFactory hibernateSessionFactory, EmfDataset dataset) throws Exception {
        Session session = hibernateSessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            dao.updateWithoutLocking(dataset, session);
        } finally {
            session.close();
        }
    }

    void updateVersion(Version version) {
        Session session = sessionFactory.getSession();
        try {
            versions.save(version, session);
        } finally {
            session.close();
        }
    }

    Version doMarkFinal(Version derived) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version version = versions.markFinal(derived, session);
            session.close();

            return version;
        } catch (HibernateException e) {
            LOG.error("Could not mark a derived Version: " + derived.getDatasetId() + " as Final" + "." + e);
            throw new EmfException("Could not mark a derived Version: " + derived.getDatasetId() + " as Final");
        }
    }

    public Version markFinal(DataAccessToken token) throws EmfException {
        Version derived = token.getVersion();
        Version current = accessor.currentVersion(derived);
        if (current.isLocked() && !derived.isLocked(current.getLockOwner()))
            throw new EmfException("Cannot mark Version " + derived.getName() + " Final as it is locked by "
                    + current.getLockOwner());

        return doMarkFinal(derived);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
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
                    + token.getVersion().getVersion() + "." + e.getMessage(), e);
            throw new EmfException("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void closeSession(User user, DataAccessToken token) throws EmfException {
        try {
            accessor.closeEditSession(user, token);
        } finally {
            new PerformanceMetrics().gc("Closing Data Editor session - (" + token + ")");
        }
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
            LOG.error("Could not confirm changes for Version: " + version.getDatasetId() + "." + e);
            throw new EmfException("Could not confirm changes for Version: " + version.getDatasetId());
        }
    }

    public TableMetadata getTableMetadata(String table) throws EmfException {
        try {
            TableDefinition definition = dbServer.getEmissionsDatasource().tableDefinition();
            return definition.getTableMetaData(table);
        } catch (SQLException e) {
            LOG.error("Database error. Failed to get table metadata for table: ", e);
            throw new EmfException("Database error");
        }

    }

}

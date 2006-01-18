package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.impl.EmfServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataViewServiceImpl extends EmfServiceImpl implements DataViewService {
    private static Log LOG = LogFactory.getLog(DataViewServiceImpl.class);

    private Versions versions;

    private VersionedRecordsReader reader;

    private DataEditorServiceCache cache;

    private HibernateSessionFactory sessionFactory;

    public DataViewServiceImpl() throws Exception {
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("could not initialize Data Editor Service", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DataViewServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory)
            throws Exception {
        super(datasource, dbServer);
        init(dbServer, dbServer.getEmissionsDatasource(), sessionFactory);
    }

    private void init(DbServer dbServer, Datasource datasource, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        versions = new Versions();
        reader = new DefaultVersionedRecordsReader(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        cache = new DataEditorServiceCache(reader, writerFactory, datasource, dbServer.getSqlDataTypes());
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
            LOG.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            LOG.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalRecords();
        } catch (SQLException e) {
            LOG.error("Failed to get total records count: " + e.getMessage());
            throw new EmfException(e.getMessage());
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

    public void close() throws EmfException {
        // TODO: release/flush locks?
        try {
            cache.invalidate();
        } catch (SQLException e) {
            LOG.error("Could not close DataEditor Service. Reason: " + e.getMessage());
            throw new EmfException("Could not close DataEditor Service");
        }
    }

    public DataAccessToken openSession(DataAccessToken token, int pageSize) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.init(token, pageSize, session);
            session.close();

            return token;
        } catch (SQLException e) {
            LOG.error("Could not initialize editing Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not initialize editing Session for Dataset: " + token.datasetId()
                    + ", Version: " + token.getVersion().getVersion());
        }
    }

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.init(token, session);
            session.close();

            return token;
        } catch (SQLException e) {
            LOG.error("Could not initialize editing Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not initialize editing Session for Dataset: " + token.datasetId()
                    + ", Version: " + token.getVersion().getVersion());
        }
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.close(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not close editing Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not close editing Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage());
        }
    }

}

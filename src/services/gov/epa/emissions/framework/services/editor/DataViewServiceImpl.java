package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.impl.EmfServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataViewServiceImpl extends EmfServiceImpl implements DataViewService {
    private static Log LOG = LogFactory.getLog(DataViewServiceImpl.class);

    private DataAccessor accessor;

    public DataViewServiceImpl() throws Exception {
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("could not initialize DataView Service", ex);
            throw new InfrastructureException("could not initialize DataView Service");
        }
    }

    public DataViewServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory)
            throws Exception {
        super(datasource, dbServer);
        init(dbServer, dbServer.getEmissionsDatasource(), sessionFactory);
    }

    private void init(DbServer dbServer, Datasource datasource, HibernateSessionFactory sessionFactory) {
        VersionedRecordsReader reader = new DefaultVersionedRecordsReader(datasource);
        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        DataAccessCacheImpl cache = new DataAccessCacheImpl(reader, writerFactory, datasource, dbServer
                .getSqlDataTypes());

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

    public Version[] getVersions(long datasetId) throws EmfException {
        return accessor.getVersions(datasetId);
    }

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
        Version current = accessor.currentVersion(token.getVersion());
        if (!current.isFinalVersion())
            throw new EmfException("Can only view a final Version.");

        try {
            return accessor.openSession(token);
        } catch (Exception e) {
            LOG.error("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + ". Reason: " + e.getMessage(), e);
            throw new EmfException("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        accessor.closeSession(token);
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        accessor.shutdown();
        super.finalize();
    }

}

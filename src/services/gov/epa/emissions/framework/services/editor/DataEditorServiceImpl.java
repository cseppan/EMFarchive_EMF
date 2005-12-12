package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EditToken;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServiceImpl implements DataEditorService {
    private static Log log = LogFactory.getLog(DataEditorServiceImpl.class);

    private Datasource datasource;

    private Versions versions;

    private VersionedRecordsReader reader;

    private DataEditorCachePolicy cache;

    public DataEditorServiceImpl() throws InfrastructureException {
        try {
            Context ctx = new InitialContext();

            DataSource emfDatabase = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            DbServer dbServer = new PostgresDbServer(emfDatabase.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                    EMFConstants.EMF_EMISSIONS_SCHEMA);

            init(dbServer);
        } catch (Exception ex) {
            log.error("could not initialize Data Editor Service", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DataEditorServiceImpl(DbServer dbServer) throws SQLException {
        init(dbServer);
    }

    public Page getPage(EditToken token, int pageNumber) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.page(pageNumber);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getPageCount(EditToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalPages();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Page getPageWithRecord(EditToken token, int recordId) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getTotalRecords(EditToken token) throws EmfException {
        try {
            PageReader reader = cache.reader(token);
            return reader.totalRecords();
        } catch (SQLException e) {
            log.error("Failed to get total records count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Version derive(Version baseVersion, String name) throws EmfException {
        try {
            return versions.derive(baseVersion, name);
        } catch (SQLException e) {
            throw new EmfException("Could not derive a new Version from the base Version: " + baseVersion.getVersion()
                    + " of Dataset: " + baseVersion.getDatasetId());
        }
    }

    public void submit(EditToken token, ChangeSet changeset) {
        List list = cache.changesets(token);
        list.add(changeset);
    }

    public void discard(EditToken token) {
        cache.discardChangeSets(token);
    }

    public void save(EditToken token) throws EmfException {
        try {
            VersionedRecordsWriter writer = cache.writer(token);
            List list = cache.changesets(token);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                ChangeSet element = (ChangeSet) iter.next();
                writer.update(element);
            }
        } catch (Exception e) {
            throw new EmfException("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion());
        }

        discard(token);
    }

    public Version markFinal(Version derived) throws EmfException {
        try {
            return versions.markFinal(derived);
        } catch (SQLException e) {
            throw new EmfException("Could not mark a derived Version: " + derived.getDatasetId() + " as Final");
        }
    }

    public Version[] getVersions(long datasetId) throws EmfException {
        try {
            return versions.get(datasetId);
        } catch (SQLException e) {
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        }
    }

    public void close() throws EmfException {
        invalidateCache();

        try {
            versions.close();
            reader.close();
        } catch (SQLException e) {
            log.error("Could not close Versions & VersionedRecordsReader due to: " + e.getMessage());
            throw new EmfException("Could not close Versions & VersionedRecordsReader", e.getMessage());
        }
    }

    private void init(DbServer dbServer) throws SQLException {
        this.datasource = dbServer.getEmissionsDatasource();

        versions = new Versions(datasource);
        reader = new VersionedRecordsReader(datasource);
        cache = new DataEditorCachePolicy(reader, datasource, dbServer.getSqlDataTypes());
    }

    private void invalidateCache() throws EmfException {
        try {
            cache.invalidate();
        } catch (SQLException e) {
            log.error("Could not invalidate cache due to " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EditToken;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataEditorServiceImpl implements DataEditorService {
    private static Log log = LogFactory.getLog(DataEditorServiceImpl.class);

    private Datasource datasource;

    private Map pageReadersMap;

    private Versions versions;

    private SqlDataTypes sqlTypes;

    private Map writersMap;

    public DataEditorServiceImpl() throws InfrastructureException {
        try {
            Context ctx = new InitialContext();

            DataSource emfDatabase = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            DbServer dbServer = new PostgresDbServer(emfDatabase.getConnection(), EMFConstants.EMF_REFERENCE_SCHEMA,
                    EMFConstants.EMF_EMISSIONS_SCHEMA);

            init(dbServer);
        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DataEditorServiceImpl(DbServer dbServer) throws SQLException {
        init(dbServer);
    }

    public Page getPage(String tableName, int pageNumber) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.page(pageNumber);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getPageCount(String tableName) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.totalPages();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Page getPageWithRecord(String tableName, int recordId) throws EmfException {
        try {
            PageReader reader = getReader(tableName);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getTotalRecords(EditToken token) throws EmfException {
        try {
            PageReader reader = getReader(token.getTable());
            return reader.totalRecords();
        } catch (SQLException e) {
            log.error("Failed to get total records count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Version derive(Version baseVersion) throws EmfException {
        try {
            return versions.derive(baseVersion);
        } catch (SQLException e) {
            throw new EmfException("Could not derive a new Version from the base Version: " + baseVersion.getVersion()
                    + " of Dataset: " + baseVersion.getDatasetId());
        }
    }

    public void submit(EditToken token, ChangeSet changeset) throws EmfException {
        try {
            VersionedRecordsWriter writer = getWriter(token);
            writer.update(changeset);
        } catch (Exception e) {
            throw new EmfException("Could not update Dataset: " + token.getDatasetId() + " with changes for Version: "
                    + token.getVersion());
        }
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
        closePageReaders();

        try {
            versions.close();
        } catch (SQLException e) {
            log.error("Could not close Versions due to: " + e.getMessage());
            throw new EmfException("Could not close Versions", e.getMessage());
        }
    }

    private void init(DbServer dbServer) throws SQLException {
        pageReadersMap = new HashMap();
        writersMap = new HashMap();

        this.datasource = dbServer.getEmissionsDatasource();
        sqlTypes = dbServer.getSqlDataTypes();
        versions = new Versions(datasource);
    }

    private PageReader getReader(String tableName) throws SQLException {
        if (!pageReadersMap.containsKey(tableName)) {
            String query = "SELECT * FROM " + datasource.getName() + "." + tableName;
            ScrollableVersionedRecords sr = new ScrollableVersionedRecords(datasource, query);
            PageReader reader = new PageReader(20, sr);

            pageReadersMap.put(tableName, reader);
        }

        return (PageReader) pageReadersMap.get(tableName);
    }

    private void closePageReaders() throws EmfException {
        Collection all = pageReadersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            try {
                PageReader pageReader = (PageReader) iter.next();
                pageReader.close();
            } catch (SQLException e) {
                log.error("Could not close 'query session' due to " + e.getMessage());
                throw new EmfException(e.getMessage());
            }
        }

        pageReadersMap.clear();
    }

    /**
     * This method is for cleaning up session specific objects within this
     * service.
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    private VersionedRecordsWriter getWriter(EditToken token) throws SQLException {
        Object key = key(token);
        if (!writersMap.containsKey(key)) {
            VersionedRecordsWriter writer = new VersionedRecordsWriter(datasource, token.getTable(), sqlTypes);
            writersMap.put(key, writer);
        }

        return (VersionedRecordsWriter) writersMap.get(key);
    }

    private Object key(EditToken token) {
        return "D:" + token.getDatasetId() + "V:" + token.getVersion() + "T:" + token.getTable();
    }

}

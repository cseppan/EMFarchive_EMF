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
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
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

    private Map readersMap;

    private Versions versions;

    private SqlDataTypes sqlTypes;

    private Map writersMap;

    private VersionedRecordsReader reader;

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
            PageReader reader = getReader(token);
            return reader.page(pageNumber);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getPageCount(EditToken token) throws EmfException {
        try {
            PageReader reader = getReader(token);
            return reader.totalPages();
        } catch (SQLException e) {
            log.error("Failed to get page count: " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public Page getPageWithRecord(EditToken token, int recordId) throws EmfException {
        try {
            PageReader reader = getReader(token);
            return reader.pageByRecord(recordId);
        } catch (SQLException ex) {
            log.error("Initialize reader: " + ex.getMessage());
            throw new EmfException("Page Reader error: " + ex.getMessage());
        }
    }

    public int getTotalRecords(EditToken token) throws EmfException {
        try {
            PageReader reader = getReader(token);
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

    public void submit(EditToken token, ChangeSet changeset) throws EmfException {
        try {
            VersionedRecordsWriter writer = getWriter(token);
            writer.update(changeset);
        } catch (Exception e) {
            throw new EmfException("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
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
        closeReaders();
        closeWriters();

        try {
            versions.close();
            reader.close();
        } catch (SQLException e) {
            log.error("Could not close Versions & VersionedRecordsReader due to: " + e.getMessage());
            throw new EmfException("Could not close Versions & VersionedRecordsReader", e.getMessage());
        }
    }

    private void init(DbServer dbServer) throws SQLException {
        readersMap = new HashMap();
        writersMap = new HashMap();

        this.datasource = dbServer.getEmissionsDatasource();
        sqlTypes = dbServer.getSqlDataTypes();

        versions = new Versions(datasource);
        reader = new VersionedRecordsReader(datasource);
    }

    private PageReader getReader(EditToken token) throws SQLException {
        Object key = token.key();
        if (!readersMap.containsKey(key)) {
            ScrollableVersionedRecords records = reader.fetch(token.getVersion(), token.getTable());
            PageReader reader = new PageReader(20, records);

            readersMap.put(key, reader);
        }

        return (PageReader) readersMap.get(key);
    }

    private void closeReaders() throws EmfException {
        Collection all = readersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            try {
                PageReader pageReader = (PageReader) iter.next();
                pageReader.close();
            } catch (SQLException e) {
                log.error("Could not close 'query session' due to " + e.getMessage());
                throw new EmfException(e.getMessage());
            }
        }

        readersMap.clear();
    }

    private VersionedRecordsWriter getWriter(EditToken token) throws SQLException {
        Object key = token.key();
        if (!writersMap.containsKey(key)) {
            VersionedRecordsWriter writer = new VersionedRecordsWriter(datasource, token.getTable(), sqlTypes);
            writersMap.put(key, writer);
        }

        return (VersionedRecordsWriter) writersMap.get(key);
    }

    private void closeWriters() throws EmfException {
        Collection all = writersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            try {
                VersionedRecordsWriter writer = (VersionedRecordsWriter) iter.next();
                writer.close();
            } catch (SQLException e) {
                log.error("Could not close 'write session' due to " + e.getMessage());
                throw new EmfException(e.getMessage());
            }
        }

        writersMap.clear();
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

}

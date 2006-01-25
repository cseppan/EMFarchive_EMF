package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.dao.EmfProperties;
import gov.epa.emissions.framework.dao.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.impl.EmfProperty;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;

public class DataAccessCache implements DataUpdatesCache {

    private Map readersMap;

    private VersionedRecordsReader recordsReader;

    private Map writersMap;

    private Datasource datasource;

    private SqlDataTypes sqlTypes;

    private Map changesetsMap;

    private VersionedRecordsWriterFactory writerFactory;

    private EmfProperties properties;

    public DataAccessCache(VersionedRecordsReader reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes) {
        this(reader, writerFactory, datasource, sqlTypes, new EmfPropertiesDAO());
    }

    public DataAccessCache(VersionedRecordsReader reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes, EmfProperties properties) {
        this.properties = properties;

        recordsReader = reader;
        this.writerFactory = writerFactory;
        this.datasource = datasource;
        this.sqlTypes = sqlTypes;

        readersMap = new HashMap();
        writersMap = new HashMap();
        changesetsMap = new HashMap();
    }

    public PageReader reader(DataAccessToken token) {
        return (PageReader) readersMap.get(token.key());

    }

    public VersionedRecordsWriter writer(DataAccessToken token) {
        return (VersionedRecordsWriter) writersMap.get(token.key());
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws SQLException {
        Map map = pageChangesetsMap(token, session);
        Integer pageKey = pageChangesetsKey(pageNumber);
        if (!map.containsKey(pageKey)) {
            map.put(pageKey, new ChangeSets());
        }

        return (ChangeSets) map.get(pageKey);
    }

    public void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session)
            throws SQLException {
        ChangeSets sets = changesets(token, pageNumber, session);
        sets.add(changeset);
    }

    public void discardChangeSets(DataAccessToken token, Session session) throws SQLException {
        Map pageChangsetsMap = pageChangesetsMap(token, session);
        pageChangsetsMap.clear();
    }

    public ChangeSets changesets(DataAccessToken token, Session session) throws SQLException {
        ChangeSets all = new ChangeSets();

        Map pageChangesetsMap = pageChangesetsMap(token, session);
        Set keys = new TreeSet(pageChangesetsMap.keySet());
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            ChangeSets sets = (ChangeSets) pageChangesetsMap.get(iter.next());
            all.add(sets);
        }

        return all;
    }

    public void init(DataAccessToken token, Session session) throws SQLException {
        init(token, defaultPageSize(session), session);
    }

    public int defaultPageSize(Session session) {
        EmfProperty pageSize = properties.getProperty("page-size", session);
        return Integer.parseInt(pageSize.getValue());
    }

    public void init(DataAccessToken token, int pageSize, Session session) throws SQLException {
        initChangesetsMap(token);
        initReader(token, pageSize, session);
        initWriter(token);
    }

    public void invalidate() throws SQLException {
        closeReaders();
        closeWriters();
        changesetsMap.clear();
    }

    public void reload(DataAccessToken token, Session session) throws SQLException {
        close(token, session);
        init(token, session);
    }

    public void close(DataAccessToken token, Session session) throws SQLException {
        removeChangesets(token, session);
        closeReader(token);
        closeWriter(token);
    }

    private void closeReader(DataAccessToken token) throws SQLException {
        PageReader reader = (PageReader) readersMap.remove(token.key());
        reader.close();
    }

    private void closeWriter(DataAccessToken token) throws SQLException {
        VersionedRecordsWriter writer = (VersionedRecordsWriter) writersMap.remove(token.key());
        writer.close();
    }

    private void removeChangesets(DataAccessToken token, Session session) throws SQLException {
        discardChangeSets(token, session);
        changesetsMap.remove(token.key());
    }

    private void closeWriters() throws SQLException {
        Collection all = writersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            VersionedRecordsWriter writer = (VersionedRecordsWriter) iter.next();
            writer.close();
        }

        writersMap.clear();
    }

    void closeReaders() throws SQLException {
        Collection all = readersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            PageReader pageReader = (PageReader) iter.next();
            pageReader.close();
        }

        readersMap.clear();
    }

    private Integer pageChangesetsKey(int pageNumber) {
        return new Integer(pageNumber);
    }

    private Map pageChangesetsMap(DataAccessToken token, Session session) throws SQLException {
        init(token, session);
        return (Map) changesetsMap.get(token.key());
    }

    private void initWriter(DataAccessToken token) throws SQLException {
        if (!writersMap.containsKey(token.key())) {
            VersionedRecordsWriter writer = writerFactory.create(datasource, token.getTable(), sqlTypes);
            writersMap.put(token.key(), writer);
        }
    }

    private void initReader(DataAccessToken token, int pageSize, Session session) throws SQLException {
        if (!readersMap.containsKey(token.key())) {
            ScrollableVersionedRecords records = recordsReader.fetch(token.getVersion(), token.getTable(), session);
            PageReader reader = new PageReader(pageSize, records);

            readersMap.put(token.key(), reader);
        }
    }

    private void initChangesetsMap(DataAccessToken token) {
        if (!changesetsMap.containsKey(token.key())) {
            changesetsMap.put(token.key(), new HashMap());
        }
    }

    public void save(DataAccessToken token, Session session) throws Exception {
        VersionedRecordsWriter writer = writer(token);
        ChangeSets sets = changesets(token, session);
        for (Iterator iter = sets.iterator(); iter.hasNext();) {
            ChangeSet element = (ChangeSet) iter.next();
            writer.update(element);
        }

        reload(token, session);
    }

}

package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.EditToken;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;

public class DataEditorServiceCache {

    private Map readersMap;

    private VersionedRecordsReader recordsReader;

    private Map writersMap;

    private Datasource datasource;

    private SqlDataTypes sqlTypes;

    private Map changesetsMap;

    private VersionedRecordsWriterFactory writerFactory;

    public DataEditorServiceCache(VersionedRecordsReader reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes) {
        recordsReader = reader;
        this.writerFactory = writerFactory;
        this.datasource = datasource;
        this.sqlTypes = sqlTypes;

        readersMap = new HashMap();
        writersMap = new HashMap();
        changesetsMap = new HashMap();
    }

    public PageReader reader(EditToken token) {
        return (PageReader) readersMap.get(token.key());

    }

    public VersionedRecordsWriter writer(EditToken token) {
        return (VersionedRecordsWriter) writersMap.get(token.key());
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public List changesets(EditToken token, int pageNumber, Session session) throws SQLException {
        Map map = pageChangesetsMap(token, session);
        Integer pageKey = pageChangesetsKey(pageNumber);
        if (!map.containsKey(pageKey)) {
            map.put(pageKey, new ArrayList());
        }

        return (List) map.get(pageKey);
    }

    public void submitChangeSet(EditToken token, ChangeSet changeset, int pageNumber, Session session)
            throws SQLException {
        List list = changesets(token, pageNumber, session);
        list.add(changeset);
    }

    public void discardChangeSets(EditToken token, Session session) throws SQLException {
        Map pageChangsetsMap = pageChangesetsMap(token, session);
        pageChangsetsMap.clear();
    }

    public List changesets(EditToken token, Session session) throws SQLException {
        List all = new ArrayList();
        Map pageChangesetsMap = pageChangesetsMap(token, session);
        Set keys = new TreeSet(pageChangesetsMap.keySet());
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            List list = (List) pageChangesetsMap.get(iter.next());
            all.addAll(list);
        }

        return all;
    }

    public void init(EditToken token, Session session) throws SQLException {
        init(token, 100, session);
    }

    public void init(EditToken token, int pageSize, Session session) throws SQLException {
        initChangesetsMap(token);
        initReader(token, pageSize, session);
        initWriter(token);
    }

    public void invalidate() throws SQLException {
        closeReaders();
        closeWriters();
        changesetsMap.clear();
    }

    public void reload(EditToken token, Session session) throws SQLException {
        close(token, session);
        init(token, session);
    }

    public void close(EditToken token, Session session) throws SQLException {
        removeChangesets(token, session);
        closeReader(token);
        closeWriter(token);
    }

    private void closeReader(EditToken token) throws SQLException {
        PageReader reader = (PageReader) readersMap.remove(token.key());
        reader.close();
    }

    private void closeWriter(EditToken token) throws SQLException {
        VersionedRecordsWriter writer = (VersionedRecordsWriter) writersMap.remove(token.key());
        writer.close();
    }

    private void removeChangesets(EditToken token, Session session) throws SQLException {
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

    private Map pageChangesetsMap(EditToken token, Session session) throws SQLException {
        init(token, session);
        return (Map) changesetsMap.get(token.key());
    }

    private void initWriter(EditToken token) throws SQLException {
        if (!writersMap.containsKey(token.key())) {
            VersionedRecordsWriter writer = writerFactory.create(datasource, token.getTable(), sqlTypes);
            writersMap.put(token.key(), writer);
        }
    }

    private void initReader(EditToken token, int pageSize, Session session) throws SQLException {
        if (!readersMap.containsKey(token.key())) {
            ScrollableVersionedRecords records = recordsReader.fetch(token.getVersion(), token.getTable(), session);
            PageReader reader = new PageReader(pageSize, records);

            readersMap.put(token.key(), reader);
        }
    }

    private void initChangesetsMap(EditToken token) {
        if (!changesetsMap.containsKey(token.key())) {
            changesetsMap.put(token.key(), new HashMap());
        }
    }

}

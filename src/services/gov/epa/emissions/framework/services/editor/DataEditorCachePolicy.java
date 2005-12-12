package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.EditToken;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataEditorCachePolicy {

    private Map readersMap;

    private VersionedRecordsReader recordsReader;

    private Map writersMap;

    private Datasource datasource;

    private SqlDataTypes sqlTypes;

    private Map changesetsMap;

    public DataEditorCachePolicy(VersionedRecordsReader reader, Datasource datasource, SqlDataTypes sqlTypes) {
        recordsReader = reader;
        this.datasource = datasource;
        this.sqlTypes = sqlTypes;

        readersMap = new HashMap();
        writersMap = new HashMap();
        changesetsMap = new HashMap();
    }

    public PageReader reader(EditToken token) throws SQLException {
        Object key = token.key();
        if (!readersMap.containsKey(key)) {
            ScrollableVersionedRecords records = recordsReader.fetch(token.getVersion(), token.getTable());
            PageReader reader = new PageReader(20, records);

            readersMap.put(key, reader);
        }

        return (PageReader) readersMap.get(key);

    }

    public VersionedRecordsWriter writer(EditToken token) throws SQLException {
        Object key = token.key();
        if (!writersMap.containsKey(key)) {
            VersionedRecordsWriter writer = new VersionedRecordsWriter(datasource, token.getTable(), sqlTypes);
            writersMap.put(key, writer);
        }

        return (VersionedRecordsWriter) writersMap.get(key);
    }

    public void invalidate() throws SQLException {
        closeReaders();
        closeWriters();
    }

    private void closeWriters() throws SQLException {
        Collection all = writersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            VersionedRecordsWriter writer = (VersionedRecordsWriter) iter.next();
            writer.close();
        }

        writersMap.clear();
    }

    private void closeReaders() throws SQLException {
        Collection all = readersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            PageReader pageReader = (PageReader) iter.next();
            pageReader.close();
        }

        readersMap.clear();
    }

    public List changesets(EditToken token) {
        if (!changesetsMap.containsKey(token.key())) {
            changesetsMap.put(token.key(), new ArrayList());
        }

        return (List) changesetsMap.get(token.key());
    }

    public void discardChangeSets(EditToken token) {
        changesetsMap.remove(token.key());// clear
    }

}

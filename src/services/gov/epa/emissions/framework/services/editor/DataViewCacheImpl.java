package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.Session;

public class DataViewCacheImpl implements DataViewCache {

    private Map readersMap;

    private VersionedRecordsReader recordsReader;

    private EmfProperties properties;

    public DataViewCacheImpl(VersionedRecordsReader reader) {
        this(reader, new EmfPropertiesDAO());
    }

    public DataViewCacheImpl(VersionedRecordsReader reader, EmfProperties properties) {
        this.properties = properties;
        recordsReader = reader;
        readersMap = new HashMap();
    }

    public PageReader reader(DataAccessToken token) {
        return (PageReader) readersMap.get(token.key());

    }

    public void init(DataAccessToken token, Session session) throws SQLException {
        init(token, defaultPageSize(session), session);
    }

    public void init(DataAccessToken token, int pageSize, Session session) throws SQLException {
        initReader(token, pageSize, session);
    }

    public void init(DataAccessToken token, int pageSize, String columnFilter, String rowFilter, String sortOrder,
            Session session) throws Exception {
        reinitialize(token, pageSize, columnFilter, rowFilter, sortOrder, session);
    }

    public void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder,
            Session session) throws Exception {
        reinitialize(token, defaultPageSize(session), columnFilter, rowFilter, sortOrder, session);
    }

    public int defaultPageSize(Session session) {
        EmfProperty pageSize = properties.getProperty("page-size", session);
        return Integer.parseInt(pageSize.getValue());
    }

    public int pageSize(DataAccessToken token) {
        return reader(token).pageSize();
    }

    public void invalidate() throws SQLException {
        closeReaders();
    }

    public void reload(DataAccessToken token, Session session) throws SQLException {
        close(token, session);
        init(token, session);
    }

    public void close(DataAccessToken token, Session session) throws SQLException {
        PageReader reader = (PageReader) readersMap.remove(token.key());
        reader.close();
    }

    void closeReaders() throws SQLException {
        Collection all = readersMap.values();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            PageReader pageReader = (PageReader) iter.next();
            pageReader.close();
        }

        readersMap.clear();
    }

    private void initReader(DataAccessToken token, int pageSize, Session session) throws SQLException {
        if (readersMap.containsKey(token.key()))
            return;

        ScrollableVersionedRecords records = recordsReader
                .optimizedFetch(token.getVersion(), token.getTable(), session);
        PageReader reader = new PageReader(pageSize, records);

        cacheReader(token, reader);
    }

    private void cacheReader(DataAccessToken token, PageReader reader) throws SQLException {
        if (readersMap.containsKey(token.key())) {// close old/stale reader - performance enhancement
            PageReader oldReader = reader(token);
            oldReader.close();
        }

        readersMap.put(token.key(), reader);
    }

    private void reinitialize(DataAccessToken token, int pageSize, String columnFilter, String rowFilter,
            String sortOrder, Session session) throws Exception {
        ScrollableVersionedRecords records = recordsReader.optimizedFetch(token.getVersion(), token.getTable(),
                columnFilter, rowFilter, sortOrder, session);
        PageReader reader = new PageReader(pageSize, records);

        cacheReader(token, reader);
    }
}

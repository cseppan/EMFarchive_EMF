package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.PageReader;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsReader;
import gov.epa.emissions.framework.dao.EmfProperties;
import gov.epa.emissions.framework.dao.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.DataAccessToken;

import org.hibernate.Session;

public class DataAccessCacheImpl implements DataAccessCache {

    private DataViewCache view;

    private DataUpdatesCache update;

    public DataAccessCacheImpl(VersionedRecordsReader reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes) {
        this(reader, writerFactory, datasource, sqlTypes, new EmfPropertiesDAO());
    }

    public DataAccessCacheImpl(VersionedRecordsReader reader, VersionedRecordsWriterFactory writerFactory,
            Datasource datasource, SqlDataTypes sqlTypes, EmfProperties properties) {
        view = new DataViewCacheImpl(reader, properties);
        update = new DataUpdatesCacheImpl(writerFactory, datasource, sqlTypes, properties);
    }

    public void init(DataAccessToken token, Session session) throws Exception {
        init(token, defaultPageSize(session), session);
    }

    public void init(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder, Session session)
            throws Exception {//TODO
    }

    public void init(DataAccessToken token, int pageSize, Session session) throws Exception {
        view.init(token, pageSize, session);
        update.init(token, session);
    }

    public PageReader reader(DataAccessToken token) {
        return view.reader(token);
    }

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    public ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws Exception {
        return update.changesets(token, pageNumber, session);
    }

    public void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session)
            throws Exception {
        update.submitChangeSet(token, changeset, pageNumber, session);
    }

    public void discardChangeSets(DataAccessToken token, Session session) throws Exception {
        update.discardChangeSets(token, session);
    }

    public ChangeSets changesets(DataAccessToken token, Session session) throws Exception {
        return update.changesets(token, session);
    }

    public int defaultPageSize(Session session) {
        return view.defaultPageSize(session);
    }

    public int pageSize(DataAccessToken token) {
        return view.pageSize(token);
    }

    public void invalidate() throws Exception {
        view.invalidate();
        update.invalidate();
    }

    public void reload(DataAccessToken token, Session session) throws Exception {
        close(token, session);
        init(token, session);
    }

    public void close(DataAccessToken token, Session session) throws Exception {
        view.close(token, session);
        update.close(token, session);
    }

    public void save(DataAccessToken token, Session session) throws Exception {
        update.save(token, session);
        reload(token, session);
    }

    public boolean hasChanges(DataAccessToken token, Session session) throws Exception {
        return update.hasChanges(token, session);
    }

}

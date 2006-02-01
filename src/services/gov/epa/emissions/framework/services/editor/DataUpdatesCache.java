package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.DataAccessToken;

import java.sql.SQLException;

import org.hibernate.Session;

public interface DataUpdatesCache extends DataViewCache {

    VersionedRecordsWriter writer(DataAccessToken token);

    /*
     * Keeps a two-level mapping. First map, ChangeSetMap is a map of tokens and PageChangeSetMap. PageChangeSetMap maps
     * Page Number to Change Sets (of that Page)
     */
    ChangeSets changesets(DataAccessToken token, int pageNumber, Session session) throws SQLException;

    void submitChangeSet(DataAccessToken token, ChangeSet changeset, int pageNumber, Session session)
            throws SQLException;

    void discardChangeSets(DataAccessToken token, Session session) throws SQLException;

    ChangeSets changesets(DataAccessToken token, Session session) throws SQLException;

    boolean hasChanges(DataAccessToken token, Session session) throws Exception;

    void save(DataAccessToken token, Session session) throws Exception;

}
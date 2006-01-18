package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;

public interface DataEditorService {
    // read
    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    // edit
    void submit(DataAccessToken token, ChangeSet changeset, int page) throws EmfException;

    void discard(DataAccessToken token) throws EmfException;

    void save(DataAccessToken token) throws EmfException;

    // version-related
    Version[] getVersions(long datasetId) throws EmfException;

    Version derive(Version baseVersion, String name) throws EmfException;

    Version markFinal(Version derived) throws EmfException;

    // session
    DataAccessToken openSession(DataAccessToken token) throws EmfException;

    DataAccessToken openSession(DataAccessToken token, int pageSize) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;

    /**
     * close all sessions, and the service itself
     */
    void close() throws EmfException;

}
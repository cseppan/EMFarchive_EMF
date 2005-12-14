package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;

public interface DataEditorService {
    // read
    Page getPage(EditToken token, int pageNumber) throws EmfException;

    int getPageCount(EditToken token) throws EmfException;

    Page getPageWithRecord(EditToken token, int recordId) throws EmfException;

    int getTotalRecords(EditToken token) throws EmfException;

    // edit
    void submit(EditToken token, ChangeSet changeset, int page) throws EmfException;

    void discard(EditToken token) throws EmfException;

    void save(EditToken token) throws EmfException;

    // version-related
    Version[] getVersions(long datasetId) throws EmfException;

    Version derive(Version baseVersion, String name) throws EmfException;

    Version markFinal(Version derived) throws EmfException;

    // session
    void openSession(EditToken token) throws EmfException;

    void closeSession(EditToken token) throws EmfException;

    /**
     * close all sessions, and the service itself
     */
    void close() throws EmfException;

}
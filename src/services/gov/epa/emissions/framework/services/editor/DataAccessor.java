package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;

public interface DataAccessor {

    int defaultPageSize();

    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int record) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    Version currentVersion(Version reference) throws EmfException;

    Version[] getVersions(long datasetId) throws EmfException;

    void shutdown() throws EmfException;

    DataAccessToken openSession(DataAccessToken token, int pageSize) throws Exception;

    DataAccessToken openSession(DataAccessToken token) throws Exception;

    void closeSession(DataAccessToken token) throws EmfException;

    DataAccessToken openEditSession(User user, DataAccessToken token) throws Exception;

    DataAccessToken openEditSession(User user, DataAccessToken token, int pageSize) throws Exception;

    DataAccessToken closeEditSession(DataAccessToken token) throws EmfException;

    boolean isLockOwned(DataAccessToken token) throws EmfException;

    DataAccessToken renewLock(DataAccessToken token) throws EmfException;

    void lock(User user, DataAccessToken token) throws EmfException;

    boolean isLocked(Version version) throws EmfException;

}
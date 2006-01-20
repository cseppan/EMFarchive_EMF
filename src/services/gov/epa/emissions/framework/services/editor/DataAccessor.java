package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;

public interface DataAccessor {

    public abstract int defaultPageSize();

    public abstract Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    public abstract int getPageCount(DataAccessToken token) throws EmfException;

    public abstract Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException;

    public abstract int getTotalRecords(DataAccessToken token) throws EmfException;

    public abstract Version currentVersion(Version reference) throws EmfException;

    public abstract Version[] getVersions(long datasetId) throws EmfException;

    public abstract void shutdown() throws EmfException;

    public abstract DataAccessToken openSession(DataAccessToken token, int pageSize) throws Exception;

    public abstract DataAccessToken openSession(DataAccessToken token) throws Exception;

    public abstract void closeSession(DataAccessToken token) throws EmfException;

    public abstract DataAccessToken openEditSession(User user, DataAccessToken token) throws Exception;

    public abstract DataAccessToken openEditSession(User user, DataAccessToken token, int pageSize) throws Exception;

    public abstract DataAccessToken closeEditSession(DataAccessToken token) throws EmfException;

    public abstract boolean isLockOwned(DataAccessToken token) throws EmfException;

    public abstract DataAccessToken renewLock(DataAccessToken token) throws EmfException;

}
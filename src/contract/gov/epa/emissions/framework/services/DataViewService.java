package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;

public interface DataViewService {
    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    Version[] getVersions(long datasetId) throws EmfException;

    // session
    DataAccessToken openSession(DataAccessToken token) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;

}
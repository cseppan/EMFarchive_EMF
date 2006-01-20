package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;

public interface DataAccessService {

    // read
    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int recordId) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    // version-related
    Version[] getVersions(long datasetId) throws EmfException;

}
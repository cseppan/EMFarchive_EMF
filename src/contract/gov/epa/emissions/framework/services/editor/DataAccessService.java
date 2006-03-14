package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.services.EmfException;

public interface DataAccessService {
    // read
    /**
     * Applie's constraints and returns Page 1
     */
    Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException;

    Page getPage(DataAccessToken token, int pageNumber) throws EmfException;

    int getPageCount(DataAccessToken token) throws EmfException;

    Page getPageWithRecord(DataAccessToken token, int record) throws EmfException;

    int getTotalRecords(DataAccessToken token) throws EmfException;

    // version-related
    Version[] getVersions(long datasetId) throws EmfException;
    
    TableMetadata getTableMetadata(String table) throws EmfException;

}
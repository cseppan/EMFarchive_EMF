package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;

public interface DataEditorService {
    Page getPage(String tableName, int pageNumber) throws EmfException;

    int getPageCount(String tableName) throws EmfException;

    Page getPageWithRecord(String tableName, int recordId) throws EmfException;

    int getTotalRecords(String tableName) throws EmfException;

    Version[] getVersions(long datasetId) throws EmfException;

    Version derive(Version baseVersion) throws EmfException;

    void submit(ChangeSet changeset) throws EmfException;

    void close() throws EmfException;

    Version markFinal(Version derived) throws EmfException;

}
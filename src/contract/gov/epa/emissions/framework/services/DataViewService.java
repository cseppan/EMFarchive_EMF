package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.framework.EmfException;

public interface DataViewService extends DataAccessService {
    DataAccessToken openSession(DataAccessToken token) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;

    Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException;
}
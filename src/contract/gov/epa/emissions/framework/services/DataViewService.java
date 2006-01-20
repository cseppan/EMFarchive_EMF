package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface DataViewService extends DataAccessService {// tagging service
    // session
    DataAccessToken openSession(DataAccessToken token) throws EmfException;

    void closeSession(DataAccessToken token) throws EmfException;
}
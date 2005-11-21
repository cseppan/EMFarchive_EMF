package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

public interface LoggingService {

    AccessLog[] getAccessLogs(long datasetid) throws EmfException;

}

package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;

public interface LoggingService {

    AccessLog[] getAccessLogs(long datasetid) throws EmfException;

}

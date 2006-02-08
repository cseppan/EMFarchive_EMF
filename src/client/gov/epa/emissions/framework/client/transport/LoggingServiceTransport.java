package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingService;

public class LoggingServiceTransport implements LoggingService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public LoggingServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createSessionEnabledCall("User Service");
    }

    public AccessLog[] getAccessLogs(long datasetid) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAccessLogs");
        call.addLongParam("datasetId");
        call.setReturnType(mappings.logs());

        return (AccessLog[]) call.requestResponse(new Object[] { new Long(datasetid) });
    }

}

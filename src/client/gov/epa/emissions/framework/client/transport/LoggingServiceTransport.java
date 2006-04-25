package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.LoggingService;

public class LoggingServiceTransport implements LoggingService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public LoggingServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("User Service");
    }

    public AccessLog[] getAccessLogs(int datasetid) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAccessLogs");
        call.addIntegerParam("datasetId");
        call.setReturnType(mappings.logs());

        return (AccessLog[]) call.requestResponse(new Object[] { new Integer(datasetid) });
    }

}

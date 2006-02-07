package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class LoggingServiceTransport implements LoggingService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public LoggingServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    public AccessLog[] getAccessLogs(long datasetid) throws EmfException {

        try {
            Call call = callFactory.createCall();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "getAccessLogs");
            mappings.addLongParam(call, "datasetId");
            mappings.setReturnType(call, mappings.logs());

            return (AccessLog[]) call.invoke(new Object[] { new Long(datasetid) });
        } catch (AxisFault fault) {
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Logging Service");
        }
    }

}

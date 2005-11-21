package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.AccessLog;
import gov.epa.emissions.framework.services.LoggingService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingServiceTransport implements LoggingService {
    private static Log LOG = LogFactory.getLog(LoggingServiceTransport.class);

    private CallFactory callFactory;

    private LoggingMappings mappings;

    public LoggingServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new LoggingMappings();
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
            throwExceptionOnAxisFault("Could not get Access Logs for Dataset: " + datasetid, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get Access Logs for Dataset: " + datasetid, e);
        }

        return null;
    }

    private String extractMessage(String faultReason) {
        return faultReason.substring(faultReason.indexOf("Exception: ") + 11);
    }

    private void throwExceptionDueToServiceErrors(String message, Exception e) throws EmfException {
        LOG.error(message, e);
        throw new EmfException(message, e.getMessage(), e);
    }

    private void throwExceptionOnAxisFault(String message, AxisFault fault) throws EmfException {
        LOG.error(message, fault);
        throw new EmfException(extractMessage(fault.getMessage()));
    }
}

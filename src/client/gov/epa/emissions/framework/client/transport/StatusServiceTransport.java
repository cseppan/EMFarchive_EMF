package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StatusServiceTransport implements StatusService {
    private static Log LOG = LogFactory.getLog(StatusServiceTransport.class);

    private StatusMappings mappings;

    private CallFactory callFactory;

    public StatusServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new StatusMappings();
    }

    public Status[] getAll(String username) throws EmfException {
        try {
            Call call = callFactory.createCall();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "getAll");
            mappings.addStringParam(call, "username");
            mappings.setReturnType(call, mappings.logsw());

            return (Status[]) call.invoke(new Object[] { username });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get all Status messages for user: " + username, fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get all Status messages for user: " + username, e);
        }

        return null;
    }

    public void create(Status status) throws EmfException {
        try {
            Call call = callFactory.createCall();
            call.setMaintainSession(true);

            mappings.register(call);
            mappings.setOperation(call, "create");
            mappings.addParam(call, "status", mappings.status());
            mappings.setVoidReturnType(call);

            call.invoke(new Object[] { status });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not create Status: " + status.getMessage(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not create Status: " + status.getMessage(), e);
        }
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

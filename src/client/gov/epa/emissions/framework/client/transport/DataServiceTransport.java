package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataServiceTransport implements DataService {
    private static Log LOG = LogFactory.getLog(DataServiceTransport.class);

    private CallFactory callFactory;

    private EmfMappings mappings;

    public DataServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    public EmfDataset[] getDatasets() throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "getDatasets");
            mappings.setReturnType(call, mappings.datasets());

            return (EmfDataset[]) call.invoke(new Object[] {});
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to fetch Datasets", fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to fetch Datasets", e);
        }

        return null;
    }

    public void updateDataset(EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateDataset");
            mappings.addParam(call, "dataset", mappings.dataset());
            mappings.setAnyReturnType(call);

            call.invoke(new Object[] { dataset });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Failed to update Dataset: " + dataset.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Failed to update Dataset: " + dataset.getName(), e);
        }
    }

    private String extractMessage(String faultReason) {// FIXME: what's this?
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

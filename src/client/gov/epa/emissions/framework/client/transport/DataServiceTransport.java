package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EMFConstants;
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

    public void updateDatasetWithoutLock(EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateDatasetWithoutLock");
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
        String msg=extractMessage(fault.getMessage());
        
        if (fault.getCause()!=null){
            if (fault.getCause().getMessage().equals(EMFConstants.CONNECTION_REFUSED)){
                msg="EMF server not responding";
            }            
        }
        throw new EmfException(msg);
    }

    
    public EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "obtainLockedDataset");
            mappings.addParam(call, "owner", mappings.user());
            mappings.addParam(call, "dataset", mappings.dataset());
            mappings.setReturnType(call, mappings.dataset());

            return (EmfDataset) call.invoke(new Object[] { owner, dataset });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not get Dataset lock: " + dataset.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not get sector lock: " + dataset.getName(), e);
        }

        return null;
    }

    public EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "updateDataset");
            mappings.addParam(call, "dataset", mappings.dataset());
            mappings.setReturnType(call, mappings.dataset());

            return (EmfDataset) call.invoke(new Object[] { dataset });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not update Dataset: " + dataset.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not update Dataset: " + dataset.getName(), e);
        }

        return null;
    }

    public EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        try {
            Call call = callFactory.createCall();

            mappings.register(call);
            mappings.setOperation(call, "releaseLockedDataset");
            mappings.addParam(call, "locked", mappings.dataset());
            mappings.setReturnType(call, mappings.dataset());

            return (EmfDataset) call.invoke(new Object[] { locked });
        } catch (AxisFault fault) {
            throwExceptionOnAxisFault("Could not release Dataset lock: " + locked.getName(), fault);
        } catch (Exception e) {
            throwExceptionDueToServiceErrors("Could not release Dataset lock: " + locked.getName(), e);
        }

        return null;
    }

}

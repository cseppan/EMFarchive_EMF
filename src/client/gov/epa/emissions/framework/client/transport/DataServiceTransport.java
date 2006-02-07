package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;

public class DataServiceTransport implements DataService {
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Data Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Data Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Data Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Data Service");
        }
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
            throw new EmfServiceException(fault);
        } catch (Exception e) {
            throw new EmfException("Unable to connect to Data Service");
        }
    }

}

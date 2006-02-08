package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

public class DataServiceTransport implements DataService {
    private CallFactory callFactory;

    private EmfMappings mappings;

    public DataServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Data Service");
    }

    public EmfDataset[] getDatasets() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasets");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {});
    }

    public void updateDatasetWithoutLock(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDatasetWithoutLock");
        call.addParam("dataset", mappings.dataset());
        call.setVoidReturnType();

        call.request(new Object[] { dataset });
    }

    public EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedDataset");
        call.addParam("owner", mappings.user());
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { owner, dataset });
    }

    public EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDataset");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { dataset });
    }

    public EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedDataset");
        call.addParam("locked", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { locked });
    }

}

package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class DataServiceTransport implements DataService {
    private CallFactory callFactory;

    private DataMappings mappings;

    public DataServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
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

    public EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addParam("datasetType", mappings.datasetType());
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {datasetType});
    
    }

    public void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("deleteDatasets");
        call.addParam("owner", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.setVoidReturnType();
        
        call.request(new Object[] { owner, datasets });
    }

    public EmfDataset getDataset(Integer datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDataset");
        call.addParam("datasetId", mappings.integer());
        call.setReturnType(mappings.dataset());
        
        return (EmfDataset)call.requestResponse(new Object[]{ datasetId });
    }

}

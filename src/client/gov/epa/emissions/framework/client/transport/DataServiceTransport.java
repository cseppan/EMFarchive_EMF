package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class DataServiceTransport implements DataService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public DataServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Data Service");
        
        return call;
    }

    public synchronized EmfDataset[] getDatasets() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDatasets");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {});
    }

    public synchronized EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedDataset");
        call.addParam("owner", mappings.user());
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { owner, dataset });
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateDataset");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { dataset });
    }

    public synchronized EmfDataset releaseLockedDataset(EmfDataset locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedDataset");
        call.addParam("locked", mappings.dataset());
        call.setReturnType(mappings.dataset());

        return (EmfDataset) call.requestResponse(new Object[] { locked });
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addParam("datasetType", mappings.datasetType());
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {datasetType});
    
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException {

        EmfCall call = call();

        call.setOperation("getDatasets");
        call.addIntegerParam("datasetTypeId");
        call.addStringParam("nameContaining");
        call.setReturnType(mappings.datasets());

        return (EmfDataset[]) call.requestResponse(new Object[] {new Integer(datasetTypeId), nameContaining});
    
    }

    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("deleteDatasets");
        call.addParam("owner", mappings.user());
        call.addParam("datasets", mappings.datasets());
        call.setVoidReturnType();
        
        call.request(new Object[] { owner, datasets });
    }

    public synchronized EmfDataset getDataset(Integer datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDataset");
        call.addParam("datasetId", mappings.integer());
        call.setReturnType(mappings.dataset());
        
        return (EmfDataset)call.requestResponse(new Object[]{ datasetId });
    }

    public synchronized EmfDataset getDataset(String datasetName) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDataset");
        call.addStringParam("datasetName");
        call.setReturnType(mappings.dataset());
        
        return (EmfDataset)call.requestResponse(new Object[]{ datasetName });
    }

    public synchronized String[] getDatasetValues(Integer datasetId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getDatasetValues");
        call.addParam("datasetId", mappings.integer());
        call.setStringArrayReturnType();
        
        return (String[])call.requestResponse(new Object[]{ datasetId });
    }

    public Version obtainedLockOnVersion(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainedLockOnVersion");
        call.addParam("user", mappings.user());
        call.addParam("id", mappings.integer());
        call.setReturnType(mappings.version());

        return (Version) call.requestResponse(new Object[] { user, id});
    }

    public void updateVersionNReleaseLock(Version locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateVersionNReleaseLock");
        call.addParam("locked", mappings.version());
        call.setVoidReturnType();

        call.request(new Object[] { locked });
    }

}

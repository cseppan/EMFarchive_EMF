package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;

public class CostServiceTransport implements CostService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public CostServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("CoST Service");
    }
    
    public ControlMeasure[] getMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.setReturnType(mappings.controlMeasures());
        Object[] params = new Object[] { };

        return (ControlMeasure[]) call.requestResponse(params);
    }

}

package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;

public class CostServiceTransport implements CostService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public CostServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("CostService");
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] {});
    }

    public void addMeasure(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("addMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.setVoidReturnType();

        call.request(new Object[] { measure });
    }

    public void removeMeasure(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.setVoidReturnType();

        call.request(new Object[] { measure });
    }

    public ControlMeasure obtainLockedMeasure(User owner, ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedMeasure");
        call.addParam("owner", mappings.user());
        call.addParam("measure", mappings.controlMeasure());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { owner, measure });
    }

    public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedControlMeasure");
        call.addParam("locked", mappings.controlMeasure());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { locked });
    }
    
    public ControlMeasure updateMeasure(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.setReturnType(mappings.controlMeasure());
        
        return (ControlMeasure) call.requestResponse(new Object[] { measure });
    }

}

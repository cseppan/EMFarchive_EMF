package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

public class CostServiceTransport implements CostService {

    private DataMappings mappings;

    private EmfCall emfCall;

    public CostServiceTransport(EmfCall controlMeasureCall) {
        this.emfCall = controlMeasureCall;
        mappings = new DataMappings();
    }

    private EmfCall call() {
        return emfCall;
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

    public Scc[] getSccs(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccs");
        call.addParam("measure", mappings.controlMeasure());
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { measure });
        
        return sccs;
    }

}

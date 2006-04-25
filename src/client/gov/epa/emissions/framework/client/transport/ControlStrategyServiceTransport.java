package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

public class ControlStrategyServiceTransport implements ControlStrategyService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public ControlStrategyServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("ControlStrategy Service");
    }

    public ControlStrategy[] getControlStrategies() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlStrategies");
        call.setReturnType(mappings.controlStrategies());

        return (ControlStrategy[]) call.requestResponse(new Object[] {});
    }

    public void addControlStrategy(ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addControlStrategy");
        call.addParam("element", mappings.controlStrategy());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { owner, element });

    }

    public ControlStrategy releaseLocked(ControlStrategy locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { locked });
    }

    public ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateControlStrategy");
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { element });
    }
    
    

}

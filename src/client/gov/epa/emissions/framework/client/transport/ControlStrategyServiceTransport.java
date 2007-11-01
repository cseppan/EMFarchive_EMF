package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ControlStrategyServiceTransport implements ControlStrategyService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public ControlStrategyServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("ControlStrategy Service");
        
        return call;
    }

    public synchronized ControlStrategy[] getControlStrategies() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlStrategies");
        call.setReturnType(mappings.controlStrategies());

        return (ControlStrategy[]) call.requestResponse(new Object[] {});
    }

    public synchronized int addControlStrategy(ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addControlStrategy");
        call.addParam("element", mappings.controlStrategy());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { element });
    }

    public synchronized ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { owner, element });

    }

//    public void releaseLocked(ControlStrategy locked) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("releaseLocked");
//        call.addParam("element", mappings.controlStrategy());
//        call.setReturnType(mappings.controlStrategy());
//
//        call.request(new Object[] { locked });
//    }

    public synchronized void releaseLocked(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addIntegerParam("id");
        call.setReturnType(mappings.controlStrategy());

        call.request(new Object[] { new Integer(id) });
    }

    public synchronized ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateControlStrategy");
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { element });
    }

    public synchronized ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateControlStrategyWithLock");
        call.addParam("element", mappings.controlStrategy());
        call.setReturnType(mappings.controlStrategy());

        return (ControlStrategy) call.requestResponse(new Object[] { element });
    }

//    public void removeControlStrategies(ControlStrategy[] elements, User user) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("removeControlStrategies");
//        call.addParam("elements", mappings.controlStrategies());
//        call.addParam("user", mappings.user());
//        call.setVoidReturnType();
//
//        call.request(new Object[] { elements, user });
//    }

    public synchronized void removeControlStrategies(int[] ids, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeControlStrategies");
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { ids, user });
    }

    public synchronized void runStrategy(User user, ControlStrategy strategy) throws EmfException {
        EmfCall call = call();

        call.setOperation("runStrategy");
        call.addParam("user", mappings.user());
        call.addParam("strategy", mappings.controlStrategy());
        call.setVoidReturnType();

        call.request(new Object[] { user, strategy });
    }

    public synchronized StrategyType[] getStrategyTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getStrategyTypes");
        call.setReturnType(mappings.strategyTypes());

        return (StrategyType[]) call.requestResponse(new Object[] {});
    }

    public synchronized void stopRunStrategy() throws EmfException {
        EmfCall call = call();

        call.setOperation("stopRunStrategy");
        call.setVoidReturnType();

        call.request(new Object[] {});
    }

    public synchronized void createInventory(User user, ControlStrategy controlStrategy, ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("createInventory");
        call.addParam("user", mappings.user());
        call.addParam("controlStrategy", mappings.controlStrategy());
        call.addParam("controlStrategyInputDataset", mappings.controlStrategyInputDataset());
        call.setVoidReturnType();

        call.request(new Object[] { user, controlStrategy, controlStrategyInputDataset });

    }

    public synchronized ControlStrategyResult getControlStrategyResult(int controlStrategyId, int inputDatasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("controlStrategyResults");
        call.addIntegerParam("controlStrategyId");
        call.addIntegerParam("inputDatasetId");

        call.setReturnType(mappings.controlStrategyResult());

        return (ControlStrategyResult) call.requestResponse(new Object[] { new Integer(controlStrategyId), 
                new Integer(inputDatasetId) });
    }

    public synchronized String controlStrategyRunStatus(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("controlStrategyRunStatus");
        call.addIntParam();

        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized ControlMeasureClass[] getControlMeasureClasses(int controlStrategyId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlMeasureClasses");
        call.addIntParam();
        call.setReturnType(mappings.controlMeasureClasses());

        return (ControlMeasureClass[]) call.requestResponse(new Object[] { new Integer(controlStrategyId) });
    }

    public synchronized int isDuplicateName(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("isDuplicateName");
        call.addStringParam("name");
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new String(name) });
    }

    public synchronized int copyControlStrategy(int id, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyControlStrategy");
        call.addIntegerParam("id");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new Integer(id), creator });
    }

    public synchronized ControlStrategy getById(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getById");
        call.addIntegerParam("id");
        call.setReturnType(mappings.controlStrategy());
        return (ControlStrategy) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlStrategyResults");
        call.addIntegerParam("controlStrategyId");
        call.setReturnType(mappings.controlStrategyResults());

        return (ControlStrategyResult[]) call.requestResponse(new Object[] { new Integer(controlStrategyId) });
    }

}

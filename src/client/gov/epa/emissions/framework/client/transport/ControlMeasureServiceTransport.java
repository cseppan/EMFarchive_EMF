package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

public class ControlMeasureServiceTransport implements ControlMeasureService {

    private CallFactory callFactory;

    private DataMappings mappings;

    public ControlMeasureServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("ControlMeasureService");
    }

    public ControlMeasure[] getMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] {});
    }
    
    public ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasures");
        call.addParam("pollutant", mappings.pollutant());
        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] { pollutant });
    }

    public void addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("addMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setVoidReturnType();

        call.request(new Object[] { measure, sccs });
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

//    public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("releaseLockedControlMeasure");
//        call.addParam("locked", mappings.controlMeasure());
//        call.setReturnType(mappings.controlMeasure());
//
//        return (ControlMeasure) call.requestResponse(new Object[] { locked });
//    }

    public void releaseLockedControlMeasure(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedControlMeasure");
        call.addIntegerParam("id");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { new Integer(id) });
    }

    public ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { measure, sccs });
    }

    public Scc[] getSccsWithDescriptions(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccsWithDescriptions");
        call.addParam("measure", mappings.controlMeasure());
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { measure });

        return sccs;
    }

    public Scc[] getSccs(ControlMeasure measure) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccs");
        call.addParam("measure", mappings.controlMeasure());
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { measure });

        return sccs;
    }

    public ControlTechnology[] getControlTechnologies() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlTechnologies");
        call.setReturnType(mappings.controlTechnologies());

        ControlTechnology[] technologies = (ControlTechnology[]) call.requestResponse(new Object[] {});

        return technologies;
    }

    public CostYearTable getCostYearTable(int targetYear) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCostYearTable");
        call.addIntegerParam("targetYear");

        call.setReturnType(mappings.costYearTable());

        return (CostYearTable) call.requestResponse(new Object[] { new Integer(targetYear) });
    }
    
    public ControlMeasureClass[] getMeasureClasses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasureClasses");
        call.setReturnType(mappings.controlMeasureClasses());

        return (ControlMeasureClass[]) call.requestResponse(new Object[] { });
    }

    public ControlMeasureClass getMeasureClass(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeasureClass");
        call.addIntegerParam("name");
        call.setReturnType(mappings.controlMeasureClass());

        return (ControlMeasureClass) call.requestResponse(new Object[] { name });
    }

    public LightControlMeasure[] getLightControlMeasures() throws EmfException {
        EmfCall call = call();

        call.setOperation("getLightControlMeasures");
        call.setReturnType(mappings.lightControlMeasures());

        return (LightControlMeasure[]) call.requestResponse(new Object[] { });
    }

}

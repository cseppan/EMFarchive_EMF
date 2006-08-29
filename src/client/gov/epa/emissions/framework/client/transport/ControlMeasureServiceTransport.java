package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
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

    public ControlTechnology[] getControlTechnologies() throws EmfException {
        EmfCall call = call();

        call.setOperation("getControlTechnologies");
        call.setReturnType(mappings.controlTechnologies());

        ControlTechnology[] technologies = (ControlTechnology[]) call.requestResponse(new Object[] {});

        return technologies;
    }

    public ControlMeasure[] importControlMeasures(String folderPath, String[] fileNames, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("importControlMeasures");
        call.addParam("folderPath", mappings.string());
        call.addParam("fileNames", mappings.strings());
        call.addParam("user", mappings.user());

        call.setReturnType(mappings.controlMeasures());

        return (ControlMeasure[]) call.requestResponse(new Object[] { folderPath, fileNames, user });
    }
}

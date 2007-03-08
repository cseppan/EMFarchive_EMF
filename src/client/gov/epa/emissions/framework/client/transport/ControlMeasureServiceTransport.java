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
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

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

    public int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("addMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setIntegerReturnType();

        return (Integer)call.requestResponse(new Object[] { measure, sccs });
    }

    public void removeMeasure(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeMeasure");
        call.addIntegerParam("controlMeasureId");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(controlMeasureId) });
    }

    public ControlMeasure obtainLockedMeasure(User owner, int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedMeasure");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { owner, new Integer(controlMeasureId) });
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

    public void releaseLockedControlMeasure(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedControlMeasure");
        call.addIntegerParam("controlMeasureId");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { new Integer(controlMeasureId) });
    }

    public ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateMeasure");
        call.addParam("measure", mappings.controlMeasure());
        call.addParam("sccs", mappings.sccs());
        call.setReturnType(mappings.controlMeasure());

        return (ControlMeasure) call.requestResponse(new Object[] { measure, sccs });
    }

    public Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccsWithDescriptions");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });

        return sccs;
    }

    public Scc[] getSccs(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSccs");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.sccs());

        Scc[] sccs = (Scc[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });

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

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEfficiencyRecords");
        call.addIntegerParam("controlMeasureId");
        call.setReturnType(mappings.efficiencyRecords());

        return (EfficiencyRecord[]) call.requestResponse(new Object[] { new Integer(controlMeasureId) });
    }

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter) throws EmfException {
        EmfCall call = call();

        call.setOperation("getEfficiencyRecords");
        call.addIntegerParam("controlMeasureId");
        call.addIntegerParam("recordLimit");
        call.addStringParam("filter");
        call.setReturnType(mappings.efficiencyRecords());

        return (EfficiencyRecord[]) call.requestResponse(new Object[] { new Integer(controlMeasureId), new Integer(recordLimit), filter });
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EmfCall call = call();

        call.setOperation("addEfficiencyRecord");
        call.addParam("efficiencyRecord", mappings.efficiencyRecord());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { efficiencyRecord });
    }

    public void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeEfficiencyRecord");
        call.addIntegerParam("efficiencyRecordId");
        call.setVoidReturnType();

        call.requestResponse(new Object[] { new Integer(efficiencyRecordId) });
    }

    public void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateEfficiencyRecord");
        call.addParam("efficiencyRecord", mappings.efficiencyRecord());
        call.setVoidReturnType();

        call.requestResponse(new Object[] { efficiencyRecord });
    }
}

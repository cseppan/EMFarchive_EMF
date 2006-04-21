package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.Grid;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.Speciation;

public class CaseServiceTransport implements CaseService {

    private CallFactory callFactory;

    private EmfMappings mappings;

    public CaseServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new EmfMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Case Service");
    }

    public Case[] getCases() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.setReturnType(mappings.cases());

        return (Case[]) call.requestResponse(new Object[] {});
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAbbreviations");
        call.setReturnType(mappings.abbreviations());

        return (Abbreviation[]) call.requestResponse(new Object[] {});
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAirQualityModels");
        call.setReturnType(mappings.airQualityModels());

        return (AirQualityModel[]) call.requestResponse(new Object[] {});
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseCategories");
        call.setReturnType(mappings.caseCategories());

        return (CaseCategory[]) call.requestResponse(new Object[] {});
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmissionsYears");
        call.setReturnType(mappings.emissionsYears());

        return (EmissionsYear[]) call.requestResponse(new Object[] {});
    }

    public Grid[] getGrids() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGrids");
        call.setReturnType(mappings.grids());

        return (Grid[]) call.requestResponse(new Object[] {});
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeteorlogicalYears");
        call.setReturnType(mappings.meteorlogicalYears());

        return (MeteorlogicalYear[]) call.requestResponse(new Object[] {});
    }

    public Speciation[] getSpeciations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSpeciations");
        call.setReturnType(mappings.speciations());

        return (Speciation[]) call.requestResponse(new Object[] {});
    }

    public void addCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCase");
        call.addParam("element", mappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public void removeCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCase");
        call.addParam("element", mappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addParam("element", mappings.caseObject());
        call.setReturnType(mappings.caseObject());

        return (Case) call.requestResponse(new Object[] { owner, element });
    }

    public Case releaseLocked(Case locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("element", mappings.caseObject());
        call.setReturnType(mappings.caseObject());

        return (Case) call.requestResponse(new Object[] { locked });
    }

    public Case updateCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("update");
        call.addParam("element", mappings.caseObject());
        call.setReturnType(mappings.caseObject());

        return (Case) call.requestResponse(new Object[] { element });
    }

}

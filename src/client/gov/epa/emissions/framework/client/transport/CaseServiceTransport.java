package gov.epa.emissions.framework.client.transport;

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

    public Abbreviation[] getAbbreviations() {
        // NOTE Auto-generated method stub
        return null;
    }

    public AirQualityModel[] getAirQualityModels() {
        // NOTE Auto-generated method stub
        return null;
    }

    public CaseCategory[] getCaseCategories() {
        // NOTE Auto-generated method stub
        return null;
    }

    public EmissionsYear[] getEmissionsYears() {
        // NOTE Auto-generated method stub
        return null;
    }

    public Grid[] getGrids() {
        // NOTE Auto-generated method stub
        return null;
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() {
        // NOTE Auto-generated method stub
        return null;
    }

    public Speciation[] getSpeciations() {
        // NOTE Auto-generated method stub
        return null;
    }

    public void addCase(Case element) {
        // NOTE Auto-generated method stub

    }

    public void removeCase(Case element) {
        // NOTE Auto-generated method stub

    }

}

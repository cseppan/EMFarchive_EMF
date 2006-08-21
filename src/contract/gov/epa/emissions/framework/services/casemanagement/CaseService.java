package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface CaseService {

    Abbreviation[] getAbbreviations() throws EmfException;

    AirQualityModel[] getAirQualityModels() throws EmfException;

    CaseCategory[] getCaseCategories() throws EmfException;

    EmissionsYear[] getEmissionsYears() throws EmfException;
    
    GridResolution[] getGridResolutions() throws EmfException;

    Grid[] getGrids() throws EmfException;

    MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException;

    Speciation[] getSpeciations() throws EmfException;

    InputName[] getInputNames() throws EmfException;

    InputEnvtVar[] getInputEnvtVars() throws EmfException;
 
    Program[] getPrograms() throws EmfException;

    Case[] getCases() throws EmfException;

    void addCase(Case element) throws EmfException;

    void removeCase(Case element) throws EmfException;

    Case obtainLocked(User owner, Case element) throws EmfException;

    Case releaseLocked(Case locked) throws EmfException;

    Case updateCase(Case element) throws EmfException;
    
    void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport) throws EmfException;

    InputName addCaseInputName(InputName name) throws EmfException;

}

package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseRunStatus;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;

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
 
    CaseProgram[] getPrograms() throws EmfException;

    ModelToRun[] getModelToRuns() throws EmfException;

    SubDir[] getSubDirs() throws EmfException;

    Case[] getCases() throws EmfException;

    void addCase(Case element) throws EmfException;

    void removeCase(Case element) throws EmfException;

    Case obtainLocked(User owner, Case element) throws EmfException;

    Case releaseLocked(Case locked) throws EmfException;

    Case updateCase(Case element) throws EmfException;
    
    void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport) throws EmfException;

    InputName addCaseInputName(InputName name) throws EmfException;

    CaseProgram addProgram(CaseProgram program) throws EmfException;
    
    ModelToRun addModelToRun(ModelToRun model) throws EmfException;

    GridResolution addGridResolution(GridResolution gridResolution) throws EmfException;

    InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException;

    SubDir addSubDir(SubDir subdir) throws EmfException;

    CaseInput addCaseInput(CaseInput input) throws EmfException;
    
    void updateCaseInput(CaseInput input) throws EmfException;
    
    void removeCaseInputs(CaseInput[] inputs) throws EmfException;
    
    CaseInput[] getCaseInputs(int caseId) throws EmfException;
    
    Case[] copyCaseObject(int[] toCopy) throws EmfException;
    
    CaseJob addCaseJob(CaseJob job) throws EmfException;
    
    CaseJob[] getCaseJobs(int caseId) throws EmfException;

    CaseRunStatus addCaseRunStatus(CaseRunStatus status) throws EmfException;
    
    CaseRunStatus[] getCaseRunStatus(int casejobId) throws EmfException;
    
    Executable addExecutable(Executable exe) throws EmfException;
    
    Executable[] getExecutables(int casejobId) throws EmfException;

    void removeCaseJobs(CaseJob[] jobs) throws EmfException;

    void updateCaseJob(CaseJob job) throws EmfException;
}

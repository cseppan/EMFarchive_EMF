package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;

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

    Case addCase(User owner, Case element) throws EmfException;

    CaseCategory addCaseCategory(CaseCategory element) throws EmfException;
    
    Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException;

    void removeCase(Case element) throws EmfException;

    Case obtainLocked(User owner, Case element) throws EmfException;

    Case releaseLocked(Case locked) throws EmfException;

    Case updateCase(Case element) throws EmfException;
    
    void export(User user, String dirName, String purpose, boolean overWrite, int caseId) throws EmfException;

    InputName addCaseInputName(InputName name) throws EmfException;

    CaseProgram addProgram(CaseProgram program) throws EmfException;
    
    ModelToRun addModelToRun(ModelToRun model) throws EmfException;

    GridResolution addGridResolution(GridResolution gridResolution) throws EmfException;

    InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException;

    SubDir addSubDir(SubDir subdir) throws EmfException;

    CaseInput addCaseInput(CaseInput input) throws EmfException;
    
    void updateCaseInput(User user, CaseInput input) throws EmfException;
    
    void removeCaseInputs(CaseInput[] inputs) throws EmfException;
    
    CaseInput[] getCaseInputs(int caseId) throws EmfException;
    
    Case[] copyCaseObject(int[] toCopy, User user) throws EmfException;
    
    CaseJob addCaseJob(CaseJob job) throws EmfException;
    
    CaseJob getCaseJob(int jobId) throws EmfException;
    
    CaseJob[] getCaseJobs(int caseId) throws EmfException;
    
    int[] getJobIds(int caseId, String[] jobNames) throws EmfException;
    
    String[] getDependentJobs(int jobId) throws EmfException;
    
    String[] getAllValidJobs(int jobId) throws EmfException;

    JobRunStatus[] getJobRunStatuses() throws EmfException;
    
    void removeCaseJobs(CaseJob[] jobs) throws EmfException;

    void updateCaseJob(User user, CaseJob job) throws EmfException;

    Host[] getHosts() throws EmfException;

    Host addHost(Host host) throws EmfException;
        
    Executable addExecutable(Executable exe) throws EmfException;
    
    ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException;
    
    ParameterEnvVar[] getParameterEnvVars() throws EmfException;

    ValueType addValueType(ValueType type) throws EmfException;
    
    ValueType[] getValueTypes() throws EmfException;

    ParameterName addParameterName(ParameterName name) throws EmfException;
    
    ParameterName[] getParameterNames() throws EmfException;
    
    CaseParameter addCaseParameter(CaseParameter param) throws EmfException;
    
    void removeCaseParameters(CaseParameter[] params) throws EmfException;
    
    CaseParameter[] getCaseParameters(int caseId) throws EmfException;

    void updateCaseParameter(User user, CaseParameter parameter) throws EmfException;

    void runJobs(CaseJob[] jobs, User user) throws EmfException;
    String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException;

    // Used for CaseService ExportInputs
    void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException;
    void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose) throws EmfException;
    
    // For command line client
    int recordJobMessage(JobMessage message, String jobKey) throws EmfException;
    int recordJobMessage(JobMessage[] msgs, String[] keys) throws EmfException;
    JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException;
    
    void registerOutput(CaseOutput output, String jobKey) throws EmfException;
    void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException;
    CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException;
    
    String restoreTaskManagers() throws EmfException;

    String printStatusCaseJobTaskManager() throws EmfException;

    Case[] getCases(CaseCategory category) throws EmfException;
    
    String validateJobs(Integer[] jobIDs) throws EmfException;

 }

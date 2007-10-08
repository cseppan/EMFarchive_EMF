package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.Grid;
import gov.epa.emissions.framework.services.casemanagement.GridResolution;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;

public class CaseServiceTransport implements CaseService {

    private CallFactory callFactory;

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    private EmfCall emfCall;
    
    public CaseServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    private EmfCall call() throws EmfException {
        if (emfCall==null){
//            System.out.println("No emfcall so create");
//            emfCall=callFactory.createCall("Case Service");
            emfCall=callFactory.createSessionEnabledCall("Case Service");

        }
        return this.emfCall;
    }

    public synchronized Case[] getCases() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] {});
    }

    public synchronized Abbreviation[] getAbbreviations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAbbreviations");
        call.setReturnType(caseMappings.abbreviations());

        return (Abbreviation[]) call.requestResponse(new Object[] {});
    }

    public synchronized AirQualityModel[] getAirQualityModels() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAirQualityModels");
        call.setReturnType(caseMappings.airQualityModels());

        return (AirQualityModel[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseCategory[] getCaseCategories() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseCategories");
        call.setReturnType(caseMappings.caseCategories());

        return (CaseCategory[]) call.requestResponse(new Object[] {});
    }

    public synchronized EmissionsYear[] getEmissionsYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmissionsYears");
        call.setReturnType(caseMappings.emissionsYears());

        return (EmissionsYear[]) call.requestResponse(new Object[] {});
    }

    public synchronized Grid[] getGrids() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGrids");
        call.setReturnType(caseMappings.grids());

        return (Grid[]) call.requestResponse(new Object[] {});
    }

    public synchronized MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeteorlogicalYears");
        call.setReturnType(caseMappings.meteorlogicalYears());

        return (MeteorlogicalYear[]) call.requestResponse(new Object[] {});
    }

    public synchronized Speciation[] getSpeciations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSpeciations");
        call.setReturnType(caseMappings.speciations());

        return (Speciation[]) call.requestResponse(new Object[] {});
    }

    public synchronized void addCase(User user, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCase");
        call.addParam("user", dataMappings.user());
        call.addParam("element", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { user, element });
    }

    public synchronized void removeCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCase");
        call.addParam("element", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public synchronized Case obtainLocked(User owner, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", dataMappings.user());
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { owner, element });
    }

    public synchronized Case releaseLocked(Case locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { locked });
    }

    public synchronized Case updateCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCase");
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { element });
    }

    public synchronized GridResolution[] getGridResolutions() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGridResolutions");
        call.setReturnType(caseMappings.gridResolutions());

        return (GridResolution[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseInput[] getCaseInputs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] {});
    }

    public synchronized InputName[] getInputNames() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputNames");
        call.setReturnType(caseMappings.inputnames());

        return (InputName[]) call.requestResponse(new Object[] {});
    }

    public synchronized InputEnvtVar[] getInputEnvtVars() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputEnvtVars");
        call.setReturnType(caseMappings.inputEnvtVars());

        return (InputEnvtVar[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseProgram[] getPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getPrograms");
        call.setReturnType(caseMappings.programs());

        return (CaseProgram[]) call.requestResponse(new Object[] {});
    }

    public synchronized void export(User user, String dirName, String purpose, boolean overWrite, int caseId)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("export");
        call.addParam("user", dataMappings.user());
        call.addStringParam("dirName");
        call.addStringParam("purpose");
        call.addBooleanParameter("overWrite");
        call.addParam("caseId", caseMappings.integer());
        call.setVoidReturnType();

        call.request(new Object[] { user, dirName, purpose, Boolean.valueOf(overWrite), Integer.valueOf(caseId) });
    }

    
    public synchronized InputName addCaseInputName(InputName name) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInputName");
        call.addParam("name", caseMappings.inputname());
        call.setReturnType(caseMappings.inputname());

        return (InputName) call.requestResponse(new Object[] { name });
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        EmfCall call = call();

        call.setOperation("addProgram");
        call.addParam("program", caseMappings.program());
        call.setReturnType(caseMappings.program());

        return (CaseProgram) call.requestResponse(new Object[] { program });
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        EmfCall call = call();

        call.setOperation("addInputEnvtVar");
        call.addParam("inputEnvtVar", caseMappings.inputEnvtVar());
        call.setReturnType(caseMappings.inputEnvtVar());

        return (InputEnvtVar) call.requestResponse(new Object[] { inputEnvtVar });
    }

    public synchronized ModelToRun[] getModelToRuns() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModelToRuns");
        call.setReturnType(caseMappings.modelToRuns());

        return (ModelToRun[]) call.requestResponse(new Object[] {});
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        EmfCall call = call();

        call.setOperation("addModelToRun");
        call.addParam("model", caseMappings.modelToRun());
        call.setReturnType(caseMappings.modelToRun());

        return (ModelToRun) call.requestResponse(new Object[] { model });
    }

    public synchronized GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        EmfCall call = call();

        call.setOperation("addGridResolution");
        call.addParam("gridResolution", caseMappings.gridResolution());
        call.setReturnType(caseMappings.gridResolution());

        return (GridResolution) call.requestResponse(new Object[] { gridResolution });
    }

    public synchronized SubDir[] getSubDirs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSubDirs");
        call.setReturnType(caseMappings.subdirs());

        return (SubDir[]) call.requestResponse(new Object[] {});
    }

    public synchronized SubDir addSubDir(SubDir subdir) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSubDir");
        call.addParam("subdir", caseMappings.subdir());
        call.setReturnType(caseMappings.subdir());

        return (SubDir) call.requestResponse(new Object[] { subdir });
    }

    public synchronized CaseInput addCaseInput(CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInput");
        call.addParam("input", caseMappings.caseinput());
        call.setReturnType(caseMappings.caseinput());

        return (CaseInput) call.requestResponse(new Object[] { input });
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseInput");
        call.addParam("user", dataMappings.user());
        call.addParam("input", caseMappings.caseinput());
        call.setVoidReturnType();

        call.request(new Object[] { user, input });
    }

    public synchronized void removeCaseInputs(CaseInput[] inputs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseInputs");
        call.addParam("inputs", caseMappings.caseinputs());
        call.setVoidReturnType();

        call.request(new Object[] { inputs });
    }

    public synchronized CaseInput[] getCaseInputs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.addParam("caseId", dataMappings.integer());
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized Case[] copyCaseObject(int[] toCopy, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyCaseObject");
        call.addIntArrayParam();
        call.addParam("user", dataMappings.user());
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { toCopy, user });
    }

    public synchronized CaseJob addCaseJob(CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseJob");
        call.addParam("job", caseMappings.casejob());
        call.setReturnType(caseMappings.casejob());

        return (CaseJob) call.requestResponse(new Object[] { job });
    }

    public synchronized CaseJob[] getCaseJobs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseJobs");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.casejobs());

        return (CaseJob[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized CaseJob getCaseJob(int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseJob");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.casejob());

        return (CaseJob) call.requestResponse(new Object[] { new Integer(jobId) });
    }

    public synchronized JobRunStatus[] getJobRunStatuses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobRunStatuses");
        call.setReturnType(caseMappings.jobRunStatuses());

        return (JobRunStatus[]) call.requestResponse(new Object[] {});
    }

    public synchronized Executable[] getExecutables(int casejobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getExecutables");
        call.setReturnType(caseMappings.executables());

        return (Executable[]) call.requestResponse(new Object[] {});
    }

    public synchronized void removeCaseJobs(CaseJob[] jobs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseJobs");
        call.addParam("jobs", caseMappings.casejobs());
        call.setVoidReturnType();

        call.request(new Object[] { jobs });
    }

    public synchronized void updateCaseJob(User user, CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseJob");
        call.addParam("user", dataMappings.user());
        call.addParam("job", caseMappings.casejob());
        call.setVoidReturnType();

        call.request(new Object[] { user, job });
    }

    public synchronized Host[] getHosts() throws EmfException {
        EmfCall call = call();

        call.setOperation("getHosts");
        call.setReturnType(caseMappings.hosts());

        return (Host[]) call.requestResponse(new Object[] {});
    }

    public synchronized Host addHost(Host host) throws EmfException {
        EmfCall call = call();

        call.setOperation("addHost");
        call.addParam("host", caseMappings.host());
        call.setReturnType(caseMappings.host());

        return (Host) call.requestResponse(new Object[] { host });
    }

    public synchronized Executable addExecutable(Executable exe) throws EmfException {
        EmfCall call = call();

        call.setOperation("addExecutable");
        call.addParam("exe", caseMappings.executable());
        call.setReturnType(caseMappings.executable());

        return (Executable) call.requestResponse(new Object[] { exe });
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        EmfCall call = call();

        call.setOperation("addParameterEnvVar");
        call.addParam("envVar", caseMappings.parameterEnvVar());
        call.setReturnType(caseMappings.parameterEnvVar());

        return (ParameterEnvVar) call.requestResponse(new Object[] { envVar });
    }

    public synchronized ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        EmfCall call = call();

        call.setOperation("getParameterEnvVars");
        call.setReturnType(caseMappings.parameterEnvVars());

        return (ParameterEnvVar[]) call.requestResponse(new Object[] {});
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("addValueType");
        call.addParam("type", caseMappings.valueType());
        call.setReturnType(caseMappings.valueType());

        return (ValueType) call.requestResponse(new Object[] { type });
    }

    public synchronized ValueType[] getValueTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getValueTypes");
        call.setReturnType(caseMappings.valueTypes());

        return (ValueType[]) call.requestResponse(new Object[] {});
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        EmfCall call = call();

        call.setOperation("addParameterName");
        call.addParam("name", caseMappings.parameterName());
        call.setReturnType(caseMappings.parameterName());

        return (ParameterName) call.requestResponse(new Object[] { name });
    }

    public synchronized ParameterName[] getParameterNames() throws EmfException {
        EmfCall call = call();

        call.setOperation("getParameterNames");
        call.setReturnType(caseMappings.parameterNames());

        return (ParameterName[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseParameter addCaseParameter(CaseParameter param) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseParameter");
        call.addParam("param", caseMappings.caseParameter());
        call.setReturnType(caseMappings.caseParameter());

        return (CaseParameter) call.requestResponse(new Object[] { param });
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseParameters");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.caseParameters());

        return (CaseParameter[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized void removeCaseParameters(CaseParameter[] params) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseParameters");
        call.addParam("params", caseMappings.caseParameters());
        call.setVoidReturnType();

        call.request(new Object[] { params });
    }

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseParameter");
        call.addParam("user", dataMappings.user());
        call.addParam("parameter", caseMappings.caseParameter());
        call.setVoidReturnType();

        call.request(new Object[] { user, parameter });
    }

    public synchronized void runJobs(CaseJob[] jobs, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runJobs");
        call.addParam("jobs", caseMappings.casejobs());
        call.addParam("user", dataMappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { jobs, user });
    }

    public synchronized String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        EmfCall call = call();
        call.setOperation("runJobs");
        call.addParam("jobids", caseMappings.casejobIds());
        call.addParam("caseId", caseMappings.integer());        
        call.addParam("user", dataMappings.user());
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { jobIds, Integer.valueOf(caseId), user });
    }

    public synchronized void exportCaseInputs(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        EmfCall call = call();
        call.setOperation("exportCaseInputs");
        call.addParam("user", dataMappings.user());
        call.addParam("caseInputIds", caseMappings.caseInputIds());
        call.addStringParam("purpose");
        
        call.setVoidReturnType();
        call.request(new Object[] { user, caseInputIds, purpose });
        
    }

    public synchronized void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        EmfCall call = call();
        call.setOperation("exportCaseInputsWithOverwrite");
        call.addParam("user", dataMappings.user());
        call.addParam("caseInputIds", caseMappings.caseInputIds());
        call.addStringParam("purpose");
        
        call.setVoidReturnType();
        call.request(new Object[] { user, caseInputIds, purpose });
        
        
    }

    public synchronized int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("recordJobMessage");
        call.addParam("message", caseMappings.jobMessage());
        call.addStringParam("jobKey");
        call.setIntegerReturnType();
        call.setTimeOut(20000); //set time out in milliseconds to terminate if service doesn't response
        
        return (Integer)call.requestResponse(new Object[]{ message, jobKey });
    }
    
    public synchronized int recordJobMessage(JobMessage[] msgs, String[] keys) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("recordJobMessage");
        call.addParam("msgs", caseMappings.jobMessages());
        call.addParam("keys", caseMappings.strings());
        call.setIntegerReturnType();
        call.setTimeOut(20000); //set time out in milliseconds to terminate if service doesn't response
        
        return (Integer)call.requestResponse(new Object[]{ msgs, keys });
    }

    public synchronized JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getJobMessages");
        call.addIntegerParam("caseId");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.jobMessages());
        
        return (JobMessage[])call.requestResponse(new Object[]{new Integer(caseId), new Integer(jobId)});
    }

    public synchronized String[] getAllValidJobs(int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllValidJobs");
        call.addIntegerParam("jobId");
        call.setStringArrayReturnType();

        return (String[]) call.requestResponse(new Object[] { new Integer(jobId) });
    }

    public synchronized String[] getDependentJobs(int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDependentJobs");
        call.addIntegerParam("jobId");
        call.setStringArrayReturnType();

        return (String[]) call.requestResponse(new Object[] { new Integer(jobId) });
    }
    
    public synchronized int[] getJobIds(int caseId, String[] names) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getJobIds");
        call.addIntegerParam("caseId");
        call.addParam("names", caseMappings.strings());
        call.setIntArrayReturnType();
        
        return (int[]) call.requestResponse(new Object[] { new Integer(caseId), names });
    }

    public synchronized String restoreTaskManagers() throws EmfException {
        EmfCall call = call();
        call.setOperation("restoreTaskManagers");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { });
    }

    public synchronized String printStatusCaseJobTaskManager() throws EmfException {
        EmfCall call = call();
        call.setOperation("printStatusCaseJobTaskManager");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { });
    }

}

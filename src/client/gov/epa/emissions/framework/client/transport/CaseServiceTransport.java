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
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;

public class CaseServiceTransport implements CaseService {

    private CallFactory callFactory;

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    public CaseServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    private EmfCall call() throws EmfException {
        return callFactory.createCall("Case Service");
    }

    public Case[] getCases() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] {});
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAbbreviations");
        call.setReturnType(caseMappings.abbreviations());

        return (Abbreviation[]) call.requestResponse(new Object[] {});
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAirQualityModels");
        call.setReturnType(caseMappings.airQualityModels());

        return (AirQualityModel[]) call.requestResponse(new Object[] {});
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseCategories");
        call.setReturnType(caseMappings.caseCategories());

        return (CaseCategory[]) call.requestResponse(new Object[] {});
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmissionsYears");
        call.setReturnType(caseMappings.emissionsYears());

        return (EmissionsYear[]) call.requestResponse(new Object[] {});
    }

    public Grid[] getGrids() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGrids");
        call.setReturnType(caseMappings.grids());

        return (Grid[]) call.requestResponse(new Object[] {});
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeteorlogicalYears");
        call.setReturnType(caseMappings.meteorlogicalYears());

        return (MeteorlogicalYear[]) call.requestResponse(new Object[] {});
    }

    public Speciation[] getSpeciations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSpeciations");
        call.setReturnType(caseMappings.speciations());

        return (Speciation[]) call.requestResponse(new Object[] {});
    }

    public void addCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCase");
        call.addParam("element", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public void removeCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCase");
        call.addParam("element", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", dataMappings.user());
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { owner, element });
    }

    public Case releaseLocked(Case locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { locked });
    }

    public Case updateCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCase");
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { element });
    }

    public GridResolution[] getGridResolutions() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGridResolutions");
        call.setReturnType(caseMappings.gridResolutions());

        return (GridResolution[]) call.requestResponse(new Object[] {});
    }

    public CaseInput[] getCaseInputs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] {});
    }

    public InputName[] getInputNames() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputNames");
        call.setReturnType(caseMappings.inputnames());

        return (InputName[]) call.requestResponse(new Object[] {});
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputEnvtVars");
        call.setReturnType(caseMappings.inputEnvtVars());

        return (InputEnvtVar[]) call.requestResponse(new Object[] {});
    }

    public CaseProgram[] getPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getPrograms");
        call.setReturnType(caseMappings.programs());

        return (CaseProgram[]) call.requestResponse(new Object[] {});
    }

    public void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("export");
        call.addParam("user", dataMappings.user());
        call.addStringParam("dirName");
        call.addStringParam("purpose");
        call.addBooleanParameter("overWrite");
        call.addParam("caseToExport", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { user, dirName, purpose, Boolean.valueOf(overWrite), caseToExport });
    }

    public InputName addCaseInputName(InputName name) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInputName");
        call.addParam("name", caseMappings.inputname());
        call.setReturnType(caseMappings.inputname());

        return (InputName) call.requestResponse(new Object[] { name });
    }

    public CaseProgram addProgram(CaseProgram program) throws EmfException {
        EmfCall call = call();

        call.setOperation("addProgram");
        call.addParam("program", caseMappings.program());
        call.setReturnType(caseMappings.program());

        return (CaseProgram) call.requestResponse(new Object[] { program });
    }

    public InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        EmfCall call = call();

        call.setOperation("addInputEnvtVar");
        call.addParam("inputEnvtVar", caseMappings.inputEnvtVar());
        call.setReturnType(caseMappings.inputEnvtVar());

        return (InputEnvtVar) call.requestResponse(new Object[] { inputEnvtVar });
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModelToRuns");
        call.setReturnType(caseMappings.modelToRuns());

        return (ModelToRun[]) call.requestResponse(new Object[] {});
    }

    public ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        EmfCall call = call();

        call.setOperation("addModelToRun");
        call.addParam("model", caseMappings.modelToRun());
        call.setReturnType(caseMappings.modelToRun());

        return (ModelToRun) call.requestResponse(new Object[] { model });
    }

    public GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        EmfCall call = call();

        call.setOperation("addGridResolution");
        call.addParam("gridResolution", caseMappings.gridResolution());
        call.setReturnType(caseMappings.gridResolution());

        return (GridResolution) call.requestResponse(new Object[] { gridResolution });
    }

    public SubDir[] getSubDirs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSubDirs");
        call.setReturnType(caseMappings.subdirs());

        return (SubDir[]) call.requestResponse(new Object[] {});
    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSubDir");
        call.addParam("subdir", caseMappings.subdir());
        call.setReturnType(caseMappings.subdir());

        return (SubDir) call.requestResponse(new Object[] { subdir });
    }

    public CaseInput addCaseInput(CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInput");
        call.addParam("input", caseMappings.caseinput());
        call.setReturnType(caseMappings.caseinput());

        return (CaseInput) call.requestResponse(new Object[] { input });
    }

    public void updateCaseInput(CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseInput");
        call.addParam("input", caseMappings.caseinput());
        call.setVoidReturnType();

        call.request(new Object[] { input });
    }

    public void removeCaseInputs(CaseInput[] inputs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseInputs");
        call.addParam("inputs", caseMappings.caseinputs());
        call.setVoidReturnType();

        call.request(new Object[] { inputs });
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public Case[] copyCaseObject(int[] toCopy) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("copyCaseObject");
        call.addIntArrayParam();
        call.setReturnType(caseMappings.cases());
        
        return (Case[]) call.requestResponse(new Object[] {toCopy});
    }

    public CaseJob addCaseJob(CaseJob job) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addCaseJob");
        call.addParam("job", caseMappings.casejob());
        call.setReturnType(caseMappings.casejob());
        
        return (CaseJob) call.requestResponse(new Object[]{job});
    }

    public CaseJob[] getCaseJobs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseJobs");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.casejobs());

        return (CaseJob[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public CaseJob getCaseJob(int jobId) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getCaseJob");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.casejob());
        
        return (CaseJob) call.requestResponse(new Object[] { new Integer(jobId) });
    }

    public JobRunStatus[] getJobRunStatuses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobRunStatuses");
        call.setReturnType(caseMappings.jobRunStatuses());

        return (JobRunStatus[]) call.requestResponse(new Object[] { });
    }

    public Executable[] getExecutables(int casejobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getExecutables");
        call.setReturnType(caseMappings.executables());

        return (Executable[]) call.requestResponse(new Object[] { });
    }

    public void removeCaseJobs(CaseJob[] jobs) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction...");
    }

    public void updateCaseJob(CaseJob job) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("under construction...");
    }

    public Host[] getHosts() throws EmfException {
        EmfCall call = call();

        call.setOperation("getHosts");
        call.setReturnType(caseMappings.hosts());

        return (Host[]) call.requestResponse(new Object[] { });
    }

    public Host addHost(Host host) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addHost");
        call.addParam("host", caseMappings.host());
        call.setReturnType(caseMappings.host());
        
        return (Host) call.requestResponse(new Object[] {host});
    }

    public Executable addExecutable(Executable exe) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addExecutable");
        call.addParam("exe", caseMappings.executable());
        call.setReturnType(caseMappings.executable());
        
        return (Executable) call.requestResponse(new Object[] {exe});
    }

}

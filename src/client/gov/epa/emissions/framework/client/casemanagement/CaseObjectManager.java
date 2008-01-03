package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CaseObjectManager {

    private static CaseObjectManager com = null;

    private CaseProgram[] programs = null;

    private InputEnvtVar[] inputEnvtVars = null;

    private InputName[] inputNames = null;

    private SubDir[] subDirs = null;

    // TBD: Probably should put these into a DataObjectManager

    private Sector[] sectors = null;

    private Sector[] sectorsWithAll = null;

    private static Sector allSectors = null;

    private static CaseJob allJobsForSector = null;

    private int lastCaseId = -1;

    private CaseJob[] jobsForLastCaseId = null;

    private DatasetType[] datasetTypes = null;

    private DataCommonsService dataCommonsService = null;

    private List<Host> hosts;

    private JobRunStatus[] sortedStatuses;

    private List<ParameterEnvVar> parameterEnvtVars;

    private List<ParameterName> parameterNames;

    private List<ValueType> parameterValueTypes;

    private List<CaseCategory> categoies;

    private List<Abbreviation> abbreviations;

    private List<Project> projects;

    private List<Region> regions;

    private List<ModelToRun> modelToRuns;

    // up to here

    private EmfSession session = null;

    private CaseService caseService = null;

    public static CaseObjectManager getCaseObjectManager(EmfSession aSession) {
        allSectors = new Sector("All sectors", "All sectors");
        allJobsForSector = new CaseJob("All jobs for sector");
        allJobsForSector.setId(0); // to match the id 0 in parameters tab and inputs tab

        if (com == null)
            com = new CaseObjectManager(aSession);
        return com;
    }

    private CaseObjectManager(EmfSession aSession) {
        this.session = aSession;
        this.caseService = session.caseService();

        this.dataCommonsService = session.dataCommonsService();
    }

    public synchronized void refresh() throws EmfException {
        // refresh the pieces of the cache that have been used so far
        if (programs != null)
            programs = caseService.getPrograms();
        if (inputEnvtVars != null)
            inputEnvtVars = caseService.getInputEnvtVars();
        if (inputNames != null)
            inputNames = caseService.getInputNames();
        if (subDirs != null)
            subDirs = caseService.getSubDirs();

        if (sectors != null)
            sectors = dataCommonsService.getSectors();
        if (datasetTypes != null)
            datasetTypes = dataCommonsService.getDatasetTypes();

        lastCaseId = -1;
        jobsForLastCaseId = null;
    }

    public synchronized DatasetType[] getDatasetTypes() throws EmfException {
        if (datasetTypes == null)
            datasetTypes = dataCommonsService.getDatasetTypes();

        return datasetTypes;
    }

    public synchronized Sector[] getSectors() throws EmfException {
        if (sectors == null)
            sectors = dataCommonsService.getSectors();

        return sectors;
    }

    public synchronized Sector[] getSectorsWithAll() throws EmfException {
        if (sectorsWithAll == null) {
            List list = new ArrayList();
            list.add(allSectors);
            list.addAll(Arrays.asList(getSectors()));
            sectorsWithAll = (Sector[]) list.toArray(new Sector[0]);
        }
        return sectorsWithAll;
    }

    public synchronized Sector getSectorForAll() {
        return allSectors;
    }

    public synchronized SubDir[] getSubDirs() throws EmfException {
        if (subDirs == null)
            subDirs = caseService.getSubDirs();

        return subDirs;
    }

    public synchronized CaseJob[] getCaseJobsWithAll(int caseId) throws EmfException {
        if (this.lastCaseId == caseId) // if the same as the last case,
        {
            return this.jobsForLastCaseId;
        }
        // otherwise, get a new list
        List<CaseJob> jobs = new ArrayList<CaseJob>();
        jobs.addAll(Arrays.asList(caseService.getCaseJobs(caseId)));
        jobs.add(0, allJobsForSector);
        jobsForLastCaseId = jobs.toArray(new CaseJob[jobs.size()]);
        //Arrays.sort(jobsForLastCaseId, new CaseJobNameComparator());
        lastCaseId = caseId;
        return jobsForLastCaseId;
    }

    public synchronized CaseJob getJobForAll() {
        return allJobsForSector;
    }

    public synchronized SubDir addSubDir(SubDir subDir) throws EmfException {
        SubDir newVar = caseService.addSubDir(subDir);
        // refresh the cache when a new one is added
        subDirs = caseService.getSubDirs();

        return newVar;
    }

    public synchronized SubDir getOrAddSubDir(Object selected) throws EmfException {
        if (selected == null)
            return null;

        SubDir subDir = null;
        if (selected instanceof String) {
            subDir = new SubDir(selected.toString());
        } else if (selected instanceof SubDir) {
            subDir = (SubDir) selected;
        }
        this.getSubDirs(); // make sure programs have been retrieved

        for (int i = 0; i < subDirs.length; i++) {
            if (subDirs[i].toString().equalsIgnoreCase(subDir.getName().toString()))
                return subDirs[i]; // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addSubDir(subDir);
    }

    @SuppressWarnings("unchecked")
    public synchronized InputName[] getInputNames() throws EmfException {
        if (inputNames == null)
            inputNames = caseService.getInputNames();

        List<InputName> inputs = Arrays.asList(inputNames);
        Collections.sort(inputs);

        return inputs.toArray(new InputName[0]);
    }

    public synchronized InputName addInputName(InputName inputName) throws EmfException {
        InputName newVar = caseService.addCaseInputName(inputName);
        // refresh the cache when a new one is added
        inputNames = caseService.getInputNames();
        List<InputName> inputs = Arrays.asList(inputNames);
        Collections.sort(inputs);

        return newVar;
    }

    public synchronized InputName getOrAddInputName(Object selected) throws EmfException {
        if (selected == null)
            return null;

        InputName inputName = null;
        if (selected instanceof String) {
            inputName = new InputName(selected.toString());
        } else if (selected instanceof InputName) {
            inputName = (InputName) selected;
        }
        this.getInputNames(); // make sure programs have been retrieved

        for (int i = 0; i < inputNames.length; i++) {
            if (inputNames[i].toString().equalsIgnoreCase(inputName.getName().toString()))
                return inputNames[i]; // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addInputName(inputName);
    }

    public synchronized InputEnvtVar[] getInputEnvtVars() throws EmfException {
        if (inputEnvtVars == null)
            inputEnvtVars = caseService.getInputEnvtVars();

        List<InputEnvtVar> inputVars = Arrays.asList(inputEnvtVars);
        Collections.sort(inputVars);

        return inputVars.toArray(new InputEnvtVar[0]);
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        InputEnvtVar newVar = caseService.addInputEnvtVar(inputEnvtVar);
        // refresh the cache when a new one is added
        inputEnvtVars = caseService.getInputEnvtVars();
        List<InputEnvtVar> inputVars = Arrays.asList(inputEnvtVars);
        Collections.sort(inputVars);

        return newVar;
    }

    public synchronized InputEnvtVar getOrAddInputEnvtVar(Object selected) throws EmfException {
        if (selected == null)
            return null;

        InputEnvtVar inputEnvtVar = null;
        if (selected instanceof String) {
            inputEnvtVar = new InputEnvtVar(selected.toString());
        } else if (selected instanceof InputEnvtVar) {
            inputEnvtVar = (InputEnvtVar) selected;
        }
        this.getInputEnvtVars(); // make sure programs have been retrieved

        for (int i = 0; i < inputEnvtVars.length; i++) {
            if (inputEnvtVars[i].toString().equalsIgnoreCase(inputEnvtVar.getName().toString()))
                return inputEnvtVars[i]; // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addInputEnvtVar(inputEnvtVar);
    }

    public synchronized ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        if (parameterEnvtVars == null) {
            parameterEnvtVars = Arrays.asList(caseService.getParameterEnvVars());
            Collections.sort(parameterEnvtVars);
        }

        return parameterEnvtVars.toArray(new ParameterEnvVar[0]);
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        ParameterEnvVar newVar = caseService.addParameterEnvVar(envVar);

        // refresh the cache when a new one is added
        parameterEnvtVars = Arrays.asList(caseService.getParameterEnvVars());
        Collections.sort(parameterEnvtVars);

        return newVar;
    }

    public synchronized ParameterEnvVar getOrAddParameterEnvtVar(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ParameterEnvVar parameterEnvtVar = null;
        if (selected instanceof String) {
            parameterEnvtVar = new ParameterEnvVar(selected.toString());
        } else if (selected instanceof ParameterEnvVar) {
            parameterEnvtVar = (ParameterEnvVar) selected;
        }
        this.getParameterEnvVars(); // make sure parameterEnvtVar have been retrieved

        if (parameterEnvtVars.contains(parameterEnvtVar))
            return parameterEnvtVars.get(parameterEnvtVars.indexOf(parameterEnvtVar));

        // the parameterEnvtVar was not found in the list
        return addParameterEnvVar(parameterEnvtVar);
    }

    public synchronized ParameterName[] getParameterNames() throws EmfException {
        if (parameterNames == null) {
            parameterNames = Arrays.asList(caseService.getParameterNames());
            Collections.sort(parameterNames);
        }

        return parameterNames.toArray(new ParameterName[0]);
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        ParameterName newName = caseService.addParameterName(name);

        // refresh the cache when a new one is added
        parameterNames = Arrays.asList(caseService.getParameterNames());
        Collections.sort(parameterNames);

        return newName;
    }

    public synchronized ParameterName getOrAddParameterName(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ParameterName parameterName = null;
        if (selected instanceof String) {
            parameterName = new ParameterName(selected.toString());
        } else if (selected instanceof ParameterName) {
            parameterName = (ParameterName) selected;
        }
        this.getParameterNames(); // make sure parameterEnvtVar have been retrieved

        if (parameterNames.contains(parameterName))
            return parameterNames.get(parameterNames.indexOf(parameterName));

        // the parameterEnvtVar was not found in the list
        return addParameterName(parameterName);
    }

    public synchronized CaseProgram[] getPrograms() throws EmfException {
        if (programs == null) {
            List<CaseProgram> caseProgs = Arrays.asList(caseService.getPrograms());
            Collections.sort(caseProgs);
            programs = caseProgs.toArray(new CaseProgram[0]);
        }

        return programs;
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        CaseProgram newProgram = caseService.addProgram(program);

        List<CaseProgram> caseProgs = Arrays.asList(caseService.getPrograms());
        Collections.sort(caseProgs);
        programs = caseProgs.toArray(new CaseProgram[0]);

        return newProgram;
    }

    public synchronized CaseProgram getOrAddProgram(Object selected) throws EmfException {
        if (selected == null)
            return null;

        CaseProgram program = null;
        if (selected instanceof String) {
            program = new CaseProgram(selected.toString());
        } else if (selected instanceof CaseProgram) {
            program = (CaseProgram) selected;
        }
        this.getPrograms(); // make sure programs have been retrieved

        for (int i = 0; i < programs.length; i++) {
            if (programs[i].toString().equalsIgnoreCase(program.getName().toString()))
                return programs[i]; // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addProgram(program);
    }

    public synchronized Host[] getJobHosts() throws EmfException {
        if (hosts == null) {
            hosts = Arrays.asList(caseService.getHosts());
            Collections.sort(hosts);
        }

        return hosts.toArray(new Host[0]);
    }

    public synchronized Host addJobHost(Host host) throws EmfException {
        Host newHost = caseService.addHost(host);

        hosts = Arrays.asList(caseService.getHosts());
        Collections.sort(hosts);

        return newHost;
    }

    public synchronized Host getOrAddHost(Object selected) throws EmfException {
        if (selected == null)
            return null;

        Host host = null;

        if (selected instanceof String) {
            host = new Host(selected.toString());
        } else if (selected instanceof Host) {
            host = (Host) selected;
        }

        this.getPrograms(); // make sure hosts have been retrieved

        if (hosts.contains(host))
            return hosts.get(hosts.indexOf(host));

        return addJobHost(host);
    }

    public synchronized JobRunStatus[] getJobRunStatuses() throws EmfException {
        if (sortedStatuses != null)
            return sortedStatuses;

        JobRunStatus[] statuses = caseService.getJobRunStatuses();
        sortedStatuses = new JobRunStatus[statuses.length];

        for (int i = 0; i < statuses.length; i++) {
            String status = statuses[i].getName().toUpperCase();

            if (status.startsWith("NOT START"))
                sortedStatuses[0] = statuses[i];
            else if (status.startsWith("EXPORT"))
                sortedStatuses[1] = statuses[i];
            else if (status.startsWith("SUBMIT"))
                sortedStatuses[2] = statuses[i];
            else if (status.startsWith("RUN"))
                sortedStatuses[3] = statuses[i];
            else if (status.startsWith("COMPLET"))
                sortedStatuses[4] = statuses[i];
            else if (status.startsWith("QUALITY"))
                sortedStatuses[5] = statuses[i];
            else if (status.startsWith("FAIL"))
                sortedStatuses[6] = statuses[i];
        }

        return sortedStatuses;
    }

    public synchronized ValueType[] getParameterValueTypes() throws EmfException {
        if (parameterValueTypes == null) {
            parameterValueTypes = Arrays.asList(session.caseService().getValueTypes());
            Collections.sort(parameterValueTypes);
        }

        return parameterValueTypes.toArray(new ValueType[0]);
    }

    public synchronized CaseCategory[] getCaseCategories() throws EmfException {
        if (categoies == null) {
            categoies = Arrays.asList(caseService.getCaseCategories());
            Collections.sort(categoies);
        }

        return categoies.toArray(new CaseCategory[0]);
    }

    public synchronized CaseCategory addCaseCategory(CaseCategory cat) throws EmfException {
        CaseCategory newCateg = caseService.addCaseCategory(cat);

        // refresh the cache when a new one is added
        categoies = Arrays.asList(caseService.getCaseCategories());
        Collections.sort(categoies);

        return newCateg;
    }

    public synchronized CaseCategory getOrAddCaseCategory(Object selected) throws EmfException {
        if (selected == null)
            return null;

        CaseCategory category = null;
        if (selected instanceof String) {
            category = new CaseCategory(selected.toString());
        } else if (selected instanceof CaseCategory) {
            category = (CaseCategory) selected;
        }

        this.getCaseCategories(); // make sure category have been retrieved

        if (categoies.contains(category))
            return categoies.get(categoies.indexOf(category));

        // the category was not found in the list
        return addCaseCategory(category);
    }

    public synchronized Abbreviation[] getAbbreviations() throws EmfException {
        abbreviations = Arrays.asList(caseService.getAbbreviations());
        Collections.sort(abbreviations);

        return abbreviations.toArray(new Abbreviation[0]);
    }

    public synchronized Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException {
        Abbreviation newAbbr = caseService.addAbbreviation(abbr);

        // refresh the cache when a new one is added
        abbreviations = Arrays.asList(caseService.getAbbreviations());
        Collections.sort(abbreviations);

        return newAbbr;
    }

    public synchronized Abbreviation getOrAddAbbreviation(Object selected) throws EmfException {
        if (selected == null)
            return null;

        Abbreviation abbr = null;
        if (selected instanceof String) {
            abbr = new Abbreviation(selected.toString());
        } else if (selected instanceof Abbreviation) {
            abbr = (Abbreviation) selected;
        }

        this.getAbbreviations(); // make sure abbreviation have been retrieved

        if (abbreviations.contains(abbr))
            return abbreviations.get(abbreviations.indexOf(abbr));

        // the abbreviation was not found in the list
        return addAbbreviation(abbr);
    }

    public synchronized Project[] getProjects() throws EmfException {
        if (projects == null) {
            projects = Arrays.asList(dataCommonsService.getProjects());
            Collections.sort(projects);
        }

        return projects.toArray(new Project[0]);
    }

    public synchronized Project addProject(Project proj) throws EmfException {
        Project newProject = dataCommonsService.addProject(proj);

        projects = Arrays.asList(dataCommonsService.getProjects());
        Collections.sort(projects);

        return newProject;
    }

    public synchronized Project getOrAddProject(Object selected) throws EmfException {
        if (selected == null)
            return null;

        Project proj = null;
        if (selected instanceof String) {
            proj = new Project(selected.toString());
        } else if (selected instanceof Project) {
            proj = (Project) selected;
        }

        this.getProjects(); // make sure project have been retrieved

        if (projects.contains(proj))
            return projects.get(projects.indexOf(proj));

        // the project was not found in the list
        return addProject(proj);
    }

    public synchronized Region[] getRegions() throws EmfException {
        if (regions == null) {
            regions = Arrays.asList(dataCommonsService.getRegions());
            Collections.sort(regions);
        }

        return regions.toArray(new Region[0]);
    }

    public synchronized ModelToRun[] getModelToRuns() throws EmfException {
        if (modelToRuns == null) {
            modelToRuns = Arrays.asList(caseService.getModelToRuns());
            Collections.sort(modelToRuns);
        }

        return modelToRuns.toArray(new ModelToRun[0]);
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        ModelToRun newModel = caseService.addModelToRun(model);

        modelToRuns = Arrays.asList(caseService.getModelToRuns());
        Collections.sort(modelToRuns);

        return newModel;
    }

    public synchronized ModelToRun getOrAddModelToRun(Object selected) throws EmfException {
        if (selected == null)
            return null;

        ModelToRun model = null;
        if (selected instanceof String) {
            model = new ModelToRun(selected.toString());
        } else if (selected instanceof ModelToRun) {
            model = (ModelToRun) selected;
        }

        this.getModelToRuns(); // make sure models have been retrieved

        if (modelToRuns.contains(model))
            return modelToRuns.get(modelToRuns.indexOf(model));

        // the model was not found in the list
        return addModelToRun(model);
    }
}

package gov.epa.emissions.framework.client.casemanagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataCommonsService;
//import gov.epa.emissions.framework.services.data.DataService;
//import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.inputs.CaseJobNameComparator;

public class CaseObjectManager {
    
    private static CaseObjectManager com = null;
    
    private CaseProgram [] programs = null;
    
    private InputEnvtVar [] inputEnvtVars = null;
    
    private InputName [] inputNames = null;
    
    private SubDir [] subDirs = null;
    
    // TBD: Probably should put these into a DataObjectManager
    
    private Sector [] sectors = null;
    
    private Sector [] sectorsWithAll = null;
    
    private static Sector allSectors = null;
    
    private static CaseJob allJobsForSector = null;
    
    private int lastCaseId = -1;
    
    private CaseJob [] jobsForLastCaseId = null;
    
    private DatasetType [] datasetTypes = null;

    private DataCommonsService dataCommonsService = null;

    // up to here
    
    private EmfSession session = null; 
    
    private CaseService caseService = null;
    
    
    public static CaseObjectManager getCaseObjectManager(EmfSession aSession) {
        allSectors = new Sector("All sectors","All sectors");
        allJobsForSector = new CaseJob("All jobs for sector");
        
        if (com == null)
            com = new CaseObjectManager(aSession);
        return com;
    }
    
    private CaseObjectManager(EmfSession aSession)
    {
        this.session = aSession;
        this.caseService = session.caseService();
        
        this.dataCommonsService = session.dataCommonsService();
    }
    
    public synchronized void refresh() throws EmfException
    {
        // refresh the pieces of the cache that have been used so far
        if (programs != null) programs = caseService.getPrograms();
        if (inputEnvtVars != null) inputEnvtVars = caseService.getInputEnvtVars();
        if (inputNames != null) inputNames = caseService.getInputNames();
        if (subDirs != null) subDirs = caseService.getSubDirs();
        
        if (sectors != null) sectors = dataCommonsService.getSectors();
        if (datasetTypes != null) datasetTypes = dataCommonsService.getDatasetTypes();
        
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
        if (sectorsWithAll == null)
        {
           List list = new ArrayList();
           list.add(allSectors);
           list.addAll(Arrays.asList(getSectors()));
           sectorsWithAll = (Sector[]) list.toArray(new Sector[0]);
        }
        return sectorsWithAll;
    }

    public synchronized SubDir [] getSubDirs() throws EmfException
    {
        if (subDirs == null)
            subDirs = caseService.getSubDirs();
        
        return subDirs;
    }
     
    public synchronized CaseJob[] getCaseJobsWithAll(int caseId) throws EmfException 
    {
        if (this.lastCaseId == caseId)  // if the same as the last case, 
        {
            return this.jobsForLastCaseId;
        }
        // otherwise, get a new list
        List<CaseJob> jobs = new ArrayList<CaseJob>();
        jobs.add(allJobsForSector);
        jobs.addAll(Arrays.asList(caseService.getCaseJobs(caseId)));
        //sort the Case Jobs before sending them to the ComboBox
        jobsForLastCaseId = jobs.toArray(new CaseJob[jobs.size()]);
        Arrays.sort(jobsForLastCaseId, new CaseJobNameComparator());
        lastCaseId = caseId;
        return jobsForLastCaseId;       
    }
    
    public synchronized SubDir addSubDir(SubDir subDir) throws EmfException
    {
        SubDir newVar = caseService.addSubDir(subDir);
        // refresh the cache when a new one is added
        subDirs = caseService.getSubDirs();

        return newVar;
    }
    
    public synchronized SubDir getOrAddSubDir(Object selected) throws EmfException
    {
        if (selected == null)
            return null;
        
        SubDir subDir = null;
        if (selected instanceof String) {
            subDir = new SubDir(selected.toString());
        }
        else if (selected instanceof SubDir)
        {
            subDir = (SubDir)selected;
        }
        this.getSubDirs();  // make sure programs have been retrieved
        
        for (int i = 0; i < subDirs.length; i++)
        {
           if (subDirs[i].toString().equalsIgnoreCase(subDir.getName().toString()))
                return subDirs[i];  // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addSubDir(subDir);
    }
    
    @SuppressWarnings("unchecked")
    public synchronized InputName [] getInputNames() throws EmfException
    {
        if (inputNames == null)
            inputNames = caseService.getInputNames();
        
        List<InputName> inputs = Arrays.asList(inputNames);
        Collections.sort(inputs);
        
        return inputs.toArray(new InputName[0]);
    }
        
    public synchronized InputName addInputName(InputName inputName) throws EmfException
    {
        InputName newVar = caseService.addCaseInputName(inputName);
        // refresh the cache when a new one is added
        inputNames = caseService.getInputNames();
        List<InputName> inputs = Arrays.asList(inputNames);
        Collections.sort(inputs);

        return newVar;
    }
    
    public synchronized InputName getOrAddInputName(Object selected) throws EmfException
    {
        if (selected == null)
            return null;
        
        InputName inputName = null;
        if (selected instanceof String) {
            inputName = new InputName(selected.toString());
        }
        else if (selected instanceof InputName)
        {
            inputName = (InputName)selected;
        }
        this.getInputNames();  // make sure programs have been retrieved
        
        for (int i = 0; i < inputNames.length; i++)
        {
           if (inputNames[i].toString().equalsIgnoreCase(inputName.getName().toString()))
                return inputNames[i];  // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addInputName(inputName);
    }
    
    public synchronized InputEnvtVar [] getInputEnvtVars() throws EmfException
    {
        if (inputEnvtVars == null)
            inputEnvtVars = caseService.getInputEnvtVars();
        
        List<InputEnvtVar> inputVars = Arrays.asList(inputEnvtVars);
        Collections.sort(inputVars);
        
        return inputVars.toArray(new InputEnvtVar[0]);
    }
        
    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException
    {
        InputEnvtVar newVar = caseService.addInputEnvtVar(inputEnvtVar);
        // refresh the cache when a new one is added
        inputEnvtVars = caseService.getInputEnvtVars();
        List<InputEnvtVar> inputVars = Arrays.asList(inputEnvtVars);
        Collections.sort(inputVars);

        return newVar;
    }
    
    public synchronized InputEnvtVar getOrAddInputEnvtVar(Object selected) throws EmfException
    {
        if (selected == null)
            return null;
        
        InputEnvtVar inputEnvtVar = null;
        if (selected instanceof String) {
            inputEnvtVar = new InputEnvtVar(selected.toString());
        }
        else if (selected instanceof InputEnvtVar)
        {
            inputEnvtVar = (InputEnvtVar)selected;
        }
        this.getInputEnvtVars();  // make sure programs have been retrieved
        
        for (int i = 0; i < inputEnvtVars.length; i++)
        {
           if (inputEnvtVars[i].toString().equalsIgnoreCase(inputEnvtVar.getName().toString()))
                return inputEnvtVars[i];  // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addInputEnvtVar(inputEnvtVar);
    }
    
    public synchronized CaseProgram [] getPrograms() throws EmfException
    {
        if (programs == null)
          programs = caseService.getPrograms();
        
        List<CaseProgram> caseProgs = Arrays.asList(programs);
        Collections.sort(caseProgs);
        
        return caseProgs.toArray(new CaseProgram[0]);
    }
        
    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException
    {
        CaseProgram newProgram = caseService.addProgram(program);
        programs = caseService.getPrograms();
        List<CaseProgram> caseProgs = Arrays.asList(programs);
        Collections.sort(caseProgs);
        
        return newProgram;
    }
    
    public synchronized CaseProgram getOrAddProgram(Object selected) throws EmfException
    {
        if (selected == null)
            return null;
        
        CaseProgram program = null;
        if (selected instanceof String) {
            program = new CaseProgram(selected.toString());
        }
        else if (selected instanceof CaseProgram)
        {
            program = (CaseProgram)selected;
        }
        this.getPrograms();  // make sure programs have been retrieved
        
        for (int i = 0; i < programs.length; i++)
        {
           if (programs[i].toString().equalsIgnoreCase(program.getName().toString()))
                return programs[i];  // the program already exists (case insensitive check)
        }
        // the program was not found in the list
        return addProgram(program);
    }
}

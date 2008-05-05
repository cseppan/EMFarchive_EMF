package gov.epa.emissions.framework.client.casemanagement.inputs;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;

import javax.swing.JComponent;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
//import gov.epa.emissions.framework.client.casemanagement.SubDirs;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
//import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
//import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class InputFieldsPanelPresenter {

    private EmfSession session;

    private InputFieldsPanelView view;

    private int caseId;
    
    public static final String ALL_FOR_SECTOR = "All jobs for sector";
    
    private CaseObjectManager caseObjectManager = null;
    
    public InputFieldsPanelPresenter(int caseId, InputFieldsPanelView inputFields, EmfSession session) //throws EmfException 
    {
        this.session = session;
        this.view = inputFields;
        this.caseId = caseId;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseInput input, JComponent container) throws EmfException {
        view.observe(this);
        view.display(input, container, session);
    }

//    public CaseInputNames getCaseInputNames() throws EmfException {
//        System.out.println("InputFieldsPanelPresenter: Get case input names");
//        if (caseInputNames == null)
//            this.caseInputNames = new CaseInputNames(session,getInputNames());
//        return caseInputNames;
//    }
//
//    public CaseInputEnvtVars getCaseInputEnvtVars() throws EmfException {
//        System.out.println("InputFieldsPanelPresenter: Get envt vars");
//        if (caseInputEnvtVars == null)
//            this.caseInputEnvtVars = new CaseInputEnvtVars(session, getEnvtVars());
//        return this.caseInputEnvtVars;
//    }

//    public Programs getCasePrograms() throws EmfException {
//        System.out.println("InputFieldsPanelPresenter: Get programs");
//        if (programs == null)
//            this.programs = new Programs(session, getPrograms());
//        return this.programs;
//    }
    
//    public SubDirs getSubDirs() throws EmfException {
//        System.out.println("InputFieldsPanelPresenter: Get sub dirs");
//        if (subdirs == null)
//            this.subdirs = new SubDirs(session, getSubdirs());
//        return this.subdirs;
//    }

    public InputName[] getInputNames() throws EmfException {
        return caseObjectManager.getInputNames();
    }

    public Sector[] getSectors() throws EmfException {
        return caseObjectManager.getSectorsWithAll();
    }

    public CaseProgram[] getPrograms() throws EmfException {
         return caseObjectManager.getPrograms();
    }

    public SubDir[] getSubdirs() throws EmfException {
        return caseObjectManager.getSubDirs();
    }

    public InputEnvtVar[] getEnvtVars() throws EmfException {
        return caseObjectManager.getInputEnvtVars();
    }

    public CaseJob[] getCaseJobs() throws EmfException 
    {
        return caseObjectManager.getCaseJobsWithAll(caseId);
   }
    
    public DatasetType[] getDSTypes() throws EmfException {
       return caseObjectManager.getDatasetTypes();
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
{
        if (type == null)
            return new EmfDataset[0];

        return dataService().getDatasets(type);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return dataEditorService().getVersions(dataset.getId());
    }

    private DataService dataService() {
        return session.dataService();
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    public void doSave() throws EmfException {
         session.caseService().updateCaseInput(session.user(), view.setFields());
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public InputName getInputName(Object selected) throws EmfException {
        return caseObjectManager.getOrAddInputName(selected);
    }

    public InputEnvtVar getInputEnvtVar(Object selected) throws EmfException {
        return caseObjectManager.getOrAddInputEnvtVar(selected);
    }

    public CaseProgram getCaseProgram(Object selected) throws EmfException {
        return caseObjectManager.getOrAddProgram(selected);
    }

    public SubDir getSubDir(Object selected) throws EmfException {
        return caseObjectManager.getOrAddSubDir(selected);
    }

    public int getJobIndex(int caseJobId, CaseJob [] jobs) //throws EmfException 
    {
        //CaseJob[] jobs = session.caseService().getCaseJobs(caseId);
        // AME: don't go get the jobs from the server again!
        // QH: This will use the cached list of jobs and won't get the newest list of jobs for the session.
        
        if (caseJobId == 0) return 0;
        
        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].getId() == caseJobId)
                return i; // because of the default "All jobs" job is not in db
        
        return 0;
    }
    
    public String getJobName(int jobId) throws EmfException {
        if (jobId == 0)
            return "All jobs for sector";
        
        CaseJob job = session.caseService().getCaseJob(jobId);
        if (job == null)
            throw new EmfException("Cannot retrieve job (id = " + jobId + ").");
        return job.getName();
    }
    
}

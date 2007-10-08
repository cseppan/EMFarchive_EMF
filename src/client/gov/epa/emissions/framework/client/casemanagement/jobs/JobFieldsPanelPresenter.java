package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;

import javax.swing.JComponent;

public class JobFieldsPanelPresenter {

    private EmfSession session;

    private JobFieldsPanelView view;

    private EditJobsTabPresenter parentPresenter;

    private Case caseObj;
    
    private CaseObjectManager caseObjectManager = null;

    public JobFieldsPanelPresenter(JobFieldsPanelView jobFields, EmfSession session,
            EditJobsTabPresenter parentPresenter, Case caseObj) {
        this.session = session;
        this.view = jobFields;
        this.parentPresenter = parentPresenter;
        this.caseObj = caseObj;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseJob job, JComponent container) throws EmfException {
        view.observe(this);
        view.display(caseObj, job, container);
    }

    public synchronized Sector[] getSectors() throws EmfException {
        return caseObjectManager.getSectorsWithAll();
    }

    public synchronized Host[] getHosts() throws EmfException {
        return caseObjectManager.getJobHosts();
    }

    public synchronized Host getHost(Object selected) throws EmfException {
        return caseObjectManager.getOrAddHost(selected);
    }
    
    public synchronized JobRunStatus[] getRunStatuses() throws EmfException {
        return caseObjectManager.getJobRunStatuses();
    }

    private CaseService caseService() {
        return session.caseService();
    }

    public void doSave() throws EmfException {
        caseService().updateCaseJob(session.user(), view.setFields());
    }

    public boolean checkDuplication(CaseJob job) throws EmfException {
        CaseJob[] existedJobs = parentPresenter.getCaseJobs();
        return contains(job, existedJobs);
    }

    private boolean contains(CaseJob job, CaseJob[] existedJobs) {
        String newArgs = job.getArgs();
        Sector newSector = job.getSector();
        Executable newExec = job.getExecutable();

        for (int i = 0; i < existedJobs.length; i++) {
            String existedArgs = existedJobs[i].getArgs();
            Sector existedSector = existedJobs[i].getSector();
            Executable existedExec = existedJobs[i].getExecutable();

            if (job.getId() != existedJobs[i].getId()
                    && job.getVersion() == existedJobs[i].getVersion()
                    && ((newArgs == null && existedArgs == null) || (newArgs != null && newArgs
                            .equalsIgnoreCase(existedArgs)))
                    && ((newExec == null && existedExec == null) || (newExec != null) && newExec.equals(existedExec))
                    && ((newSector == null && existedSector == null) || (newSector != null && newSector
                            .equals(existedSector)))) {
                return true;
            }
        }

        return false;
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

    public String[] getExistedJobs() throws EmfException {
        CaseJob[] existedJobs = parentPresenter.getCaseJobs();
        String[] names = new String[existedJobs.length];
        
        for (int i = 0; i < existedJobs.length; i++)
            names[i] = existedJobs[i].getName();
        
        return names;
    }
    
    public String[] getAllValidJobs(int jobId) throws EmfException {
        return caseService().getAllValidJobs(jobId);
    }

    public String[] getDependentJobs(int jobId) throws EmfException {
        return caseService().getDependentJobs(jobId);
    }

    public DependentJob[] dependentJobs(String[] jobNames) throws EmfException {
        int[] jobIds = caseService().getJobIds(caseObj.getId(), jobNames);
        DependentJob[] dependentJobs = new DependentJob[jobIds.length];
        
        for (int i = 0; i < jobIds.length; i++)
            dependentJobs[i] = new DependentJob(jobIds[i]);
        
        return dependentJobs;
    }

}

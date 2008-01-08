package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;

public class EditJobsTabPresenterImpl implements EditJobsTabPresenter {

    private Case caseObj;

    private EditJobsTabView view;

    private EmfSession session;

    public EditJobsTabPresenterImpl(EmfSession session, EditJobsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        String caseOutputDir = view.getCaseOutputFileDir();
        if (caseOutputDir != null)
            caseObj.setOutputFileDir(caseOutputDir);
        view.refresh();
    }

    public void addNewJobDialog(NewJobView dialog) {
        dialog.register(this);
        dialog.display();
    }

    public CaseJob addNewJob(CaseJob job) throws EmfException {
        job.setCaseId(caseObj.getId());
        CaseJob newJob = service().addCaseJob(job);
        view.addJob(newJob);
        refreshView();

        return newJob;
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
        // view.notifychanges();
    }

    public boolean jobsUsed(CaseJob[] jobs) throws EmfException {
        if (jobs.length == 0)
            return false;

        int caseId = jobs[0].getCaseId();
        CaseInput[] inputs = service().getCaseInputs(caseId);
        CaseParameter[] params = service().getCaseParameters(caseId);

        for (int i = 0; i < jobs.length; i++) {
            for (int j = 0; j < inputs.length; j++)
                if (inputs[j].getCaseJobID() == jobs[i].getId())
                    return true;

            for (int k = 0; k < params.length; k++)
                if (params[k].getJobId() == jobs[i].getId())
                    return true;
        }

        return false;
    }

    public void removeJobs(CaseJob[] jobs) throws EmfException {
        service().removeCaseJobs(jobs);
    }

    public void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor, view, this, session);
        presenter.display(job);
    }

    public void copyJob(CaseJob job, EditCaseJobView jobEditor) throws Exception {
        CaseJob newJob = (CaseJob) DeepCopy.copy(job);
        newJob.setName(getUniqueNewName("Copy of " + job.getName()));
        newJob.setJobkey(null); // jobkey supposedly generated when it is run
        newJob.setRunstatus(null);
        newJob.setRunLog(null);
        newJob.setRunStartDate(null);
        newJob.setRunCompletionDate(null);

        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor, view, this, session);
        presenter.display(addNewJob(newJob));
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public void addJobFields(CaseJob job, JComponent container, JobFieldsPanel jobFieldsPanel) throws EmfException {
        JobFieldsPanelPresenter jobFieldsPresenter = new JobFieldsPanelPresenter(jobFieldsPanel, session, this, caseObj);
        jobFieldsPresenter.display(job, container);
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public void runJobs(CaseJob[] jobs) throws EmfException {
        Integer[] jobIds = new Integer[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i].getExecutable() == null)
                throw new EmfException("Job " + jobs[i].getName() + " doesn't have a valid executable file.");
            jobIds[i] = new Integer(jobs[i].getId());
        }

        service().runJobs(jobIds, caseObj.getId(), session.user());
    }

    public String getJobsStatus(CaseJob[] jobs) throws EmfException {
        List<String> ok = new ArrayList<String>();
        List<String> cancel = new ArrayList<String>();
        List<String> warning = new ArrayList<String>();

        for (int i = 0; i < jobs.length; i++) {
            System.out.println("Getting status of jobs from server");
            String status = service().getCaseJob(jobs[i].getId()).getRunstatus().getName();

            if (status == null || status.trim().isEmpty())
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Not Started"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Quality Assured"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Completed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Failed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Running"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Submitted"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Exporting"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Waiting"))
                cancel.add(status);
        }

        if (ok.size() == jobs.length)
            return "OK";

        if (cancel.size() > 0)
            return "CANCEL";

        return "WARNING";
    }

    private String getUniqueNewName(String name) throws Exception {
        List<String> names = new ArrayList<String>();

        List<CaseJob> allJobs = Arrays.asList(view.caseJobs());

        for (Iterator<CaseJob> iter = allJobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            if (job.getName().startsWith(name)) {
                names.add(job.getName());
            }
        }

        if (names.size() == 0)
            return name;

        return name + " " + getSequence(name, names);
    }

    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            return Math.abs(new Random().nextInt());
        }
    }

    public String validateJobs(CaseJob[] jobs) throws EmfException {
        List<Integer> ids = new ArrayList<Integer>();
        
        for (CaseJob job : jobs)
            ids.add(new Integer(job.getId()));
        
        System.out.println("Validating input datasets of jobs");
        return service().validateJobs(ids.toArray(new Integer[0]));
    }
    
    public String validateInputDatasets(CaseJob[] jobs) throws EmfException {
        List<Integer> ids = new ArrayList<Integer>();
        
        for (CaseJob job : jobs)
            ids.add(new Integer(job.getId()));
        
        return service().validateInputDatasets(ids.toArray(new Integer[0]));
    }
}

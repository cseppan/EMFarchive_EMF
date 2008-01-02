package gov.epa.emissions.framework.client.casemanagement.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public class ShowHistoryTabPresenter {

    private Case caseObj;

    private ShowHistoryTabView view;

    private EmfSession session;

    public ShowHistoryTabPresenter(EmfSession session, ShowHistoryTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj.getId(), this);
    }

    public void doSave() {
        //doesn't need to save anything
    }

    private CaseService service() {
        return session.caseService();
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
    
    public List<CaseJob> getCaseJobs() throws EmfException {
        CaseJob[] caseJobs=service().getCaseJobs(caseObj.getId());
        List<CaseJob> jobs= new ArrayList<CaseJob>();
        jobs.addAll(Arrays.asList(caseJobs));
        Collections.sort(jobs);
        return jobs; 
 //       return service().getCaseJobs(caseObj.getId());
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        return service().getJobMessages(caseId, jobId);
    }

    public void doRemove(JobMessage[] msgs) throws EmfException{
        service().removeMessages(session.user(), msgs);
        
    }

}

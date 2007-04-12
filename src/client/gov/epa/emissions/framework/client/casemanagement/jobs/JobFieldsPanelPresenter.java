package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public class JobFieldsPanelPresenter {

    private EmfSession session;

    private JobFieldsPanelView view;

    public JobFieldsPanelPresenter(JobFieldsPanelView jobFields, EmfSession session) {
        this.session = session;
        this.view = jobFields;
    }

    public void display(CaseJob job, JComponent container) throws EmfException {
        view.observe(this);
        view.display(job, container);
    }

    public Sector[] getSectors() throws EmfException {
        List<Sector> list = new ArrayList<Sector>();
        list.add(new Sector("All sectors", "All sectors"));
        list.addAll(Arrays.asList(dataCommonsService().getSectors()));

        return list.toArray(new Sector[0]);
    }
    
    public Host[] getHosts() throws EmfException {
        List<Host> list = new ArrayList<Host>();
        list.addAll(Arrays.asList(caseService().getHosts()));
        
        return list.toArray(new Host[0]);
    }
    
    public JobRunStatus[] getRunStatuses() throws EmfException {
        JobRunStatus[] statuses = caseService().getJobRunStatuses();
        JobRunStatus[] sorted = new JobRunStatus[statuses.length];
       
        for (int i = 0; i < statuses.length; i++) {
            String status = statuses[i].getName().toUpperCase();
            
            if (status.startsWith("NOT"))
                sorted[0] = statuses[i];
            else if (status.startsWith("SUBMIT"))
                sorted[1] = statuses[i];
            else if (status.startsWith("RUN"))
                sorted[2] = statuses[i];
            else if (status.startsWith("SUCCEED"))
                sorted[3] = statuses[i];
            else if (status.startsWith("QUALITY"))
                sorted[4] = statuses[i];
            else if (status.startsWith("FAIL"))
                sorted[5] = statuses[i];
        }
        
        return sorted;
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    private CaseService caseService() {
        return session.caseService();
    }
    
    public void doSave() throws EmfException {
        //view.setFields();
        caseService().updateCaseJob(view.setFields());
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

}

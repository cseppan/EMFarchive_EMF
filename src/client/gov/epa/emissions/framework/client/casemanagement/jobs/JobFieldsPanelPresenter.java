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

    private Hosts hosts;

    private EditJobsTabPresenter parentPresenter;

    public JobFieldsPanelPresenter(JobFieldsPanelView jobFields, EmfSession session,
            EditJobsTabPresenter parentPresenter) throws EmfException {
        this.session = session;
        this.view = jobFields;
        this.hosts = new Hosts(session, getHosts());
        this.parentPresenter = parentPresenter;
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

    private Host[] getHosts() throws EmfException {
        List<Host> list = new ArrayList<Host>();
        list.addAll(Arrays.asList(caseService().getHosts()));

        return list.toArray(new Host[0]);
    }

    public Hosts getHostsObject() {
        return this.hosts;
    }

    public Host getHost(Object host) throws EmfException {
        return hosts.get(host);
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

    public void doSave(CaseJob job) throws EmfException {
        view.validateFields();
        view.setFields();
        // caseService().updateCaseJob(view.setFields());
    }

    public boolean checkDuplication(CaseJob job) throws EmfException {
        CaseJob[] existedJobs = parentPresenter.getCaseJobs();
        return contains(job, existedJobs);
    }

    private boolean contains(CaseJob job, CaseJob[] existedJobs) {
        String newArgs = job.getArgs();
        Sector newSector = job.getSector();

        for (int i = 0; i < existedJobs.length; i++) {
            String existedArgs = existedJobs[i].getArgs();
            Sector existedSector = existedJobs[i].getSector();

            if (job.getId() != existedJobs[i].getId()
                    && job.getVersion() == existedJobs[i].getVersion()
                    && ((newArgs == null && existedArgs == null) || (newArgs != null && newArgs
                            .equalsIgnoreCase(existedArgs)))
                    && job.getExecutable().equals(existedJobs[i].getExecutable())
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

}

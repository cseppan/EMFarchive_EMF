package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

public class JobFieldsPanelPresenter {

    private EmfSession session;

    private JobFieldsPanelView view;

    public JobFieldsPanelPresenter(JobFieldsPanelView inputFields, EmfSession session) {
        this.session = session;
        this.view = inputFields;
    }

    public void display(CaseJob job, JComponent container) throws EmfException {
        view.observe(this);
        view.display(job, container);
    }

    public Sector[] getSectors() throws EmfException {
        List list = new ArrayList();
        list.add(new Sector("All sectors", "All sectors"));
        list.addAll(Arrays.asList(dataCommonsService().getSectors()));

        return (Sector[]) list.toArray(new Sector[0]);
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    public void doSave() throws EmfException {
        //view.setFields();
        session.caseService().updateCaseJob(view.setFields());
    }

    public void doValidateFields() throws EmfException {
        view.setFields(); // FIXME: should do more check here
    }

}

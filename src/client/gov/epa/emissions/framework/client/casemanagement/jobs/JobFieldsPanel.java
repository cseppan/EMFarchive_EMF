package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.MessagePanel;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class JobFieldsPanel extends JPanel implements JobFieldsPanelView {

    private JobFieldsPanelPresenter presenter;

    private CaseJob job;

    public JobFieldsPanel(MessagePanel messagePanel, ManageChangeables changeablesList) {
//
    }

    public void display(CaseJob job, JComponent container) {
        this.job = job;
        JPanel panel = new JPanel(new SpringLayout());
        container.add(panel);
    }

    public CaseJob setFields() {
        return new CaseJob();
    }

    public void observe(JobFieldsPanelPresenter presenter) {
        this.presenter = presenter;
    }

    public void validateFields() {
        setFields();
    }

    public CaseJob getJob() throws EmfException {
        presenter.doValidateFields();
        return this.job;
    }

}

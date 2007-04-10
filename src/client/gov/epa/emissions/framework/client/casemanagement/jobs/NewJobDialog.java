package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewJobDialog extends Dialog implements NewJobView, ManageChangeables {

    protected boolean shouldCreate;

    protected EditJobsTabPresenterImpl presenter;

    private MessagePanel messagePanel;

//    private JobFieldsPanel jobFieldsPanel;
    
    public NewJobDialog(EmfConsole parent) {
        super("Create new CaseJob", parent);
        super.setSize(new Dimension(550, 520));
        super.center();
    }

    public void display(int caseId) {
        doDisplay();
    }

    private void doDisplay() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addNewJob();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void addNewJob() throws EmfException {
        //doValidateFields();
        shouldCreate = true;
        presenter.addNewJob(job());
        close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public CaseJob job() {
        //jobFieldsPanel.setFields();

        CaseJob newJob = new CaseJob();
        newJob.setName("a new job");
        return newJob;
    }

    public void register(Object presenter) {
        this.presenter = (EditJobsTabPresenterImpl) presenter;
    }
    
//    private void doValidateFields() {
//        jobFieldsPanel.validateFields();
//    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }



}

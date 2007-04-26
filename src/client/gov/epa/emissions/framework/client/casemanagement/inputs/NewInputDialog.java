package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewInputDialog extends Dialog implements NewInputView, ManageChangeables {

    protected boolean shouldCreate;

    protected EditInputsTabPresenterImpl presenter;

    private MessagePanel messagePanel;

    private InputFieldsPanel inputFieldsPanel;
    
    public NewInputDialog(EmfConsole parent) {
        super("Add input to case", parent);
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
        this.inputFieldsPanel = new InputFieldsPanel(messagePanel, this);

        try {
            presenter.doAddInputFields(panel, inputFieldsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addNewInput();
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

    private void addNewInput() throws EmfException {
        doValidateFields();
        shouldCreate = true;
        presenter.addNewInput(input());
        close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public CaseInput input() {
        try {
            inputFieldsPanel.setFields();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return inputFieldsPanel.getInput();
    }

    public void register(Object presenter) {
        this.presenter = (EditInputsTabPresenterImpl) presenter;
    }
    
    private void doValidateFields() throws EmfException {
        inputFieldsPanel.validateFields();
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

}

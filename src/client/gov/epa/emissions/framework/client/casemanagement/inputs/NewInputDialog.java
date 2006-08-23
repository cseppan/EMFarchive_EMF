package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
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
        super("Create new Input", parent);
        super.setSize(new Dimension(550, 520));
        super.center();
    }

    public void display(Case caseObj) {
        doDisplay(caseObj);
    }

    private void doDisplay(Case caseObj) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel(caseObj.getCaseInputs()));

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

    private JPanel buttonsPanel(final CaseInput[] inputs) {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doNew(inputs);
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

    private void doNew(CaseInput[] inputs) throws EmfException {
        doValidateFields();
        doCheckDuplicate(inputs);
        shouldCreate = true;
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
    
    private void doCheckDuplicate(CaseInput[] existingInputs) throws EmfException {
        presenter.doCheckDuplicate(input(), existingInputs);
    }

    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

}

package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditInputWindow extends DisposableInteralFrame implements EditInputView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditInputPresenterImpl presenter;

    private MessagePanel messagePanel;

    private Button ok;

    private InputFieldsPanel inputFieldsPanel;
    
    private CaseInput input;
    
    public EditInputWindow(String title, DesktopManager desktopManager) {
        super("Edit Case Input", new Dimension(550, 520), desktopManager);
        super.setLabel(super.getTitle() + ": " + title);
    }

    public void display(CaseInput input) throws EmfException {
        this.input = input;
        layout = createLayout(input);
        
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }

    private JPanel createLayout(CaseInput input) throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.inputFieldsPanel = new InputFieldsPanel(messagePanel, this);
        presenter.doAddInputFields(panel, inputFieldsPanel);
        panel.add(buttonsPanel(input));

        return panel;
    }
    
    private JPanel buttonsPanel(final CaseInput input) {
        JPanel panel = new JPanel();

        ok = new SaveButton("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSave(input);
                disposeView();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CloseButton("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doSave(CaseInput input) {
        clearMessage();
        try {
            doValidateFields();
            doCheckDuplicate();
            presenter.doSave();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private void doValidateFields() throws EmfException {
        inputFieldsPanel.validateFields();
    }
    
    private void doCheckDuplicate() throws EmfException {
        presenter.doCheckDuplicate(input);
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditInputPresenterImpl presenter) {
        this.presenter = presenter;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            super.disposeView();
    }
    
    public void loadInput() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction...");
    }

    public void populateFields() {
        // NOTE Auto-generated method stub
        
    }

}

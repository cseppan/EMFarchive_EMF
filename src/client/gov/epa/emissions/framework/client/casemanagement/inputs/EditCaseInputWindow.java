package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
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

public class EditCaseInputWindow extends DisposableInteralFrame implements EditCaseInputView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditCaseInputPresenterImpl presenter;

    private MessagePanel messagePanel;

    private Button ok;

    private InputFieldsPanel inputFieldsPanel;
    
    private CaseInput input;
    
//    private EditInputsTabView parentView;
    
    public EditCaseInputWindow(String title, EditInputsTabView parentView, DesktopManager desktopManager) {
        super("Edit Case Input", new Dimension(550, 520), desktopManager);
        super.setLabel(super.getTitle() + ": " + title);
//        this.parentView = parentView;
    }

    public void display(CaseInput input) throws EmfException {
        this.input = input;
        layout = createLayout();
        
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.inputFieldsPanel = new InputFieldsPanel(messagePanel, this);
        presenter.doAddInputFields(panel, inputFieldsPanel);
        panel.add(buttonsPanel());

        return panel;
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doSave() {
        clearMessage();
        try {
            doValidateFields();
            doCheckDuplicate();
            presenter.doSave();
//            if (hasChanges())
//                parentView.notifychanges();
            disposeView();
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

    public void observe(EditCaseInputPresenterImpl presenter) {
        this.presenter = presenter;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            super.disposeView();
    }
    
    public void loadInput() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("");
    }

    public void populateFields() {
        // NOTE Auto-generated method stub
        
    }
    
    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

}

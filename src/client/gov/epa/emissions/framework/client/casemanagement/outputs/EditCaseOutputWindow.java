package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditCaseOutputWindow extends DisposableInteralFrame implements EditCaseOutputView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditCaseOutputPresenterImpl presenter;

    private MessagePanel messagePanel;

    private Button save;
    
    private OutputFieldsPanel outputFieldsPanel; 

    public EditCaseOutputWindow(String title, DesktopManager desktopManager) {
        super("Edit Case Output", new Dimension(610, 420), desktopManager);
        super.setTitle(super.getTitle() + ": " + title);
    }
    

    public void display(CaseOutput output) throws EmfException {
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
        this.outputFieldsPanel=new OutputFieldsPanel(messagePanel, this);
        presenter.addOutputFields(panel, outputFieldsPanel);
        panel.add(buttonsPanel());

        return panel;
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });
        getRootPane().setDefaultButton(save);
        panel.add(save);

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
            //doCheckDuplicate();
            presenter.doSave();
//            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }
        disposeView();
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

    public void observe(EditCaseOutputPresenterImpl presenter) {
        this.presenter = presenter;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            super.disposeView();
    }
    
    public void populateFields() {
        // NOTE Auto-generated method stub
        
    }
    
    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void loadOutput() {
        // NOTE Auto-generated method stub
        
    }

    public CaseOutput setFields() {
        // NOTE Auto-generated method stub
        return null;
    }
    private void doValidateFields() throws EmfException {
        outputFieldsPanel.validateFields();
    }
    
    public void viewOnly(String title){
        super.setTitle("View Case Output: " + title);
        save.setVisible(false);
        outputFieldsPanel.viewOnly();
    }
}

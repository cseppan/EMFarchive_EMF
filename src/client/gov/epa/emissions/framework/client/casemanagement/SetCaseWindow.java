package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.casemanagement.inputs.SetInputFieldsPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class SetCaseWindow extends DisposableInteralFrame implements SetCaseView {

    private JPanel layout;
    
    private JPanel mainPanel;
    
    private EmfConsole parentConsole;

    private SetCasePresenter presenter;
    
    private CaseManagerPresenter managerPresenter;

    private MessagePanel messagePanel;
    
    private TextField envValue;
    
    private TextArea purpose;

    private List<SetCaseObject> setCaseObjects; 
   
    private Case caseObj;

    private Button prevButton, nexButton, editButton;

    private SetCaseObject  currentObject; 
    
    private int currentIndex=0; 
    
    private SetInputFieldsPanel setInputFieldsPanel;
    
    public SetCaseWindow(String title, EmfConsole parentConsole, 
            DesktopManager desktopManager) {
        super(title, new Dimension(520, 370), desktopManager);
        this.parentConsole = parentConsole;
    }

    public void display(Case caseObj) throws EmfException {
        this.caseObj = caseObj;
        layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }
    
    public void observe(SetCasePresenter presenter, CaseManagerPresenter managerPresenter) {
        this.presenter =presenter; 
        this.managerPresenter = managerPresenter;

    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        
        setupObjects(); 
        panel.add(mainPanel());
        panel.add(buttonsPanel());

        return panel;
    }
    
    private void setupObjects() throws EmfException {
        // add parameters to setCaseObjects
        setCaseObjects = new ArrayList<SetCaseObject>();
        CaseInput[] inputList = presenter.getCaseInput(caseObj.getId(), new Sector("All", "All"), false);
        for (CaseInput input :inputList){
            SetCaseObject obj = new SetCaseObject(input, true);
            setCaseObjects.add(obj);
        }
        CaseParameter[] paraList = presenter.getCaseParameters(caseObj.getId(), new Sector("All", "All"), false);
        for (CaseParameter par :paraList){
            SetCaseObject obj = new SetCaseObject(par, false);
            setCaseObjects.add(obj);
        }
        if (setCaseObjects.size()==0)
            throw new EmfException("No input or parameter to edit");  
    }

    
    private JPanel mainPanel() throws EmfException{
        mainPanel = new JPanel(new BorderLayout());
        //get first setCaseObjects
        currentObject = setCaseObjects.get(currentIndex);
        if (!currentObject.isInput())
            mainPanel.add(displayParam((CaseParameter)currentObject.getObject()));
        else
            mainPanel.add(displayInput((CaseInput)currentObject.getObject()));
        return mainPanel;
    }
    
    private JPanel displayInput(CaseInput input) throws EmfException {
        JPanel panel = new JPanel();
        this.setInputFieldsPanel = new SetInputFieldsPanel(messagePanel, this, presenter.getSession(), parentConsole);
        presenter.doAddInputFields(input, panel, setInputFieldsPanel);
        return panel;
    }

    
    private void panelRefresh() throws EmfException {
        mainPanel.removeAll();
        if (!currentObject.isInput())
            mainPanel.add(displayParam((CaseParameter)currentObject.getObject()));
        else
            mainPanel.add(displayInput((CaseInput)currentObject.getObject()));
        super.validate();
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(100);
        layout.setVgap(10);
        container.setLayout(layout);
        
        prevButton = new Button("Prev", prevsAction());
        container.add(prevButton);
        editButton = new OKButton("Edit Case", editAction());
        //getRootPane().setDefaultButton(editButton);
        container.add(editButton);
        nexButton = new Button("Next", nextAction());
        container.add(nexButton);
        getRootPane().setDefaultButton(nexButton);
        
        resetButtons();
        panel.add(container, BorderLayout.CENTER);
        return panel;
    }
    
    private Action editAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                clearMessage();
                try {
                    if (hasChanges()){
                        doSave();
                        resetChanges();
                    }
                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    managerPresenter.doEdit(view, caseObj);
                    disposeView();
                } catch (EmfException e) {
                    //e.printStackTrace();
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    private Action prevsAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                clearMessage();
                if (hasChanges()){
                    doSave();
                    resetChanges();
                }
                if (setCaseObjects.get(currentIndex-1) != null){
                    currentObject = setCaseObjects.get(currentIndex-1);
                    currentIndex--;
                    resetButtons(); 
                    try {
                        panelRefresh();
                    } catch (EmfException e) {
                        messagePanel.setMessage(e.getMessage());
                    } 
                }
            }
        };

        return action;
    }
    
    private Action nextAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                clearMessage();
                if (hasChanges()){
                    doSave();
                    resetChanges();
                }
                if (setCaseObjects.get(currentIndex+1) != null){
                    currentObject = setCaseObjects.get(currentIndex+1);
                    currentIndex++;
                    resetButtons(); 
                    try {
                        panelRefresh();
                    } catch (EmfException e) {
                        messagePanel.setMessage(e.getMessage());
                    } 
                }
            }
        };
        return action;
    }
    
    private void resetButtons(){
        prevButton.setEnabled(true);
        nexButton.setEnabled(true);
        getRootPane().setDefaultButton(nexButton);
        if (currentIndex == 0 )
            prevButton.setEnabled(false);
        if (setCaseObjects.size() == currentIndex+1){
            nexButton.setEnabled(false);
            getRootPane().setDefaultButton(editButton);
        }
    }
    
    private JPanel displayParam(CaseParameter param) throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        Dimension preferredSize = new Dimension(380, 25);
        
        JLabel parameterName = new JLabel(param.getParameterName().toString());
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, panel);

        JLabel envtVar = new JLabel(param.getEnvVar()==null? "":param.getEnvVar().toString());
        layoutGenerator.addLabelWidgetPair("Envt. Variable:", envtVar, panel);
        
        JLabel sector = new JLabel(param.getSector()==null? "All jobs for sector" :param.getSector().toString());
        layoutGenerator.addLabelWidgetPair("Sector:", sector, panel);
        
        JLabel job = new JLabel(presenter.getJobName(param.getJobId()));
        layoutGenerator.addLabelWidgetPair("Job:", job, panel);
        
        JLabel varTypes = new JLabel(param.getType()==null? "":param.getType().toString());
        layoutGenerator.addLabelWidgetPair("Type:", varTypes, panel);
        
        envValue = new TextField("value", param.getValue(), 34);
        envValue.setPreferredSize(preferredSize);
        addChangeable(envValue);
        layoutGenerator.addLabelWidgetPair("Value:", envValue, panel);
        
        purpose = new TextArea("Information", param.getPurpose(), 34, 3);
        purpose.setEditable(false);
        ScrollableComponent scrolpane = new ScrollableComponent(purpose);
        scrolpane.setPreferredSize(new Dimension(380, 100));
        layoutGenerator.addLabelWidgetPair("Information:", scrolpane, panel);

        JLabel required = new JLabel(param.isRequired()? "True" : "False" );
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }


    private void doSave() {
        clearMessage();
        try {
            validateFields();
            if (!currentObject.isInput())
                presenter.doSaveParam((CaseParameter)currentObject.getObject());
            else
                presenter.doSaveInput((CaseInput)currentObject.getObject());
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setMessage("Could not close: " + e.getMessage());
        }
    }
    
    private void validateFields() throws EmfException {
        if (!currentObject.isInput()){
            CaseParameter para = (CaseParameter) currentObject.getObject();
            para.setValue(envValue.getText() == null ? "" : envValue.getText().trim());
        }else{
            setInputFieldsPanel.setFields();
        }
    }
    
    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void display(CaseInput input) {
        // NOTE Auto-generated method stub
        
    }

    public void notifyLockFailure(Case caseObj) {
        String message = "Cannot edit Properties of Case: " + caseObj + System.getProperty("line.separator")
        + " as it was locked by User: " + caseObj.getLockOwner() + "(at " + format(caseObj.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
        dialog.confirm();
    }
    
    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }


}

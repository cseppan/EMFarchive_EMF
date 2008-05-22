package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditor;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

public class SensitivityWindow extends DisposableInteralFrame implements SensitivityView {
    private SensitivityPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private JRadioButton newRadioButton;

    private JRadioButton existRadioButton;
    
    private ButtonGroup buttonGroup;

    private EditableComboBox senName;

    private TextField senCasebbrev, jobGroup;

    private EmfConsole parentConsole;
    
    private Case parentCase;
    
    private List<CaseCategory> categories = new ArrayList<CaseCategory>();
    
    private List<Case> templateCases;
    
    private JList templateJobsList;
    
    private CaseManagerPresenter parentPresenter;
    
    private ComboBox senTypeCombox;
    private ComboBox categoryCombox;
    
    private Dimension preferredSize = new Dimension(276, 20);

    public SensitivityWindow(DesktopManager desktopManager, EmfConsole parentConsole, List<CaseCategory> categories) {
        super("Sensitivity", new Dimension(490, 490), desktopManager);
        super.setName(title);
        this.parentConsole = parentConsole;
        this.categories.addAll(categories);
        this.categories.remove(new CaseCategory("All"));
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(SensitivityPresenter presenter, CaseManagerPresenter parentPresenter) {
        this.presenter = presenter;
        this.parentPresenter = parentPresenter; 
    }

    public void display(Case case1) {
        super.setLabel("Add Sensitivity for Case: " + case1.getName() );
        layout.removeAll();
        doLayout(case1);
        super.display();
        super.resetChanges();
        
    }
    
    private void doLayout(Case parentCase) {
        this.parentCase = parentCase;
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createTopPanel(parentCase));
        layout.add(createCasePanel(parentCase));
        layout.add(createSenPanel(parentCase));
        layout.add(createButtonsPanel());
    }

    private JPanel createTopPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        Label caseNameNAbbr = new Label("Case Name", case1.getName() + " (" + case1.getAbbreviation() + ")");
        layoutGenerator.addLabelWidgetPair("Parent Case:", caseNameNAbbr, panel);

        layoutGenerator.addLabelWidgetPair("Select:", newOrExistRadios(), panel);
       
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    private JPanel createCasePanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Case"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        senName = new EditableComboBox(new String[0]);
        senName.setPreferredSize(preferredSize);
        addChangeable(senName);
        layoutGenerator.addLabelWidgetPair("Name:", senName, panel);
        
        senCasebbrev = new TextField("CaseAbbreviation", 25);
        addChangeable(senCasebbrev);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", senCasebbrev, panel);

        categoryCombox = new ComboBox("Select One", categories.toArray(new CaseCategory[0]));
        categoryCombox.setPreferredSize(preferredSize);
        categoryCombox.setSelectedItem(getSenTemCategory("Sensitivity"));
        layoutGenerator.addLabelWidgetPair("Case Category: ", categoryCombox, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel createSenPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sensitivity"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        CaseCategory category = getSenTemCategory("Sensitivity Template");
        try {
            getAllSenTemplateCases(category);
        } catch (EmfException e) {
            messagePanel.setError("Couldn't get Senstivity Template Cases: " + e.getMessage());
        }
        
        senTypeCombox = new ComboBox("Select One", templateCases.toArray(new Case[0]));
        senTypeCombox.setPreferredSize(preferredSize);
        senTypeCombox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        layoutGenerator.addLabelWidgetPair("Sensitivity Type:", senTypeCombox, panel);

        jobGroup = new TextField("JobGroup", 25);
        addChangeable(jobGroup);
        layoutGenerator.addLabelWidgetPair("Job Group:", jobGroup, panel);

        layoutGenerator.addLabelWidgetPair("Sensitivity Jobs: ", buildjobsPanel(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(15);
        layout.setVgap(10);
        container.setLayout(layout);
        
        Button wizardButton = new Button("Wizard", setAction());
        container.add(wizardButton);
        Button editButton = new OKButton("Edit Case", editAction());
        container.add(editButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(wizardButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }
    
    
    private CaseCategory getSenTemCategory(String name){
        for ( CaseCategory cat : categories){
            if (cat.getName().trim().equalsIgnoreCase(name))
            return cat; 
        }
        return null; 
    }
    
//    private void setAbbrev(){
//        senJobAbrev = new TextField("JobAbbreviation", 25);
//        addChangeable(senJobAbrev);
//    }
    
    private void getAllSenTemplateCases(CaseCategory category) throws EmfException{
        this.templateCases = new ArrayList<Case>();
        templateCases.addAll(Arrays.asList(presenter.getCases(category)));
        Collections.sort(templateCases);
    }
    
    private JScrollPane buildjobsPanel() {
        templateJobsList = new JList();
        templateJobsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(templateJobsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(280, 100));
        return scrollPane;
    }

    private void refresh(){
        try {
            if (senTypeCombox.getSelectedItem() == null ){
                refreshjobs(new CaseJob[] {});
                return; 
            }
            refreshjobs(presenter.getCaseJobs((Case) senTypeCombox.getSelectedItem()));
        } catch (EmfException e1) {
            e1.printStackTrace();
        }
    }
    
    public void refreshjobs(CaseJob[] jobs) {
        templateJobsList.setListData(jobs);
    }
    
    private JPanel newOrExistRadios() {
        newRadioButton = new JRadioButton("New");
        newRadioButton.setSelected(true);
        existRadioButton = new JRadioButton("Add to existing");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(newRadioButton);
        buttonGroup.add(existRadioButton);

        JPanel radioPanel = new JPanel();
        radioPanel.add(newRadioButton, radioButtonAction());
        radioPanel.add(existRadioButton, radioButtonAction());
        return radioPanel;
    }

    private Action editAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                try {
                    validateFields();
                    Case newCase = presenter.doSave(parentCase.getId(), ((Case)senTypeCombox.getSelectedItem()).getId(), jobIds(), getJobGroup(), setSensitivityCase());
                    resetChanges();
                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    parentPresenter.doEdit(view, newCase);
                    disposeView();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }
    
    private Action radioButtonAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        };
    }
    
    private String getJobGroup(){
        return jobGroup.getText().trim();
    }
    
    private void resetFields(){
        if (buttonGroup.getSelection().equals(newRadioButton.getModel())){
            //senName.setModel(new String[0]);
            //setNewCaseFields(); 
            return;
        }
        if (buttonGroup.getSelection().equals(existRadioButton.getModel())){
            //setExistingCaseFields();
            return; 
        }
    }
    
    private Case setSensitivityCase(){
        Case sensitivityCase = new Case();
        sensitivityCase.setName(senName.getSelectedItem().toString());
        sensitivityCase.setAbbreviation(new Abbreviation(senCasebbrev.getText()));
        
        sensitivityCase.setCaseCategory((CaseCategory) categoryCombox.getSelectedItem());
        return sensitivityCase;
    }
    
    private void validateFields() throws EmfException{
        if ( senName.getSelectedItem() == null )
            throw new EmfException("Please specify a name. ");
        
        validateJobGroup(jobGroup.getText().trim());
        
        if ( senTypeCombox.getSelectedItem() == null )
            throw new EmfException("Please specify sensitivity type. ");
    }
    
   private void validateJobGroup(String group) throws EmfException{
       String underscore ="_";
       if (jobGroup.getText() == null || jobGroup.getText().length() == 0)
           return; 
       for (int i = 0; i < group.length(); i++) {
           if (!(Character.isLetterOrDigit(group.charAt(i)) 
                   || underscore.equals(group.charAt(i)+"_")))
               throw new EmfException ("Job group must contain only letters, digits, and underscores. ");
       }
   }

   private int[] jobIds() throws EmfException {
        int jobsNumber = templateJobsList.getSelectedValues().length;
        //System.out.println("selectedJobs length="+ templateJobsList.getSelectedValues().length);    
        
        if (jobsNumber == 0)
            throw new EmfException("Please select one or more jobs.");
        
        int[] selectedIndexes = new int[jobsNumber];
        for (int i = 0; i < jobsNumber; i++)
            selectedIndexes[i] =((CaseJob)templateJobsList.getSelectedValues()[i]).getId();
        
        return selectedIndexes;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }
    
    private Action setAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                try {
                    validateFields();
                    Case newCase = presenter.doSave(parentCase.getId(), ((Case)senTypeCombox.getSelectedItem()).getId(), jobIds(), getJobGroup(), setSensitivityCase());
                    resetChanges();
                    setCaseView(newCase);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }


    private void setCaseView(Case newCase) throws EmfException{
        if (newCase == null)
            throw new EmfException ("The new sensitivity case is null.");
        
        String title = "Sensitivity Wizard: " + newCase.getName();
        presenter.doDisplaySetCaseWindow(newCase, title, parentConsole, desktopManager, parentPresenter);
        presenter.doClose();
    }
    
}

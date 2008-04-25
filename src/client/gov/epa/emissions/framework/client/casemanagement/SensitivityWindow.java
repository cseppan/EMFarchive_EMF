package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

    private TextField senName;

    private TextField senAbrev;

    private EmfConsole parentConsole;
    
    //private SensitivityWindow sensitivityWindow;

    private Case parentCase;
    
    private List<CaseCategory> categories;
    
    private List<Case> templateCases;
    
    private JList templateJobsList;
    
    private ComboBox senTypeCombox;
    private ComboBox categoryCombox;

    public SensitivityWindow(String title, DesktopManager desktopManager, EmfConsole parentConsole, List<CaseCategory> categories) {
        super(title, new Dimension(480, 450), desktopManager);
        super.setName(title);
        this.parentConsole = parentConsole;
        //this.sensitivityWindow = this;
        this.categories = categories;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(SensitivityPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Case case1) {
        //super.setLabel("Sensitivity");
        layout.removeAll();
        doLayout(layout, case1);
        super.display();
        super.resetChanges();
        
    }
    
    private void doLayout(JPanel layout, Case parentCase) {
        this.parentCase = parentCase;
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createMainPanel(parentCase));
        layout.add(createButtonsPanel());
    }


    private JPanel createMainPanel(Case case1) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        Label caseName = new Label("Case Name", case1.getName());
        layoutGenerator.addLabelWidgetPair("Sensitivity for Case:", caseName, panel);

        layoutGenerator.addLabelWidgetPair("Sensitivity for Case:", newOrExistRadios(), panel);

        CaseCategory category = getSenTemCategory("Sensitivity Template");
        try {
            getAllSenTemplateCases(category);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            messagePanel.setError("Couldn't get Senstivity Template Cases");
        } //{ "Adjust AQM-ready Emissions" };
        
        senTypeCombox = new ComboBox("Select One", templateCases.toArray(new Case[0]));
        senTypeCombox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        layoutGenerator.addLabelWidgetPair("Sensitivity Type:", senTypeCombox, panel);

        senName = new TextField("Sensitivity Name", 25);
        addChangeable(senName);
        layoutGenerator.addLabelWidgetPair("Sensitivity Name:", senName, panel);

        setAbbrev();
        layoutGenerator.addLabelWidgetPair("Sensitivity Abbreviation:", senAbrev, panel);

        categoryCombox = new ComboBox("Select One", categories.toArray(new CaseCategory[0]));
        categoryCombox.setSelectedItem(getSenTemCategory("Sensitivity"));
        layoutGenerator.addLabelWidgetPair("Case Category: ", categoryCombox, panel);

        layoutGenerator.addLabelWidgetPair("Sensitivity Jobs: ", buildjobsPanel(), panel);
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }
    
    private CaseCategory getSenTemCategory(String name){
        for ( CaseCategory cat : categories){
            if (cat.getName().trim().equalsIgnoreCase(name))
            return cat; 
        }
        return null; 
    }
    
    private void setAbbrev(){
        senAbrev = new TextField("Abbreviation", 25);
        addChangeable(senAbrev);
    }
    
    private void getAllSenTemplateCases(CaseCategory category) throws EmfException{
        this.templateCases = new ArrayList<Case>();
        //templateCases.add(new CaseCategory("All"));
        templateCases.addAll(Arrays.asList(presenter.getCases(category)));
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
        // newSenCase.addActionListener(radioButtonAction());
        newRadioButton.setSelected(true);
        existRadioButton = new JRadioButton("Add to existing");
        // existSenCase.addActionListener(radioButtonAction());
        existRadioButton.setEnabled(false);
        // Create logical relationship between JradioButtons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(newRadioButton);
        buttonGroup.add(existRadioButton);

        JPanel radioPanel = new JPanel();
        radioPanel.add(newRadioButton);
        radioPanel.add(existRadioButton);
        return radioPanel;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                resetChanges();

                try {
                    Case sensitivityCase = new Case();
                    //Case sensitivityCase = presenter.copyCase(parentCase.getId());
                    sensitivityCase.setName(senName.getText());
                    sensitivityCase.setAbbreviation(new Abbreviation(senAbrev.getText()));
                    
                    //sensitivityCase.setCaseTemplate(true);
                    sensitivityCase.setCaseCategory((CaseCategory) categoryCombox.getSelectedItem());
                    Case updated = presenter.doSave(parentCase.getId(), ((Case)senTypeCombox.getSelectedItem()).getId(), jobIds(), sensitivityCase);
                    //Case updated = presenter.updateCase(sensitivityCase);
                    
                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    presenter.editCase(view, updated);
                    disposeView();
                } catch (EmfException e) {
                    e.printStackTrace();
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }
    
    private int[] jobIds(){
        int jobsNumber = templateJobsList.getSelectedValues().length;
        //System.out.println("selectedJobs length="+ templateJobsList.getSelectedValues().length);    
        
        int[] selectedIndexes = new int[jobsNumber];
        for (int i = 0; i < jobsNumber; i++) {
            selectedIndexes[i] =((CaseJob)templateJobsList.getSelectedValues()[i]).getId();
            System.out.println("selectedIndexes[i]="+selectedIndexes[i]);
        }
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
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
        container.setLayout(layout);
        
        Button wizardButton = new Button("Wizard", setAction());
        wizardButton.setEnabled(false);
        container.add(wizardButton);
        Button saveButton = new OKButton("Edit Case", saveAction());
        container.add(saveButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

}

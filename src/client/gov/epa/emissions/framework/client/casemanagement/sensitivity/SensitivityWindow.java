package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
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
import java.awt.Cursor;
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

    private ComboBox senName;
    
    private ComboBox jobGroup;

    private TextField senCaseAbrev;

    private EmfConsole parentConsole;

    private Case parentCase;

    private List<CaseCategory> categories = new ArrayList<CaseCategory>();

    private List<Case> templateCases;

    private JList templateJobsList;

    private CaseManagerPresenter parentPresenter;

    private ComboBox senTypeCombox;

    private ComboBox categoryCombox;

    // private JPanel casePanel;

    private Dimension preferredSize = new Dimension(276, 20);

    public SensitivityWindow(DesktopManager desktopManager, EmfConsole parentConsole, List<CaseCategory> categories) {
        super("Sensitivity", new Dimension(560, 490), desktopManager);
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
        super.setLabel("Add Sensitivity for Case: " + case1.getName());
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

        layoutGenerator.addLabelWidgetPair("Sensitivity Case:", newOrExistRadios(), panel);

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

        senName = new ComboBox(new Case[0]);
        senName.setPreferredSize(preferredSize);
        senName.setEditable(true);
        addChangeable(senName);
        senName.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    messagePanel.clear();
                    updateCaseInfo();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Name:", senName, panel);

        senCaseAbrev = new TextField("CaseAbbreviation", 25);
        addChangeable(senCaseAbrev);
        layoutGenerator.addLabelWidgetPair("Abbreviation:", senCaseAbrev, panel);

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
                messagePanel.clear();
                refresh();
            }
        });
        layoutGenerator.addLabelWidgetPair("Sensitivity Type:", senTypeCombox, panel);

        jobGroup = new ComboBox("", new String[] {""});
        jobGroup.setPreferredSize(preferredSize);
        jobGroup.setEditable(true);
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

        Button wizardButton = new Button("Wizard", setAction(this));
        container.add(wizardButton);
        Button editButton = new OKButton("Edit Case", editAction(this));
        container.add(editButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(wizardButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private CaseCategory getSenTemCategory(String name) {
        for (CaseCategory cat : categories) {
            if (cat.getName().trim().equalsIgnoreCase(name))
                return cat;
        }
        return null;
    }

    private void getAllSenTemplateCases(CaseCategory category) throws EmfException {
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

    private void refresh() {
        try {
            if (senTypeCombox.getSelectedItem() == null) {
                refreshjobs(new CaseJob[] {});
                return;
            }
            refreshjobs(presenter.getCaseJobs((Case) senTypeCombox.getSelectedItem()));
        } catch (EmfException e1) {
            messagePanel.setError(e1.getMessage());
        }
    }

    public void refreshjobs(CaseJob[] jobs) {
        templateJobsList.setListData(jobs);
    }

    private JPanel newOrExistRadios() {
        newRadioButton = new JRadioButton("Create new case");
        newRadioButton.setSelected(true);
        existRadioButton = new JRadioButton("Add to existing case");
        buttonGroup = new ButtonGroup();
        buttonGroup.add(newRadioButton);
        newRadioButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                newRadioButtonAction();
            }
        });
        buttonGroup.add(existRadioButton);
        existRadioButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    existRadioButtonAction();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });

        JPanel radioPanel = new JPanel();
        radioPanel.add(newRadioButton);

        radioPanel.add(existRadioButton);
        return radioPanel;
    }

    private Action editAction(final SensitivityWindow window) {
        Action action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                messagePanel.setMessage("Server is processing sensitivity case...");
                
                try {
                    validateFields();
                    Case sensitivityCase = null;

                    if (newRadioButton.isSelected())
                        sensitivityCase = presenter.doSave(parentCase.getId(), ((Case) senTypeCombox.getSelectedItem())
                                .getId(), jobIds(), getJobGroup(), setSensitivityCase());
                    else
                        sensitivityCase = presenter.addSensitivities(parentCase.getId(), ((Case) senTypeCombox.getSelectedItem())
                                .getId(), jobIds(), getJobGroup(), (Case)senName.getSelectedItem());
                    
                    if (sensitivityCase == null) {
                        messagePanel.setError("Failed processing sensitivity case.");
                        return;
                    }
                    
                    resetChanges();
                    
                    CaseEditor view = new CaseEditor(parentConsole, presenter.getSession(), desktopManager);
                    parentPresenter.doEdit(view, sensitivityCase);
                    disposeView();
                    messagePanel.clear();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                } finally {
                    window.setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        return action;
    }

    private String getJobGroup() {
        return (String)jobGroup.getSelectedItem();
    }

    private void newRadioButtonAction() {
        senName.setEnabled(true);
        senName.removeAllItems();
        senName.clear();
        senName.resetModel(new Case[0]);
        senName.setEditable(true);
        senName.validate();
        senCaseAbrev.setEditable(true);
        senCaseAbrev.setText("");
        categoryCombox.setEnabled(true);
    }

    private void existRadioButtonAction() throws EmfException {
        senName.removeAllItems();
        senName.resetModel(presenter.getSensitivityCases(parentCase.getId()));
        senName.setEditable(false);
        senName.validate();

        Case selected = (Case) senName.getSelectedItem();
        senCaseAbrev.setEditable(false);
        senCaseAbrev.setText(selected == null ? "" : selected.getAbbreviation().getName());
        categoryCombox.setSelectedItem(selected == null ? 0 : selected.getCaseCategory());
        categoryCombox.setEnabled(false);
    }

    private void updateCaseInfo() throws EmfException {
        Object selected = senName.getSelectedItem();
        
        if (selected == null || selected  instanceof String) {
            if (existRadioButton.isSelected()) {
                senCaseAbrev.setText("");
                categoryCombox.setSelectedIndex(0);
            }
            
            return;
        }
        
        Case selectedCase = (Case) selected;

        if (selectedCase != null) {
            senCaseAbrev.setText(selectedCase.getAbbreviation().getName());
            categoryCombox.setSelectedItem(selectedCase.getCaseCategory());
            jobGroup.resetModel(presenter.getJobGroups(selectedCase));
        }
    }

    private Case setSensitivityCase() {
        Case sensitivityCase = new Case();
        sensitivityCase.setName(senName.getSelectedItem().toString());
        sensitivityCase.setAbbreviation(new Abbreviation(senCaseAbrev.getText()));

        sensitivityCase.setCaseCategory((CaseCategory) categoryCombox.getSelectedItem());
        return sensitivityCase;
    }

    private void validateFields() throws EmfException {
        if (senName.getSelectedItem() == null)
            throw new EmfException("Please specify a name. ");

        validateJobGroup(getJobGroup());

        if (senTypeCombox.getSelectedItem() == null)
            throw new EmfException("Please specify sensitivity type. ");
    }

    private void validateJobGroup(String group) throws EmfException {
        if (group == null || group.trim().isEmpty())
            return;
        
        for (int i = 0; i < group.length(); i++) {
            if (!Character.isLetterOrDigit(group.charAt(i)) && (group.charAt(i) != '_'))
                throw new EmfException("Job group must contain only letters, digits, and underscores. ");
        }
    }

    private int[] jobIds() throws EmfException {
        int jobsNumber = templateJobsList.getSelectedValues().length;

        if (jobsNumber == 0)
            throw new EmfException("Please select one or more jobs.");

        int[] selectedIndexes = new int[jobsNumber];
        for (int i = 0; i < jobsNumber; i++)
            selectedIndexes[i] = ((CaseJob) templateJobsList.getSelectedValues()[i]).getId();

        return selectedIndexes;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                doClose();
            }
        };

        return action;
    }

    private Action setAction(final SensitivityWindow view) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                messagePanel.clear();
                view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                messagePanel.setMessage("Server is processing sensitivity case...");
                
                try {
                    validateFields();
                    Case sensCase = null;
                    
                    if (newRadioButton.isSelected())
                        sensCase = presenter.doSave(parentCase.getId(), ((Case) senTypeCombox.getSelectedItem())
                            .getId(), jobIds(), getJobGroup(), setSensitivityCase());
                    else
                        sensCase = presenter.addSensitivities(parentCase.getId(), ((Case) senTypeCombox.getSelectedItem())
                                .getId(), jobIds(), getJobGroup(), (Case)senName.getSelectedItem());
                    
                    if (sensCase == null) {
                        messagePanel.setError("Failed processing sensitivity case.");
                        return;
                    }
                        
                    resetChanges();
                    setCaseView(sensCase);
                    messagePanel.clear();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                } finally {
                    view.setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    private void setCaseView(Case newCase) throws EmfException {
        if (newCase == null)
            throw new EmfException("The new sensitivity case is null.");

        String title = "Sensitivity Wizard: " + newCase.getName();
        presenter.doDisplaySetCaseWindow(newCase, title, parentConsole, desktopManager, parentPresenter);
        presenter.doClose();
    }

}

package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditControlStrategyMeasuresTab extends JPanel implements ControlStrategyMeasuresTabView {

    private ListWidget classesList;

    private ControlMeasureClass[] allClasses;

    private ControlMeasureClass[] classes;

    private EditControlStrategyMeasuresTabPresenter presenter;

    private ManageChangeables changeablesList;

    private ControlMeasureClass defaultClass = new ControlMeasureClass("All");

    private JPanel mainPanel;

    private SingleLineMessagePanel messagePanel;

    private EmfTableModel tableModel;

    private ControlStrategyMeasureTableData tableData;

    private SortFilterSelectModel sortFilterSelectModel;

    private EmfConsole parent;

    private EmfSession session;

    private Button addButton = new AddButton(addAction());
    
    private ControlStrategy controlStrategy;

    private ControlStrategyMeasure[] controlStrategyMeasures;

    // private JPanel sortFilterPanelContainer = new JPanel();

    private NumberFieldVerifier verifier;

    public EditControlStrategyMeasuresTab(ControlStrategy controlStrategy, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
        this.controlStrategy = controlStrategy;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
        this.session = session;
        this.verifier = new NumberFieldVerifier("");
    }

    public void display(ControlStrategy strategy) throws EmfException {
        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses();
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setupLayout(changeablesList);
    }

    private void setupLayout(ManageChangeables changeables) {
        try {
            controlStrategyMeasures = presenter.getControlMeasures();
            tableData = new ControlStrategyMeasureTableData(controlStrategyMeasures);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }

        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        this.add(createClassesPanel(changeables), BorderLayout.NORTH);
        // mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
        buildSortFilterPanel();
        // mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        this.add(mainPanel, BorderLayout.CENTER);
        // disable class filter since there are measures selected
        if (sortFilterSelectModel.getRowCount() > 0) classesList.setEnabled(false);
    }

    private SortFilterSelectionPanel sortFilterPanel() {
        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new SortFilterSelectModel(tableModel);
//        changeablesList.addChangeable(sortFilterSelectModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parent, sortFilterSelectModel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(550, 300));
        sortFilterSelectionPanel.sort(sortCriteria());
        return sortFilterSelectionPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = {"Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] {true, true }, new boolean[] { true, true });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        if (presenter.getAllControlMeasures().length == 0)
            addButton.setEnabled(false);
        addButton.setMargin(new Insets(2, 2, 2, 2));
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        removeButton.setMargin(new Insets(2, 2, 2, 2));
        panel.add(removeButton);

        Button rpButton = new BorderlessButton("Set RP %", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    setRulePenetration();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(rpButton);

        Button reButton = new BorderlessButton("Set RE %", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    setRuleEffectiveness();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(reButton);

        Button orderButton = new BorderlessButton("Set Order", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    setApplyOrder();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        panel.add(orderButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void selectionView() {
        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList);
        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, session,
                this.presenter.getAllControlMeasures());
        try {
            presenter.display(view);
        } catch (Exception exp) {
            messagePanel.setError(exp.getMessage());
        }
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        };
    }

    protected void remove() {
        messagePanel.clear();
        List selected = sortFilterSelectModel.selected();

        if (selected.size() == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        ControlStrategyMeasure[] records = (ControlStrategyMeasure[]) selected.toArray(new ControlStrategyMeasure[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            buildSortFilterPanel();
        }

        // disable class filter, if there are measures selected, or enable if no
        // measures are selected
        if (sortFilterSelectModel.getRowCount() == 0) {
            classesList.setEnabled(true);
        } else {
            classesList.setEnabled(false);
        }

    }

    private void buildSortFilterPanel() {
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
        SortFilterSelectionPanel panel = sortFilterPanel();
        mainPanel.add(panel);
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);

        // SortFilterSelectionPanel panel = sortFilterPanel();
        // sortFilterPanelContainer.removeAll();
        // sortFilterPanelContainer.add(panel);
        // mainPanel.add(sortFilterPanelContainer);
    }

    private JPanel createClassesPanel(ManageChangeables changeables) {

        // get all measure classes and cs classes
        // and add default "ALL" to both lists
        List allClassesList = new ArrayList(Arrays.asList(allClasses));
        allClassesList.add(0, defaultClass);
        allClasses = (ControlMeasureClass[]) allClassesList.toArray(new ControlMeasureClass[0]);
        if (classes.length == 0) {
            List selClassesList = new ArrayList();
            selClassesList.add(defaultClass);
            classes = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        // build list widget
        this.classesList = new ListWidget(allClasses, classes);
        changeables.addChangeable(classesList);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 100, 0, 300));
        JLabel label = new JLabel("Classes to Include:");
        JScrollPane scrollPane = new JScrollPane(classesList);
        scrollPane.setPreferredSize(new Dimension(20, 100));
        panel.add(label, BorderLayout.NORTH);
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scrollPane, BorderLayout.NORTH);
        panel.add(scrollPanel);
        return panel;
    }

    public void save(ControlStrategy controlStrategy) {
        controlStrategy.setControlMeasureClasses(getControlMeasureClasses());
        ControlStrategyMeasure[] cms = {};
        if (tableData != null) {
            cms = new ControlStrategyMeasure[tableData.rows().size()];
            for (int i = 0; i < tableData.rows().size(); i++) {
                cms[i] = (ControlStrategyMeasure)tableData.element(i);
            }
        } else {
            cms = controlStrategy.getControlMeasures();
        }
        controlStrategy.setControlMeasures(cms);
    }

    private ControlMeasureClass[] getControlMeasureClasses() {
        ControlMeasureClass[] controlMeasureClasses = {};
        ControlMeasureClass[] selClasses = {};
        if (classesList != null){
            selClasses = Arrays.asList(classesList.getSelectedValues()).toArray(
                    new ControlMeasureClass[0]);
        } else {
            selClasses = controlStrategy.getControlMeasureClasses();
        }

        // make sure we don't include the All class, its just for display purposes,
        // its not stored in the database
        if (selClasses.length != 0 && !(selClasses.length == 1 && selClasses[0].equals(defaultClass))) {
            List selClassesList = new ArrayList();
            for (int i = 0; i < selClasses.length; i++)
                if (!selClasses[i].equals(defaultClass))
                    selClassesList.add(selClasses[i]);

            controlMeasureClasses = (ControlMeasureClass[]) selClassesList.toArray(new ControlMeasureClass[0]);
        }
        return controlMeasureClasses;
    }

    public void refresh(ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub

    }

    public void observe(EditControlStrategyMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(LightControlMeasure[] measures, double rule, double rulePenetration, double ruleEffective) {
        if (measures.length > 0 ) {
            ControlStrategyMeasure[] strategyMeasures = new ControlStrategyMeasure[measures.length];
            for (int i = 0; i < measures.length; i++) {
                ControlStrategyMeasure csm = new ControlStrategyMeasure(measures[i]);
                //default rule pen and eff to 100%, and order to 1
                csm.setRulePenetration(Double.valueOf(rulePenetration));
                csm.setRuleEffectiveness(Double.valueOf(ruleEffective));
                csm.setApplyOrder(Double.valueOf(rule));
                strategyMeasures[i] = csm;
            }
            tableData.add(strategyMeasures);

            buildSortFilterPanel();

            // disable class filter since there are measures selected
            if (sortFilterSelectModel.getRowCount() > 0) classesList.setEnabled(false);
        }
    }

    private void setRulePenetration() throws EmfException {
        messagePanel.clear();
        //get selected items
        ControlStrategyMeasure[] selectedMeasures = (sortFilterSelectModel.selected()).toArray(new ControlStrategyMeasure[0]);
        //get all measures
        ControlStrategyMeasure[] measures = tableData.sources();

        if (selectedMeasures.length == 0) {
            messagePanel.setError("Please select an items that you want to update.");
            return;
        }

        String inputValue = JOptionPane.showInputDialog("Please input a rule penetration", "");

        if (inputValue != null) {
            //validate value
            Double value = validateRulePenetration(inputValue);
            //only update items that have been selected
            for (int i = 0; i < selectedMeasures.length; i++) {
                for (int j = 0; j < measures.length; j++) {
                    if (selectedMeasures[i].equals(measures[j])) {
                        measures[j].setRulePenetration(value); 
                    }
                }
            }
            //repopulate the tabe data
            tableData = new ControlStrategyMeasureTableData(measures);
            //rebuild the sort filter panel
            buildSortFilterPanel();
        }
    }
    
    private void setRuleEffectiveness() throws EmfException {
        messagePanel.clear();
        //get selected items
        ControlStrategyMeasure[] selectedMeasures = (sortFilterSelectModel.selected()).toArray(new ControlStrategyMeasure[0]);
        //get all measures
        ControlStrategyMeasure[] measures = tableData.sources();

        if (selectedMeasures.length == 0) {
            messagePanel.setError("Please select an items that you want to update.");
            return;
        }

        String inputValue = JOptionPane.showInputDialog("Please input a rule effectiveness", "");

        if (inputValue != null) {
            //validate value
            Double value = validateRuleEffectiveness(inputValue);
            //only update items that have been selected
            for (int i = 0; i < selectedMeasures.length; i++) {
                for (int j = 0; j < measures.length; j++) {
                    if (selectedMeasures[i].equals(measures[j])) {
                        measures[j].setRuleEffectiveness(value); 
                    }
                }
            }
            //repopulate the tabe data
            tableData = new ControlStrategyMeasureTableData(measures);
            //rebuild the sort filter panel
            buildSortFilterPanel();
        }
    }
    
    private void setApplyOrder() throws EmfException {
        messagePanel.clear();
        //get selected items
        ControlStrategyMeasure[] selectedMeasures = (sortFilterSelectModel.selected()).toArray(new ControlStrategyMeasure[0]);
        //get all measures
        ControlStrategyMeasure[] measures = tableData.sources();

        if (selectedMeasures.length == 0) {
            messagePanel.setError("Please select an items that you want to update.");
            return;
        }

        String inputValue = JOptionPane.showInputDialog("Please input an apply order", "1");

        if (inputValue != null) {
            //validate value
            double value = validateApplyOrder(inputValue);
            //only update items that have been selected
            for (int i = 0; i < selectedMeasures.length; i++) {
                for (int j = 0; j < measures.length; j++) {
                    if (selectedMeasures[i].equals(measures[j])) {
                        measures[j].setApplyOrder(value); 
                    }
                }
            }
            //repopulate the tabe data
            tableData = new ControlStrategyMeasureTableData(measures);
            //rebuild the sort filter panel
            buildSortFilterPanel();
        }
    }
    
    private Double validateRuleEffectiveness(String ruleEffectiveness) throws EmfException {
        if (ruleEffectiveness == null || ruleEffectiveness.trim().length() == 0) return null;
        double value = verifier.parseDouble(ruleEffectiveness);
        if (value <= 0 || value > 100)
            throw new EmfException(
                    "Enter the Rule Effectiveness as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01%");
        return new Double(value);
    }

    private Double validateRulePenetration(String rulePenetration) throws EmfException {
        if (rulePenetration == null || rulePenetration.trim().length() == 0) return null;
        double value = verifier.parseDouble(rulePenetration);
        if (value <= 0 || value > 100)
            throw new EmfException(
                    "Enter the Rule Penetration as a percent between 0 and 100. Eg: 1 = 1%.  0.01 = 0.01%");
        return new Double(value);
    }

    private Double validateApplyOrder(String applyOrder) throws EmfException {
        double value = verifier.parseDouble(applyOrder);
        return value;
    }

    public void startControlMeasuresRefresh() {
        addButton.setEnabled(false);
    }

    public void endControlMeasuresRefresh() {
        addButton.setEnabled(true);
    }
}
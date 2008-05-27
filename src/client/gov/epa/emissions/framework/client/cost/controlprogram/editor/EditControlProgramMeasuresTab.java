package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.EditButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
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

public class EditControlProgramMeasuresTab extends JPanel implements EditControlProgramTabView {
    private ListWidget classesList;

    private ControlMeasureClass[] allClasses;

    private ControlMeasureClass[] classes;

    private EditControlProgramMeasuresTabPresenter presenter;

    private ManageChangeables changeablesList;

    private ControlMeasureClass defaultClass = new ControlMeasureClass("All");

    private JPanel tablePanel; 

    private JPanel classesPanel;

    private SingleLineMessagePanel messagePanel;

    private SelectableSortFilterWrapper table;

    private ControlProgramMeasureTableData tableData;

    //private SortFilterSelectModel sortFilterSelectModel;

    private EmfConsole parent;

    private Button addButton = new AddButton(addAction());
    
//    private ControlProgram controlProgram;

    private ControlStrategyMeasure[] controlStrategyMeasures;

    // private JPanel sortFilterPanelContainer = new JPanel();

//    private NumberFieldVerifier verifier;

    public EditControlProgramMeasuresTab(ControlProgram controlProgram, ManageChangeables changeablesList,
            SingleLineMessagePanel messagePanel, EmfConsole parentConsole, EmfSession session) {
//        this.controlProgram = controlProgram;
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.parent = parentConsole;
    }

    public void display(ControlProgram controlProgram) throws EmfException {
        //mainPanel = new JPanel(new BorderLayout(5, 5));
        //mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.allClasses = presenter.getAllClasses();
        this.classes = presenter.getControlMeasureClasses();
        setupLayout(changeablesList);
    }

    private void setupLayout(ManageChangeables changeables) {
        try {
            controlStrategyMeasures = presenter.getControlMeasures();
            tableData = new ControlProgramMeasureTableData(controlStrategyMeasures);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }
        this.setLayout(new BorderLayout(5, 5));
        // this.setBorder(BorderFactory.createEmptyBorder(50,50,0,300));
        classesPanel = createClassesPanel(changeables);
        this.add(classesPanel, BorderLayout.NORTH);
        //buildSortFilterPanel();
        this.add(mainPanel(), BorderLayout.CENTER);
        
        // disable class filter since there are measures selected
        if (table.getModel().getRowCount() > 0) 
            classesPanel.setVisible(false);//classesList.setVisible(false);
        else
            classesPanel.setVisible(true);//classesList.setVisible(false);
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = {"Order", "Name" };
        return new SortCriteria(columnNames, new boolean[] {true, true }, new boolean[] { true, true });
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        if (presenter.getAllControlMeasures().length == 0)
            addButton.setEnabled(false);
        addButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(addButton);
        Button editButton = new EditButton(editAction());
        editButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(editButton);
        Button removeButton = new RemoveButton(removeAction());
        removeButton.setMargin(new Insets(2, 5, 2, 5));
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
              //get selected items
              ControlStrategyMeasure[] selectedMeasures = (table.selected()).toArray(new ControlStrategyMeasure[0]);
              int measureSize =selectedMeasures.length;
              if (measureSize == 0) {
                  messagePanel.setMessage("Please select an items that you want to edit.");
                  return;
              }
              propertySetView(measureSize);
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void propertySetView(int measureSize) {
//        ControlMeasureEditView view = new ControlMeasureEditDialog(parent, measureSize);
//        ControlMeasureEditPresenter presenter = new ControlMeasureEditPresenter(this, view, session);
//        try {
//            presenter.display(view);
//        } catch (Exception exp) {
//            messagePanel.setError(exp.getMessage());
//        }
    }
    private void selectionView() {
//        ControlMeasureSelectionView view = new ControlMeasureSelectionDialog(parent, changeablesList);
//        ControlMeasureSelectionPresenter presenter = new ControlMeasureSelectionPresenter(this, view, session,
//                this.presenter.getAllControlMeasures());
//        try {
//            presenter.display(view);
//        } catch (Exception exp) {
//            messagePanel.setError(exp.getMessage());
//        }
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                remove();

                if (table.getModel().getRowCount() > 0) 
                    classesPanel.setVisible(false);
                else
                    classesPanel.setVisible(true);
            }
        };
    }

    protected void remove() {
        messagePanel.clear();
        List selected = table.selected();

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
            refresh();
        }

        // disable class filter, if there are measures selected, or enable if no
        // measures are selected
        if (table.getModel().getRowCount() == 0) {
            classesList.setEnabled(true);
        } else {
            classesList.setEnabled(false);
        }

    }

    private JPanel mainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      
        mainPanel.removeAll();
        mainPanel.add(new JLabel("Measures to Include:"), BorderLayout.NORTH);
        mainPanel.add(tablePanel());
        mainPanel.add(buttonPanel(), BorderLayout.SOUTH);
        return mainPanel; 
    }
    
    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parent, tableData, sortCriteria());
        tablePanel.add(table);

        return tablePanel;
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
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
//        controlStrategy.setControlMeasureClasses(getControlMeasureClasses());
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

    public void observe(EditControlProgramMeasuresTabPresenter presenter) {
        this.presenter = presenter;
    }

    public void add(LightControlMeasure[] measures, Double applyOrder, Double rulePenetration, Double ruleEffective, EmfDataset ds, Integer ver) {
        messagePanel.clear();
        if (measures.length > 0 ) {
            ControlStrategyMeasure[] strategyMeasures = new ControlStrategyMeasure[measures.length];
            for (int i = 0; i < measures.length; i++) {
                ControlStrategyMeasure csm = new ControlStrategyMeasure(measures[i]);
                //default rule pen and eff to 100%, and order to 1
                csm.setRulePenetration(rulePenetration);
                csm.setRuleEffectiveness(ruleEffective);
                csm.setApplyOrder(applyOrder);
                csm.setRegionDataset(ds);
                csm.setRegionDatasetVersion(ver);
                strategyMeasures[i] = csm;
            }
            tableData.add(strategyMeasures);

            refresh();

            // disable class filter since there are measures selected
//            if (sortFilterSelectModel.getRowCount() > 0) classesList.setEnabled(false);
            if (table.getModel().getRowCount() > 0) 
                classesPanel.setVisible(false);
            else
                classesPanel.setVisible(true);
        }
    }



    public void startControlMeasuresRefresh() {
        if (addButton != null) addButton.setEnabled(false);
    }

    public void endControlMeasuresRefresh() {
        if (addButton != null) addButton.setEnabled(true);
    }

    public void edit(Double applyOrder, Double rulePenetration, 
            boolean overrideRulePenetration, Double ruleEffective, 
            boolean overrideRuleEffectiveness, EmfDataset ds, 
            Integer ver) {
        messagePanel.clear();
      //get selected items
      ControlStrategyMeasure[] selectedMeasures = (table.selected()).toArray(new ControlStrategyMeasure[0]);
      //get all measures
      ControlStrategyMeasure[] measures = tableData.sources();

      //only update items that have been selected
      for (int i = 0; i < selectedMeasures.length; i++) {
          for (int j = 0; j < measures.length; j++) {
              if (selectedMeasures[i].equals(measures[j])) {
                  if (applyOrder != null) measures[j].setApplyOrder(applyOrder);
                  if (rulePenetration != null && !overrideRulePenetration) 
                      measures[j].setRulePenetration(rulePenetration);
                  else if (overrideRulePenetration)
                      measures[j].setRulePenetration(null);
                  if (ruleEffective != null && !overrideRuleEffectiveness) 
                      measures[j].setRuleEffectiveness(ruleEffective);
                  else if (overrideRuleEffectiveness)
                      measures[j].setRuleEffectiveness(null);
                  if (ds!=null){
                      if (ds.getName().equals("None")){
                          measures[j].setRegionDataset(null);
                          measures[j].setRegionDatasetVersion(null);
                      } else {
                          measures[j].setRegionDataset(ds);
                          measures[j].setRegionDatasetVersion(ver);
                      }
                  }
              }
          }
      }
      //repopulate the tabe data
      tableData = new ControlProgramMeasureTableData(measures);
      //rebuild the sort filter panel
      refresh();
    }

    public void notifyControlProgramTypeChange(ControlProgramType controlProgramType) {
        // NOTE Auto-generated method stub
        
    }

    public void save(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub
        
    }
}
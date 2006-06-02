package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.mims.analysisengine.table.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditableCostsTab extends JPanel implements EditableCostsTabView {

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    private EditableEmfTableModel model;

    private ControlMeasureCostTableData tableData;

    private ControlMeasureCost cost;

    private JPanel mainPanel;

    private ControlMeasure measure;

    private DesktopManager desktopManager;

    private ManageChangeables changeablesList;

    private MessagePanel messagePanel;

    public EditableCostsTab(ControlMeasure measure, ManageChangeables changeablesList, EmfConsole parent,
            DesktopManager desktopManager, MessagePanel messagePanel) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = parent;
        this.changeablesList = changeablesList;
        this.desktopManager = desktopManager;
        this.messagePanel = messagePanel;
        doLayout(measure);
    }

//    private String getNote() {
//        return "<html><br>&nbsp;&nbsp;Cost per ton will be calculated based on the formula:  cost/ton = slope*(uncontrolled emissions) + constant<br></html>";
//    }

    private void doLayout(ControlMeasure measure) {
        this.measure = measure;
        this.cost = measure.getCost();
        CostRecord[] costRecords = null;
        if (cost != null)
            costRecords = cost.getCostRecords();
        else {
            this.cost = new ControlMeasureCost("");
            costRecords = new CostRecord[0];
        }
        updateMainPanel(costRecords);

        setLayout(new BorderLayout());
//        add(notePanel(getNote()), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel(), BorderLayout.SOUTH);
    }

//    private JPanel notePanel(String note) {
//        JPanel panel = new JPanel(new BorderLayout());
//        panel.add(new JLabel(note), BorderLayout.CENTER);
//        panel.setMinimumSize(new Dimension(500,30));
//        return panel;
//    }

    private void updateMainPanel(CostRecord[] costRecords) {
        mainPanel.removeAll();
        initModel(costRecords);
        JScrollPane pane = sortFilterPane(parentConsole);
        mainPanel.add(pane);
        mainPanel.validate();
    }

    private void initModel(CostRecord[] costRecords) {
        tableData = new ControlMeasureCostTableData(costRecords);
        model = new EditableEmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
    }

    private JScrollPane sortFilterPane(EmfConsole parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("controlMeasureSccTable");
        panel.sort(sortCriteria());
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Pollutant", "Cost Year", "Cost per Ton" };
        return new SortCriteria(columnNames, new boolean[] { true, true, true }, new boolean[] { true, true, true });
    }

    private JPanel controlPanel() {
        Button addButton = new Button("Add", addAction());
        Button removeButton = new Button("Remove", removeAction());
        Button editButton = new Button("Edit", editAction());
        Button importButton = new Button("Import", importAction());
        importButton.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(editButton);
        panel.add(importButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action editAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit();
            }
        };
    }

    private Action importAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // bring the scc reference tables
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        };
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        };
    }

    protected void doRemove() {
        messagePanel.clear();
        
        CostRecord[] records = getSelectedRecords();

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            refreshPanel();
        }
    }

    protected void doAdd() {
        messagePanel.clear();
        CostRecordView view = new CostRecordWindow(changeablesList, desktopManager);
        CostRecordPresenter presenter = new CostRecordPresenter(this, view);
        presenter.display(measure, null);
    }

    private void doEdit() {
        messagePanel.clear();
        
        CostRecord[] records = getSelectedRecords();
        
        if(records.length == 0)
            messagePanel.setError("Please select a record.");
        
        for (int i = 0; i < records.length; i++) {
            CostRecordView view = new CostRecordWindow(changeablesList, desktopManager);
            CostRecordPresenter presenter = new CostRecordPresenter(this, view);
            presenter.display(measure, records[i]);
        }
    }

    private CostRecord[] getSelectedRecords() {
        return (CostRecord[]) selectModel.selected().toArray(new CostRecord[0]);
    }

    public void refreshData() {
        tableData.sortByOrder();
    }

    public void refreshPanel() {
        refreshData();
        updateMainPanel(tableData.sources());
    }

    public void save(ControlMeasure measure) {
        cost.setCostRecords(tableData.sources());
        measure.setCost(cost);
    }

    public void add(CostRecord record) {
        tableData.add(record);
        refreshPanel();
    }

    public void edit(CostRecord record) {
        refreshPanel();
    }

}

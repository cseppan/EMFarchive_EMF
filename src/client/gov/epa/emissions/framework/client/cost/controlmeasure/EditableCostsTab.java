package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

    public EditableCostsTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel, DesktopManager desktopManager) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = null;
        this.desktopManager = desktopManager;
        doLayout(measure);
    }

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
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(CostRecord[] costRecords) {
        mainPanel.removeAll();
        initModel(costRecords);
        JScrollPane pane = sortFilterPane(parentConsole);// FIXME: pass the parentConsol
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
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }

    private JPanel controlPanel() {
        Button addButton = new Button("Add", addAction());
        Button removeButton = new Button("Remove", removeAction());
        Button editButton = new Button("Edit", removeAction());
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
        CostRecord[] records = (CostRecord[])selectModel.selected().toArray(new CostRecord[0]);
        tableData.remove(records);
        refreshPanel();
    }
    
    protected void doAdd() {
        CostRecordView view = new CostRecordWindow(desktopManager);
        CostRecordPresenter presenter = new CostRecordPresenter(this, view);
        presenter.display(measure);
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

}

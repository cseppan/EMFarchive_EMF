package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.EmfTableModel;
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

public class EditableEfficiencyTab extends JPanel implements EditableEfficiencyTabView {

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    private EmfTableModel model;

    private ControlMeasureEfficiencyTableData tableData;

    private JPanel mainPanel;

    private ControlMeasure measure;

    private DesktopManager desktopManager;

    private ManageChangeables changeablesList;

    private MessagePanel messagePanel;

    public EditableEfficiencyTab(ControlMeasure measure, ManageChangeables changeablesList, EmfConsole parent,
            DesktopManager desktopManager, MessagePanel messagePanel) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = parent;
        this.changeablesList = changeablesList;
        this.desktopManager = desktopManager;
        this.messagePanel = messagePanel;
        doLayout(measure);
    }

    private void doLayout(ControlMeasure measure) {
        this.measure = measure;
        EfficiencyRecord[] records = measure.getEfficiencyRecords();
        updateMainPanel(records);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(EfficiencyRecord[] records) {
        mainPanel.removeAll();
        initModel(records);
        JScrollPane pane = sortFilterPane(parentConsole);
        mainPanel.add(pane);
        mainPanel.validate();
    }

    private void initModel(EfficiencyRecord[] costRecords) {
        tableData = new ControlMeasureEfficiencyTableData(costRecords);
        model = new EmfTableModel(tableData);
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
        String[] columnNames = { "Pollutant", "Percent Reduction"};
        return new SortCriteria(columnNames, new boolean[] { true, true }, new boolean[] { true, true });
    }

    private JPanel controlPanel() {
        Button addButton = new Button("Add", addAction());
        Button editButton = new Button("Edit", editAction());
        Button removeButton = new Button("Remove", removeAction());

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(editButton);
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action editAction() {
        // NOTE Auto-generated method stub
        return null;
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
        
        EfficiencyRecord[] records = getSelectedRecords();

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
        EfficiencyRecordView view = new EfficiencyRecordWindow(changeablesList, desktopManager);
        EfficiencyRecordPresenter presenter = new EfficiencyRecordPresenter(this, view);
        presenter.display(measure, null);
    }

    private EfficiencyRecord[] getSelectedRecords() {
        return (EfficiencyRecord[]) selectModel.selected().toArray(new EfficiencyRecord[0]);
    }

    public void refreshData() {
        tableData.refresh();
    }

    public void refreshPanel() {
        refreshData();
        updateMainPanel(tableData.sources());
    }

    public void save(ControlMeasure measure) {
        measure.setEfficiencyRecords(tableData.sources());
    }

    public void add(EfficiencyRecord record) {
        tableData.add(record);
        refreshPanel();
    }

    public void edit(EfficiencyRecord record) {
        refreshPanel();
    }

}

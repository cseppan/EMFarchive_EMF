package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.EmfTableModel;
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

    private EmfTableModel model;
    
    private ControlMeasureCostTableData tableData;

    private JPanel mainPanel;

    public EditableCostsTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = null;
        doLayout(measure);
    }

    private void doLayout(ControlMeasure measure) {
        ControlMeasureCost cost = measure.getCost();
        CostRecord[] costRecords = null;
        if (cost != null)
            costRecords = cost.getCostRecords();
        else
            costRecords = new CostRecord[0];
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
    }

    private void initModel(CostRecord[] costRecords) {
        tableData = new ControlMeasureCostTableData(costRecords);
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
    }

    private JScrollPane sortFilterPane(EmfConsole parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("controlMeasureSccTable");
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }

    private JPanel controlPanel() {
        Button importButton = new Button("Import", importAction());
        Button addButton = new Button("Add", addAction());
        Button removeButton = new Button("Remove", removeAction());

        JPanel panel = new JPanel();
        panel.add(importButton);
        panel.add(addButton);
        panel.add(removeButton);
        return panel;
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
                tableData.add(new CostRecord());
                selectModel.refresh();
            }
        };
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // remove selected data
            }
        };
    }

    public void save(ControlMeasure measure) {
        //
    }

}

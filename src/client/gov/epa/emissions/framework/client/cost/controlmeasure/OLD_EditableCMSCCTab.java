package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class OLD_EditableCMSCCTab extends JPanel implements EditableCMTabView {

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    private EditableEmfTableModel model;

    private JPanel mainPanel;

    private SCCTableData tableData;

    public OLD_EditableCMSCCTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel) {
        this.mainPanel = new JPanel(new BorderLayout());
        this.parentConsole = null;
        doLayout(measure);
    }

    private void doLayout(ControlMeasure measure) {
        String[] sccs = measure.getSccs();
        updateMainPanel(sccs);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(controlPanel(), BorderLayout.SOUTH);
    }

    private void updateMainPanel(String[] sccs) {
        mainPanel.removeAll();
        initModel(sccs);
        JScrollPane pane = sortFilterPane(parentConsole);// FIXME: pass the parentConsol
        mainPanel.add(pane);
    }

    private void updateMainPanel() {
        mainPanel.removeAll();
        JScrollPane pane = sortFilterPane(parentConsole);// FIXME: pass the parentConsol
        mainPanel.add(pane);
    }

    private void initModel(String[] sccs) {
        tableData = new SCCTableData(sccs);
        model = new EditableEmfTableModel(tableData);
    }

    private JScrollPane sortFilterPane(EmfConsole parentConsole) {
        selectModel = new SortFilterSelectModel(model);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("controlMeasureSccTable");
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }

    private JPanel controlPanel() {
        Button addButton = new Button("Add", addAction());
        Button removeButton = new Button("Remove", removeAction());

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(removeButton);
        return panel;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tableData.addBlankRow();
                model.refresh();
                updateMainPanel();
                mainPanel.revalidate();
            }
        };
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tableData.removeSelected();
            }
        };
    }

    public void save(ControlMeasure measure) {
        //        
    }

}

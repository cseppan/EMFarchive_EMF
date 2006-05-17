package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditableSCCTab extends JPanel implements EditableCMTabView {

    private SortFilterSelectModel selectModel;

    private EmfConsole parentConsole;

    private EmfTableModel model;

    private JPanel mainPanel;

    public EditableSCCTab(ControlMeasure measure, EmfSession session, MessagePanel messagePanel) {
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

    private void initModel(String[] sccs) {
        model = new EmfTableModel(new SCCTableData(sccs));
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

        JPanel panel = new JPanel();
        panel.add(addButton);
        panel.add(removeButton);
        return panel;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // bring the scc reference tables
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

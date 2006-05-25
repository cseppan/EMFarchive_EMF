package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

public class EditableCMSCCTab extends JPanel implements EditableCMTabView, CMSCCTab {

    private SCCTableData tableData;

    private EmfConsole parent;

    private EmfTableModel tableModel;

    private SortFilterSelectModel sortFilterSelectModel;

    private MessagePanel messagePanel;

    private JPanel mainPanel;

    private EmfSession session;

    public EditableCMSCCTab(ControlMeasure measure, EmfSession session, ManageChangeables changeables,
            MessagePanel messagePanel, EmfConsole parent) throws EmfException {
        this.parent = parent;
        this.messagePanel = messagePanel;
        this.session = session;
        this.parent = parent;
        mainPanel = new JPanel(new BorderLayout());
        doLayout(measure, changeables);
    }

    private void doLayout(ControlMeasure measure, ManageChangeables changeables) throws EmfException {
        Scc[] sccObjs = createSccs(measure);
        SortFilterSelectionPanel sortFilterSelectionPanel = sortFilterPanel(sccObjs);
        mainPanel.removeAll();
        mainPanel.add(sortFilterSelectionPanel);

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel(), BorderLayout.SOUTH);
    }

    private SortFilterSelectionPanel sortFilterPanel(Scc[] sccObjs) {
        tableData = new SCCTableData(sccObjs);
        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new SortFilterSelectModel(tableModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parent, sortFilterSelectModel);
        return sortFilterSelectionPanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new Button("Add", addAction()));
        panel.add(new Button("Remove", removeAction()));
        return panel;
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                selectionView();
            }
        };
    }

    private void selectionView() {
        SCCSelectionView view = new SCCSelectionDialog(parent);
        SCCSelectionPresenter presenter = new SCCSelectionPresenter(EditableCMSCCTab.this, view);
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
        messagePanel.setError("Under construction");
    }

    private Scc[] createSccs(ControlMeasure measure) throws EmfException {
        CostService service = session.costService();
        Scc[] sccs = service.getSccs(measure);
        
        return sccs;
    }

    public void save(ControlMeasure measure) {
        List sccs = tableData.rows();
        String[] newSccs = new String[sccs.size()];
        for (int i = 0; i < sccs.size(); i++) {
            ViewableRow row = (ViewableRow) sccs.get(i);
            Scc scc = (Scc) row.source();
            newSccs[i] = scc.getCode();
        }
        measure.setSccs(newSccs);
    }

    public void add(Scc[] sccs) {
        SortFilterSelectionPanel panel = sortFilterPanel(sccs);
        mainPanel.removeAll();
        mainPanel.add(panel);
    }

}

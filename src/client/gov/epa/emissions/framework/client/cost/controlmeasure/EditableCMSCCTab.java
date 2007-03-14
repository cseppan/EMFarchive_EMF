package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditableCMSCCTab extends JPanel implements ControlMeasureSccTabView, CMSCCTab, Runnable {

    private SCCTableData tableData;

    private EmfConsole parent;

    private EmfTableModel tableModel;

    private TrackableSortFilterSelectModel sortFilterSelectModel;

    private MessagePanel messagePanel;

    private JPanel mainPanel;

    private EmfSession session;

    private ManageChangeables changeables;

    private Scc[] sccs = {};
    private volatile Thread populateThread;
    private ControlMeasure measure;
    private ControlMeasurePresenter controlMeasurePresenter;

    public EditableCMSCCTab(ControlMeasure measure, EmfSession session, ManageChangeables changeables,
            MessagePanel messagePanel, EmfConsole parent,
            ControlMeasurePresenter controlMeasurePresenter) {
        this.parent = parent;
        this.messagePanel = messagePanel;
        this.session = session;
        this.parent = parent;
        this.measure = measure;
        this.changeables = changeables;
        this.controlMeasurePresenter = controlMeasurePresenter;
        mainPanel = new JPanel(new BorderLayout());
        doLayout(measure, changeables);
        this.populateThread = new Thread(this);
        populateThread.start();
    }

    public void run() {
        if (measure.getId() != 0) {
            try {
                messagePanel.setMessage("Please wait while retrieving all SCCs...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    sccs = getSCCs(measure.getId());
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }        
                doLayout(measure, changeables);
                messagePanel.clear();
                setCursor(Cursor.getDefaultCursor());
            } catch (Exception e) {
                messagePanel.setError("Cannot retrieve all SCCs.");
            }
        }
        this.populateThread = null;
    }

    private void doLayout(ControlMeasure measure, ManageChangeables changeables) {
        this.changeables = changeables;
        try {
            tableData = new SCCTableData(sccs);
            SortFilterSelectionPanel sortFilterSelectionPanel = sortFilterPanel();
            mainPanel.removeAll();
            mainPanel.add(sortFilterSelectionPanel);
        } catch (Exception e) {
            messagePanel.setError(e.getMessage());
        }

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel(), BorderLayout.SOUTH);
    }

    private SortFilterSelectionPanel sortFilterPanel() {
        tableModel = new EmfTableModel(tableData);
        sortFilterSelectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(sortFilterSelectModel);
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parent, sortFilterSelectModel);
        return sortFilterSelectionPanel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        Button addButton = new AddButton(addAction());
        panel.add(addButton);
        Button removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

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
        SCCSelectionView view = new SCCSelectionDialog(parent, changeables);
        SCCSelectionPresenter presenter = new SCCSelectionPresenter(this, view);
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
            
        Scc[] records = (Scc[]) selected.toArray(new Scc[0]);

        if (records.length == 0)
            return;

        String title = "Warning";
        String message = "Are you sure you want to remove the selected row(s)?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            tableData.remove(records);
            SortFilterSelectionPanel panel = sortFilterPanel();
            mainPanel.removeAll();
            mainPanel.add(panel);
            modify();
        }
    }

    private Scc[] getSCCs(int controlMeasureId) throws EmfException {
        ControlMeasureService service = session.controlMeasureService();
        Scc[] sccs = service.getSccsWithDescriptions(controlMeasureId);

        return sccs;
    }

    public void save(ControlMeasure measure) {
        List sccsList = tableData.rows();
        sccs = new Scc[sccsList.size()];
        for (int i = 0; i < sccsList.size(); i++) {
            ViewableRow row = (ViewableRow) sccsList.get(i);
            Scc scc = (Scc) row.source();
            sccs[i] = new Scc(scc.getCode(), "");
        }
    }

    public void add(Scc[] sccs) {
        for (int i = 0; i < sccs.length; i++) {
            tableData.add(sccs);
        }

        SortFilterSelectionPanel panel = sortFilterPanel();
        mainPanel.removeAll();
        mainPanel.add(panel);
        modify();
    }

    public Scc[] sccs() {
        List sccsList = tableData.rows();
        sccs = new Scc[sccsList.size()];
        for (int i = 0; i < sccsList.size(); i++) {
            ViewableRow row = (ViewableRow) sccsList.get(i);
            Scc scc = (Scc) row.source();
            sccs[i] = new Scc(scc.getCode(), "");
        }
        return sccs;
    }

    public void refresh(ControlMeasure measure) {
        this.measure = measure;
    }

    public void modify() {
        controlMeasurePresenter.doModify();
    }

}

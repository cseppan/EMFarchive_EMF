package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ControlMeasuresManagerWindow extends ReusableInteralFrame implements ControlMeasuresManagerView, RefreshObserver {

    private SortFilterSelectModel selectModel;

    private MessagePanel messagePanel;

    private ControlMeasuresManagerPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EmfTableModel model;

    private JPanel browserPanel;

    public ControlMeasuresManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager)
            throws EmfException {
        super("Control Measures Manager", new Dimension(850, 450), desktopManager);
        super.setName("controlMeasures");
        this.session = session;
        this.parentConsole = parentConsole;

        CostService service = session.costService();
        model = new EmfTableModel(new ControlMeasureTableData(service.getMeasures()));
        selectModel = new SortFilterSelectModel(model);

        this.getContentPane().add(createLayout(parentConsole));
    }

    private JPanel createLayout(EmfConsole parentConsole) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(createBrowserPanel(parentConsole), BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBrowserPanel(EmfConsole parentConsole) {
        browserPanel = new JPanel(new BorderLayout());
        browserPanel.add(sortFilterPane(parentConsole));

        return browserPanel;
    }

    private JScrollPane sortFilterPane(EmfConsole parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.sort(sortCriteria());
        panel.getTable().setName("controlMeasuresTable");
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "AnnualizedCost" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Control Measures", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createLeftControlPanel(), BorderLayout.LINE_START);

        return controlPanel;
    }

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        Button newControlMeasure = new Button("New", newControlMeasureAction());
        panel.add(newControlMeasure);
        
        Button copy = new Button("Copy", copyAction());
        panel.add(copy);
        
        Button edit = new Button("Edit", editAction());
        panel.add(edit);
        
        Button view = new Button("View", viewAction());
        panel.add(view);

        return panel;
    }

    private Action viewAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO:
            }
        };
        
        return action;
    }

    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO:
            }
        };
        
        return action;
    }

    private Action newControlMeasureAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO:
            }
        };
        
        return action;
    }

    private Action copyAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO:
            }
        };
        
        return action;
    }

    private List getSelectedMeasures() {
        return selectModel.selected();
    }

    protected void doDisplayPropertiesViewer() {

        List measures = updateSelectedMeasures(getSelectedMeasures());
        if (measures.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Measures");
            return;
        }
    }

    private List updateSelectedMeasures(List selectedMeasures) {
        // FIXME: update only control measures that user selected
        List updatedMeasures = new ArrayList();
        try {
            ControlMeasure[] updatedAllMeasures1 = session.costService().getMeasures();
            for (int i = 0; i < selectedMeasures.size(); i++) {
                ControlMeasure selMeasure = (ControlMeasure) selectedMeasures.get(i);
                for (int j = 0; j < updatedAllMeasures1.length; j++) {
                    if (selMeasure.getId() == updatedAllMeasures1[j].getId()) {
                        updatedMeasures.add(updatedAllMeasures1[j]);
                        break;
                    }
                }
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        }
        return updatedMeasures;
    }

    public void showError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
    }

    public void observe(ControlMeasuresManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(ControlMeasure[] measures) {
        model.refresh(new ControlMeasureTableData(measures));
        selectModel.refresh();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        browserPanel.removeAll();
        browserPanel.add(sortFilterPane(parentConsole));
        super.refreshLayout();
    }

    public void showMessage(String message) {
        messagePanel.setMessage(message);
        super.refreshLayout();
    }

    public void clearMessage() {
        messagePanel.clear();
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

}

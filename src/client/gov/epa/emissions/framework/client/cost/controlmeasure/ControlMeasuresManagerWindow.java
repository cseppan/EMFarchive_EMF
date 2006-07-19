package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.*;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ControlMeasuresManagerWindow extends ReusableInteralFrame implements ControlMeasuresManagerView,
        RefreshObserver {

    private SortFilterSelectModel selectModel;

    private MessagePanel messagePanel;

    private ControlMeasuresManagerPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EmfTableModel model;

    private ControlMeasureTableData tableData;

    private JPanel browserPanel;

    private ComboBox pollutant;

    private EditableComboBox costYear;

    private DesktopManager desktopManager;

    private String[] pollutants = { "Major", "NOx", "PM10", "PM2.5", "SO2", "VOC", "CO", "CO2", "EC", "OC", "NH3", "Hg" };

    private String[] years = { "Default", "1999", "2000", "2001", "2002", "2003", "2004", "2005", "2006" };

    public ControlMeasuresManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager)
            throws EmfException {
        super("Control Measures Manager", new Dimension(825, 350), desktopManager);
        super.setName("controlMeasures");
        super.setMinimumSize(new Dimension(10, 10));
        this.session = session;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        ControlMeasureService service = session.costService();
        tableData = new ControlMeasureTableData(service.getMeasures());
        model = new EmfTableModel(tableData);
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
        panel.setPreferredSize(new Dimension(550, 120));

        return new JScrollPane(panel);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
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
        controlPanel.add(createRightControlPanel(), BorderLayout.LINE_END);

        return controlPanel;
    }

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        Button view = new ViewButton("View", viewAction());
        panel.add(view);

        Button edit = new EditButton("Edit", editAction());
        panel.add(edit);

        Button copy = new CopyButton("Copy", copyAction());
        panel.add(copy);
        copy.setEnabled(false);

        Button newControlMeasure = new NewButton("New", newControlMeasureAction());
        panel.add(newControlMeasure);

        return panel;
    }

    private Component createRightControlPanel() {
        JPanel panel = new JPanel();

        pollutant = new ComboBox(pollutants);
        pollutant.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getEfficiencyAndCost();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        panel.add(getItem("Pollutant:", pollutant));

        costYear = new EditableComboBox(years);
        costYear.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    getEfficiencyAndCost();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });
        panel.add(getItem("Cost Year:", costYear));

        Button importButton = new Button("Import", viewAction());
        panel.add(importButton);
        importButton.setEnabled(false);

        Button closeButton = new CloseButton("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        panel.add(closeButton);

        return panel;
    }

    private Component getItem(String label, JComboBox box) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        box.setPreferredSize(new Dimension(80, 25));
        layoutGenerator.addLabelWidgetPair(label, box, panel);
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                1, 1, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private Action viewAction() {
        return getAction();
    }

    private Action editAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                ControlMeasure[] measures = (ControlMeasure[]) getSelectedMeasures().toArray(new ControlMeasure[0]);
                if (measures.length == 0)
                    showError("Please select a control measure.");
                try {
                    for (int i = 0; i < measures.length; i++)
                        presenter.doEdit(parentConsole, measures[i], desktopManager);

                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        };
        return action;
    }

    private Action newControlMeasureAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    displayNewCMWindow();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        };
        return action;
    }

    private void displayNewCMWindow() throws EmfException {
        clearMessage();
        ControlMeasure measure = new ControlMeasure();
        measure.setCreator(session.user());
        presenter.doCreateNew(parentConsole, measure, desktopManager);
    }

    private Action copyAction() {
        return getAction();
    }

    private Action getAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                actions();
            }
        };
        return action;
    }

    private void actions() {
        clearMessage();

        ControlMeasure[] measures = (ControlMeasure[]) getSelectedMeasures().toArray(new ControlMeasure[0]);
        if (measures.length == 0)
            showError("Please select a control measure.");

        try {
            for (int i = 0; i < measures.length; i++) {
                presenter.doEdit(parentConsole, measures[i], desktopManager);
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        }
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

    public void getEfficiencyAndCost() throws EmfException {
        tableData.setPollutantAndYear(pollutant.getSelectedItem().toString().trim(), costYear.getSelectedItem()
                .toString().trim());
        doRefresh();
    }

}

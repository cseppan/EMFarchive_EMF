package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMImportPresenter;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMImportView;
import gov.epa.emissions.framework.client.cost.controlmeasure.io.CMImportWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class ControlMeasuresManagerWindow extends ReusableInteralFrame implements ControlMeasuresManagerView,
        RefreshObserver, Runnable {

    private SortFilterSelectModel selectModel;

    private MessagePanel messagePanel;

    private ControlMeasuresManagerPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EmfTableModel model;

    private ControlMeasureTableData tableData;

    private ComboBox pollutant;

    private ComboBox majorPollutant;

    private EditableComboBox costYear;

    private DesktopManager desktopManager;

    private Pollutant[] pollutants;

    private JPanel mainPanel;

    private Pollutant[] pollsFromDB;

    private String[] years = { "1999", "2000", "2001", "2002", "2003", "2004", "2005" };

    private CostYearTable costYearTable;

    private YearValidation yearValidation;

    private volatile Thread populateThread;

    private String threadAction;
    
    private Button copyButton;
    
    private Button refreshButton;
    
    public ControlMeasuresManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Control Measure Manager", new Dimension(855, 350), desktopManager);
        super.setName("controlMeasures");
        super.setMinimumSize(new Dimension(10, 10));
        this.session = session;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    public void run() {
        if (this.threadAction == "refresh") {
            try {
                copyButton.setEnabled(false);
                refreshButton.setEnabled(false);
                messagePanel.setMessage("Please wait while retrieving control measures...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                refresh(new ControlMeasure[0]);
                refresh(presenter.getControlMeasures(getSelectedMajorPollutant()));
                messagePanel.clear();
            } catch (Exception e) {
                messagePanel.setError("Cannot retrieve control measures.  " + e.getMessage());
            } finally  {
                copyButton.setEnabled(true);
                refreshButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                this.populateThread = null;
            }
        } else if (this.threadAction == "copy") {
            try {
                copyButton.setEnabled(false);
                refreshButton.setEnabled(false);
                messagePanel.setMessage("Please wait while copying control measures...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                copySelectedControlMeasures();
                this.populateThread = null;
                doRefresh();
                messagePanel.clear();
            } catch (Exception e) {
                messagePanel.setError("Cannot copy control measures.  " + e.getMessage());
            } finally  {
                copyButton.setEnabled(true);
                refreshButton.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                this.populateThread = null;
            }
        }
    }

    public void display(ControlMeasure[] measures) throws EmfException {
        yearValidation = new YearValidation("Cost Year");
        costYearTable = presenter.getCostYearTable();
        getAllPollutants(this.session);
        createPollutantComboBox();
        createAllPollutantsComboBox();
        createYearsComboBox();

        doLayout(this.parentConsole, measures);
        this.messagePanel.setMessage("Please select a major pollutant to retrieve related control measures.");
        super.display();
    }

    private void doLayout(EmfConsole parentConsole, ControlMeasure[] measures) throws EmfException {
        setupTableModel(measures);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(mainPanel(parentConsole), BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        this.getContentPane().add(panel);
    }

    private JPanel mainPanel(EmfConsole parentConsole) {
        this.mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JScrollPane sortFilterPane = sortFilterPane(parentConsole);
        mainPanel.add(sortFilterPane);

        return mainPanel;
    }

    private void setupTableModel(ControlMeasure[] measures) throws EmfException {
        tableData = new ControlMeasureTableData(measures, costYearTable, selectedPollutant(), selectedCostYear());
        model = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(model);
    }

    private void getAllPollutants(EmfSession session) throws EmfException {
        Pollutant[] all = getPollutants(session);
        List dbPollList = new ArrayList();
        // dbPollList.add(new Pollutant("Select one"));
        dbPollList.add(new Pollutant("All"));
        dbPollList.addAll(Arrays.asList(all));
        pollsFromDB = (Pollutant[]) dbPollList.toArray(new Pollutant[0]);

        dbPollList.clear();
        dbPollList.add(new Pollutant("Major"));
        dbPollList.addAll(Arrays.asList(all));
        pollutants = (Pollutant[]) dbPollList.toArray(new Pollutant[0]);
    }

    private Pollutant[] getPollutants(EmfSession session) throws EmfException {
        return session.dataCommonsService().getPollutants();
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
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(getItem("Major Pollutant:", majorPollutant), BorderLayout.WEST);
        panel.setBorder(BorderFactory.createEmptyBorder(4,5,4,4));
        majorPollutant.setPreferredSize(new Dimension(100, 30));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        refreshButton = new RefreshButton(this, "Refresh Control Measures", messagePanel);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private void createPollutantComboBox() {
        majorPollutant = new ComboBox("Select one", pollsFromDB);
        majorPollutant.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRefresh();
            }
        });
    }

    private Pollutant getSelectedMajorPollutant() {
        Object selected = majorPollutant.getSelectedItem();

        if (selected == null)
            return new Pollutant("Select one");

        return (Pollutant) selected;
    }

    protected void disPlayControlMeasures(Pollutant pollutant) throws EmfException {
        refresh(presenter.getControlMeasures(pollutant));
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createLeftControlPanel(), BorderLayout.LINE_START);
        controlPanel.add(createRightControlPanel(), BorderLayout.LINE_END);

        return controlPanel;
    }

    // Create View, Edit, copy, new buttons to panel
    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();
        
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        SelectAwareButton view = new SelectAwareButton("View", viewAction(), selectModel, confirmDialog);
//        Button view = new ViewButton(viewAction());
        panel.add(view);

        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), selectModel, confirmDialog);
        panel.add(edit);

        copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCopy();
            }
        });
        panel.add(copyButton);

        Button newControlMeasure = new NewButton(newControlMeasureAction());
        panel.add(newControlMeasure);

        return panel;
    }

    private Component createRightControlPanel() {
        JPanel panel = new JPanel();
        panel.add(getItem("Pollutant:", pollutant));
        panel.add(getItem("Cost Year:", costYear));
        pollutant.setPreferredSize(new Dimension(100, 30));

        Button importButton = new ImportButton(importAction());
        panel.add(importButton);

        Button exportButton = new ExportButton(exportAction());
        exportButton.setToolTipText("Export existing Control Measure(s)");
        panel.add(exportButton);

        Button closeButton = new CloseButton("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        panel.add(closeButton);

        return panel;
    }

    private void createAllPollutantsComboBox() {
        pollutant = new ComboBox(pollutants);
        pollutant.setSelectedIndex(0);
        pollutant.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getEfficiencyAndCost();
            }
        });
    }

    private void createYearsComboBox() {
        costYear = new EditableComboBox(years);
        costYear.setSelectedIndex(0);
        costYear.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getEfficiencyAndCost();
            }
        });
    }

    private Action importAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controlMeasureImport();
            }
        };
    }

    private Action exportAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                controlMeasureExport();
            }
        };
    }

    protected void controlMeasureImport() {
        CMImportView view = new CMImportWindow(parentConsole, desktopManager, session);
        CMImportPresenter presenter = new CMImportPresenter(session);
        presenter.display(view);

    }

    protected void controlMeasureExport() {
        clearMessage();
        List cmList = getSelectedMeasures();

        if (cmList.size() == 0) {
            showError("Please select a control measure.");
            return;
        }

        presenter.doExport((ControlMeasure[]) cmList.toArray(new ControlMeasure[0]), desktopManager, selectModel
                .getRowCount(), parentConsole);
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
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                ControlMeasure[] measures = (ControlMeasure[]) getSelectedMeasures().toArray(new ControlMeasure[0]);
                if (measures.length == 0)
                    showError("Please select a control measure.");
                try {
                    for (int i = 0; i < measures.length; i++)
                        presenter.doView(parentConsole, measures[i], desktopManager);

                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        };
        return action;
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

    protected void copySelectedControlMeasures() throws EmfException {
        messagePanel.clear();
        List cmList = getSelectedMeasures();
        if (cmList.isEmpty()) {
            messagePanel.setMessage("Please select one or more control measures.");
            return;
        }

        for (Iterator iter = cmList.iterator(); iter.hasNext();) {
            ControlMeasure element = (ControlMeasure) iter.next();

            try {
//                ControlMeasure coppied = (ControlMeasure) DeepCopy.copy(element);
//                coppied.setName("Copy of " + element.getName());
                presenter.doSaveCopiedControlMeasure(element.getId());
            } catch (EmfException e) {
                throw e;
            } catch (Exception e) {
                messagePanel.setError(e.getMessage());
            }
        }
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
            ControlMeasure[] updatedAllMeasures1 = session.controlMeasureService().getMeasures();
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

    public void showMessage(String message) {
        messagePanel.setMessage(message);
        super.refreshLayout();
    }

    public void clearMessage() {
        messagePanel.clear();
        super.refreshLayout();
    }

    public void doRefresh() {
        this.threadAction = "refresh";
        this.populateThread = new Thread(this);
        this.populateThread.start();
    }

    public void doCopy() {
        this.threadAction = "copy";
        this.populateThread = new Thread(this);
        this.populateThread.start();
    }

    public void refresh(ControlMeasure[] measures) {
//        clearMessage();
        try {
            setupTableModel(measures);
            panelRefresh();
        } catch (EmfException e) {
            messagePanel.setError("Error in refreshing current table: " + e.getMessage());
        }
    }

    public void getEfficiencyAndCost() {
        clearMessage();
        try {
            refreshTableData();
            panelRefresh();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void refreshTableData() throws EmfException {
        tableData.refresh(selectedPollutant(), selectedCostYear());
    }

    private void panelRefresh() {
        selectModel.refresh();
        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        mainPanel.removeAll();
        mainPanel.add(sortFilterPane(parentConsole));
        super.refreshLayout();
    }
    
    private String selectedCostYear() throws EmfException {
//        if (costYear == null) return null;
        String year = ((String) costYear.getSelectedItem()).trim();

        yearValidation.value(year, costYearTable.getStartYear(), costYearTable.getEndYear());
        return (String)costYear.getSelectedItem();
    }

    private Pollutant selectedPollutant() {
//        if (pollutant == null) return null;
        return (Pollutant) pollutant.getSelectedItem();
    }


}

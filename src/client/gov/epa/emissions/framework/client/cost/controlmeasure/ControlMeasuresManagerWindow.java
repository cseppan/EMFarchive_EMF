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
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.commons.io.DeepCopy;
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
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
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
import java.util.Iterator;
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

    private ComboBox majorPollutant;

    private EditableComboBox costYear;

    private DesktopManager desktopManager;

    private Pollutant[] pollutants;
    
    Pollutant [] pollsFromDB;

    private String[] years = { "1999", "2000", "2001", "2002", "2003", "2004", "2005", "2006" };

    private CostYearTable costYearTable;

    private YearValidation yearValidation;

    public ControlMeasuresManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager)
            throws EmfException {
        super("Control Measure Manager", new Dimension(825, 350), desktopManager);
        super.setName("controlMeasures");
        super.setMinimumSize(new Dimension(10, 10));
        this.session = session;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        getAllPollutants(this.session);
        ControlMeasureService service = session.controlMeasureService();
        costYearTable = service.getCostYearTable(1999);
        yearValidation = new YearValidation("Cost Year");
        tableData = new ControlMeasureTableData(new ControlMeasure[0], costYearTable, pollutants[0].getName(), years[0]);
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
    
    private void getAllPollutants(EmfSession session) throws EmfException {
        pollsFromDB = getPollutants(session);
        
        pollutants = new Pollutant[pollsFromDB.length+1];
        pollutants[0] = new Pollutant("Major");
        for (int p = 1; p < pollutants.length; p++)
        {
            pollutants[p] = pollsFromDB[p-1];
        }
     }

    private Pollutant[] getPollutants(EmfSession session) throws EmfException {
        return session.dataCommonsService().getPollutants();
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
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        majorPollutant = new ComboBox(pollsFromDB);
        majorPollutant.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Pollutant pol = getSelectedMajorPollutant();
                try {
                    disPlayControlMeasures(pol);
                } catch (EmfException e1) {
                    messagePanel.setError("Could not retrieve all control measures with major pollutant -- " +
                            pol.getName());
                }
            }
        });
        panel.add(getItem("Major Pollutant:", majorPollutant), BorderLayout.LINE_START);
        
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Control Measures", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private Pollutant getSelectedMajorPollutant() {
        return (Pollutant)majorPollutant.getSelectedItem();
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

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        Button view = new ViewButton(null);
        panel.add(view);
        view.setEnabled(false);

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton edit = new SelectAwareButton("Edit", editAction(), selectModel, confirmDialog);
        panel.add(edit);

        Button copy = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copySelectedControlMeasures();
            }
        });
        panel.add(copy);

        Button newControlMeasure = new NewButton(newControlMeasureAction());
        panel.add(newControlMeasure);

        return panel;
    }

    private Component createRightControlPanel() {
        JPanel panel = new JPanel();

        pollutant = new ComboBox(pollutants);
        pollutant.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getEfficiencyAndCost();
            }
        });
        panel.add(getItem("Pollutant:", pollutant));

        costYear = new EditableComboBox(years);
        costYear.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getEfficiencyAndCost();
            }
        });
        panel.add(getItem("Cost Year:", costYear));

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
        CMImportView view = new CMImportWindow(desktopManager);
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
        
        presenter.doExport((ControlMeasure[])cmList.toArray(new ControlMeasure[0]), 
                desktopManager, selectModel.getRowCount());
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
    
    protected void copySelectedControlMeasures() {
        messagePanel.clear();
        List cmList = getSelectedMeasures();
        if (cmList.isEmpty()) {
            messagePanel.setMessage("Please select one or more control measures.");
            return;
        }
        
        for (Iterator iter = cmList.iterator(); iter.hasNext();) {
            ControlMeasure element = (ControlMeasure) iter.next();
            
            try {
                ControlMeasure coppied = (ControlMeasure)DeepCopy.copy(element);
                coppied.setName("Copy of " + element.getName());
                presenter.doSaveCopiedControlMeasure(coppied, element);
                doRefresh();
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

    public void refresh(ControlMeasure[] measures) {
        try {
            tableData = new ControlMeasureTableData(measures, costYearTable, pollutant.getSelectedItem().toString(),
                    (String) costYear.getSelectedItem());
            model.refresh(tableData);
            panelRefresh();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void panelRefresh() {
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
        disPlayControlMeasures(getSelectedMajorPollutant());
    }

    public void getEfficiencyAndCost() {
        clearMessage();
        try {
            String year = ((String) costYear.getSelectedItem()).trim();
            yearValidation.value(year);
            tableData.refresh(pollutant.getSelectedItem().toString(), year);
            panelRefresh();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

}

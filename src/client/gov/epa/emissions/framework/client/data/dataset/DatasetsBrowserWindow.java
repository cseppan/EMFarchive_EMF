package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserAwareImportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenterImpl;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DatasetsBrowserWindow extends ReusableInteralFrame implements DatasetsBrowserView, RefreshObserver {

    private MessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private ComboBox dsTypesBox;

    private DatasetType[] allDSTypes;

    private SelectableSortFilterWrapper table;

    public DatasetsBrowserWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Manager", new Dimension(850, 450), desktopManager);
        super.setName("datasetsBrowser");
        this.session = session;
        this.parentConsole = parentConsole;
    }

    public void display(EmfDataset[] datasets) throws EmfException {
        getAllDSTypes();
        createDSTypesComboBox();
        JPanel panel = createLayout(datasets);
        Container contentPane = getContentPane();
        contentPane.add(panel);
        super.display();
    }

    private void getAllDSTypes() throws EmfException {
        List<DatasetType> dbDSTypes = new ArrayList<DatasetType>();
        dbDSTypes.add(new DatasetType("All"));
        dbDSTypes.addAll(Arrays.asList(presenter.getDSTypes()));
        this.allDSTypes = dbDSTypes.toArray(new DatasetType[0]);
    }

    private void createDSTypesComboBox() {
        dsTypesBox = new ComboBox("Select one", allDSTypes);
        dsTypesBox.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                DatasetType type = getSelectedDSType();
                try {
                    doRefresh();
                } catch (EmfException e1) {
                    messagePanel.setError("Could not retrieve all dataset types with -- " + type.getName());
                }
            }
        });
    }

    private DatasetType getSelectedDSType() {
        Object selected = dsTypesBox.getSelectedItem();

        if (selected == null)
            return new DatasetType("Select one");

        return (DatasetType) selected;
    }

    private JPanel createLayout(EmfDataset[] datasets) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(createBrowserPanel(datasets), BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBrowserPanel(EmfDataset[] datasets) {
        EmfDatasetTableData tableData = new EmfDatasetTableData(datasets);
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());

        JPanel browserPanel = new JPanel(new BorderLayout());
        browserPanel.add(table);

        return browserPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Datasets", messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(getDSTypePanel("Show Datasets of Type:", dsTypesBox));

        return panel;
    }

    private JPanel getDSTypePanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 150, 5, 150));

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createRightControlPanel(), BorderLayout.LINE_END);
        controlPanel.add(createLeftControlPanel(), BorderLayout.LINE_START);

        return controlPanel;
    }

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        SelectAwareButton propButton = new SelectAwareButton("Edit Properties", editPropAction(), table, confirmDialog);
        SelectAwareButton dataButton = new SelectAwareButton("Edit Data", editDataAction(), table, confirmDialog);
        Button removeButton = new RemoveButton(removeAction());

        dataButton.setMnemonic('a');

        panel.add(viewButton);
        panel.add(propButton);
        panel.add(dataButton);
        panel.add(removeButton);

        return panel;
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                doDisplayPropertiesViewer();
            }
        };
    }

    private Action editPropAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                doDisplayPropertiesEditor();
            }
        };
    }

    private Action editDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                doDisplayVersionedData();
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

    protected void importDataset() throws EmfException {
        ImportWindow importView = new ImportWindow(session.dataCommonsService(), desktopManager, parentConsole);

        ImportPresenter importPresenter = new DatasetsBrowserAwareImportPresenter(session, session.user(), session
                .eximService(), session.dataService(), this);
        presenter.doImport(importView, importPresenter);
    }

    private JPanel createRightControlPanel() {
        JPanel panel = new JPanel();

        Button importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    importDataset();
                } catch (EmfException e) {
                    showError("Could not open Import window (for creation of a new dataset)");
                }
            }
        });
        importButton.setToolTipText("Import a new Dataset");
        panel.add(importButton);

        Button exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        exportButton.setToolTipText("Export existing Dataset(s)");
        panel.add(exportButton);

        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        panel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        return panel;
    }

    protected void exportSelectedDatasets() {
        // NOTE: get selected dataset will give you all the dataset selected in the base model of sort filter table
        // model
//        EmfDataset[] emfDatasets = getNonExternalDatasets(getSelectedDatasets()); //export external datasets allowed now
        EmfDataset[] emfDatasets = getSelectedDatasets().toArray(new EmfDataset[0]);

        ExportWindow exportView = new ExportWindow(emfDatasets, desktopManager, parentConsole, session);
        getDesktopPane().add(exportView);

        ExportPresenter exportPresenter = new ExportPresenterImpl(session);
        presenter.doExport(exportView, exportPresenter, emfDatasets);
    }

    private List<EmfDataset> getSelectedDatasets() {
        return (List<EmfDataset>) table.selected();
    }

    protected void doDisplayPropertiesViewer() {
        clearMessage();
        // for now, don't get updated copies of datasets - see updateSelectedDatasets
        // List datasets = updateSelectedDatasets(getSelectedDatasets());
        List datasets = getSelectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
            EmfDataset dataset = (EmfDataset) iter.next();
            presenter.doDisplayPropertiesView(view, dataset);
        }
    }

    // private List updateSelectedDatasets(List selectedDatasets) {
    // // FIXME: update only datasets that user selected
    // List updatedDatasets = new ArrayList();
    // try {
    // EmfDataset[] updatedAllDatasets1 = session.dataService().getDatasets();
    // for (int i = 0; i < selectedDatasets.size(); i++) {
    // EmfDataset selDataset = (EmfDataset) selectedDatasets.get(i);
    // for (int j = 0; j < updatedAllDatasets1.length; j++) {
    // if (selDataset.getId() == updatedAllDatasets1[j].getId()) {
    // updatedDatasets.add(updatedAllDatasets1[j]);
    // break;
    // }
    // }
    // }
    // } catch (EmfException e) {
    // showError(e.getMessage());
    // }
    // return updatedDatasets;
    // }

    protected void doDisplayPropertiesEditor() {
        clearMessage();
        List datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            EmfDataset dataset = (EmfDataset) iter.next();
            DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);
            try {
                presenter.doDisplayPropertiesEditor(view, dataset);
            } catch (EmfException e) {
                showError(e.getMessage());
            }
        }
    }

    protected void doDisplayVersionedData() {
        clearMessage();
        List datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            VersionedDataWindow view = new VersionedDataWindow(parentConsole, desktopManager);
            presenter.doDisplayVersionedData(view, (EmfDataset) iter.next());
        }
    }

    private void doRemove() {
        clearMessage();
        List<?> datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }

        String message = "Are you sure you want to remove the selected " + datasets.size() + " dataset(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doDeleteDataset(datasets.toArray(new EmfDataset[0]));
                messagePanel.setMessage(datasets.size()
                        + " datasets have been removed. Please Refresh to see the revised list of Datasets.");
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    public void showError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
    }

    public void observe(DatasetsBrowserPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(EmfDataset[] datasets) {
        clearMessage();
        table.refresh(new EmfDatasetTableData(datasets));
        super.refreshLayout();
    }

    public void showMessage(String message) {
        messagePanel.setMessage(message);
        super.refreshLayout();
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {
        refresh(presenter.getEmfDatasets(getSelectedDSType()));
    }

    public void notifyLockFailure(EmfDataset dataset) {
        clearMessage();
        showError("Cannot obtain a lock for dataset \"" + dataset.getName() + "\".");
    }
}

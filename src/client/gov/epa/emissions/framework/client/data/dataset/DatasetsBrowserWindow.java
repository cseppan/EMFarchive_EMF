package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
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
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
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
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DatasetsBrowserWindow extends ReusableInteralFrame implements DatasetsBrowserView, RefreshObserver {

    private SortFilterSelectModel selectModel;

    private MessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EmfTableModel model;

    private JPanel browserPanel;

    public DatasetsBrowserWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager)
            throws EmfException {
        super("Dataset Manager", new Dimension(850, 450), desktopManager);
        super.setName("datasetsBrowser");
        this.session = session;
        this.parentConsole = parentConsole;

        DataService services = session.dataService();
        model = new EmfTableModel(new EmfDatasetTableData(services.getDatasets()));
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
        panel.getTable().setName("datasetsTable");
        panel.setPreferredSize(new Dimension(450, 120));

        return new JScrollPane(panel);
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Datasets", messagePanel);
        panel.add(button, BorderLayout.EAST);

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

        String[] options = { "View", "Edit Properties", "Edit Data" };
        DefaultComboBoxModel model = new DefaultComboBoxModel(options);
        final JComboBox combo = new JComboBox(model);
        combo.setEditable(false);
        combo.setPreferredSize(new Dimension(125, 25));
        combo.setToolTipText("Select one of the options and click Go to view/edit a Dataset");
        panel.add(combo);
        Action dataAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                String selected = (String) combo.getSelectedItem();
                selectedOption(selected);
            }
        };
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton goButton = new SelectAwareButton("Go", dataAction, selectModel, confirmDialog);
        panel.add(goButton);

        return panel;
    }

    private void selectedOption(String option) {
        if (option.equals("View"))
            doDisplayPropertiesViewer();

        if (option.equals("Edit Properties"))
            doDisplayPropertiesEditor();

        if (option.equals("Edit Data"))
            doDisplayVersionedData();
    }

    protected void importDataset() throws EmfException {
        ImportWindow importView = new ImportWindow(session.dataCommonsService(), desktopManager);

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
        //NOTE: get selected dataset will give you all the dataset selected in the base model of sort filter table model
        EmfDataset[] emfDatasets = getNonExternalDatasets(getSelectedDatasets());

        ExportWindow exportView = new ExportWindow(emfDatasets, desktopManager);
        getDesktopPane().add(exportView);

        ExportPresenter exportPresenter = new ExportPresenterImpl(session);
        presenter.doExport(exportView, exportPresenter, emfDatasets);
    }

    private EmfDataset[] getNonExternalDatasets(List emfDatasets) {
        List nonExternal = new ArrayList();
        for (int i = 0; i < emfDatasets.size(); i++) {
            EmfDataset temp = (EmfDataset) emfDatasets.get(i);
            if (!temp.getDatasetType().isExternal())
                nonExternal.add(temp);
        }

        return (EmfDataset[]) nonExternal.toArray(new EmfDataset[0]);
    }

    private List getSelectedDatasets() {
        return selectModel.selected();
    }

    protected void doDisplayPropertiesViewer() {

        List datasets = updateSelectedDatasets(getSelectedDatasets());
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesViewer view = new DatasetPropertiesViewer(parentConsole, desktopManager);
            EmfDataset dataset = (EmfDataset) iter.next();
            presenter.doDisplayPropertiesView(view, dataset);
        }
    }

    private List updateSelectedDatasets(List selectedDatasets) {
        // FIXME: update only datasets that user selected
        List updatedDatasets = new ArrayList();
        try {
            EmfDataset[] updatedAllDatasets1 = session.dataService().getDatasets();
            for (int i = 0; i < selectedDatasets.size(); i++) {
                EmfDataset selDataset = (EmfDataset) selectedDatasets.get(i);
                for (int j = 0; j < updatedAllDatasets1.length; j++) {
                    if (selDataset.getId() == updatedAllDatasets1[j].getId()) {
                        updatedDatasets.add(updatedAllDatasets1[j]);
                        break;
                    }
                }
            }
        } catch (EmfException e) {
            showError(e.getMessage());
        }
        return updatedDatasets;
    }

    protected void doDisplayPropertiesEditor() {
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

    public void showError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
    }

    public void observe(DatasetsBrowserPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(EmfDataset[] datasets) {
        model.refresh(new EmfDatasetTableData(datasets));
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

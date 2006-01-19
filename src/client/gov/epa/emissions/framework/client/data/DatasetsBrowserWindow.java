package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserAwareImportPresenter;
import gov.epa.emissions.framework.client.exim.DefaultExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.PropertiesViewWindow;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataWindow;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.mims.analysisengine.table.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DatasetsBrowserWindow extends ReusableInteralFrame implements DatasetsBrowserView {

    private SortFilterSelectModel selectModel;

    private JPanel layout;

    private MessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private EmfConsole parentConsole;

    private JScrollPane sortFilterPane;

    private EmfSession session;

    private EmfTableModel model;

    public DatasetsBrowserWindow(EmfSession session, EmfConsole parentConsole) throws EmfException {
        super("Datasets Browser", new Dimension(800, 450), parentConsole.desktop());
        super.setName("datasetsBrowser");

        this.session = session;
        DataService services = session.dataService();
        model = new EmfTableModel(new EmfDatasetTableData(services.getDatasets()));
        selectModel = new SortFilterSelectModel(model);
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);

        // FIXME: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout(layout, parentConsole);
    }

    private void createLayout(JPanel layout, EmfConsole parentConsole) {
        layout.removeAll();

        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        sortFilterPane = createSortFilterPane(parentConsole);
        layout.add(sortFilterPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createSortFilterPane(EmfConsole parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        SortCriteria sortCriteria = sortCriteria();
        panel.sort(sortCriteria );
        panel.getTable().setName("datasetsTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 120));

        return scrollPane;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = {"Last Modified Date"};
        return new SortCriteria(columnNames ,new boolean []{false},new boolean []{true});
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        panel.add(createRefreshPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createRefreshPanel() {
        JButton button = new JButton(refreshIcon());
        button.setToolTipText("Refresh Datasets");
        button.setName("refresh");
        button.setBorderPainted(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doRefresh();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });

        JPanel panel = new JPanel();
        panel.add(button);

        return panel;
    }

    private ImageIcon refreshIcon() {
        return new ImageResources().refresh("Refresh Datasets");
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

        Button goButton = new Button("Go", new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                String selected = (String) combo.getSelectedItem();
                selectedOption(selected);
            }
        });
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
        ImportWindow importView = new ImportWindow(session.dataCommonsService(), desktop);
        desktop.add(importView);

        ImportPresenter importPresenter = new DatasetsBrowserAwareImportPresenter(session, session.user(), session
                .eximService(), session.dataService(), this);
        presenter.doImport(importView, importPresenter);
    }

    private JPanel createRightControlPanel() {
        JPanel panel = new JPanel();

        JButton importButton = new Button("Import", new AbstractAction() {
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

        JButton exportButton = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        exportButton.setToolTipText("Export existing Dataset(s)");
        panel.add(exportButton);

        JButton closeButton = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        panel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        return panel;
    }

    protected void exportSelectedDatasets() {
        List datasets = getSelectedDatasets();
        EmfDataset[] emfDatasets = (EmfDataset[]) datasets.toArray(new EmfDataset[0]);

        ExportWindow exportView = new ExportWindow(emfDatasets);
        getDesktopPane().add(exportView);

        ExportPresenter exportPresenter = new DefaultExportPresenter(session);
        presenter.doExport(exportView, exportPresenter, emfDatasets);
    }

    private List getSelectedDatasets() {
        int[] selected = selectModel.getSelectedIndexes();

        return model.elements(selected);
    }

    protected void doDisplayPropertiesViewer() {
        List datasets = getSelectedDatasets();

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            PropertiesViewWindow view = new PropertiesViewWindow(session, parentConsole);
            desktop.add(view);
            presenter.doDisplayPropertiesView(view, (EmfDataset) iter.next());
        }
    }

    protected void doDisplayPropertiesEditor() {
        List datasets = getSelectedDatasets();

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole);
            desktop.add(view);
            try {
                presenter.doDisplayPropertiesEditor(view, (EmfDataset) iter.next());
            } catch (EmfException e) {
                showError(e.getMessage());
            }
        }
    }

    protected void doDisplayVersionedData() {
        List datasets = getSelectedDatasets();

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            VersionedDataWindow view = new VersionedDataWindow(parentConsole);
            desktop.add(view);
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

    public void close() {
        super.dispose();
    }

    public void display() {
        this.setVisible(true);
    }

    public void refresh(EmfDataset[] datasets) {
        model.refresh(new EmfDatasetTableData(datasets));
        selectModel.refresh();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(layout, parentConsole);
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

}

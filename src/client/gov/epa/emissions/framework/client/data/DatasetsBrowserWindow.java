package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.exim.DefaultExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.meta.MetadataWindow;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DatasetsBrowserWindow extends ReusableInteralFrame implements DatasetsBrowserView {

    private SortFilterSelectModel selectModel;

    private JPanel layout;

    private MessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private EmfFrame parentConsole;

    private JScrollPane sortFilterPane;

    private EmfSession session;

    private EmfTableModel model;

    public DatasetsBrowserWindow(EmfSession session, EmfFrame parentConsole, JDesktopPane desktop) throws EmfException {
        super("Datasets Browser", desktop);
        super.setName("datasetsBrowser");

        this.session = session;
        DataServices services = session.getDataServices();
        model = new EmfTableModel(new EmfDatasetTableData(services.getDatasets()));
        selectModel = new SortFilterSelectModel(model);
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
        this.setSize(new Dimension(800, 300));

        // FIXME: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout(layout, parentConsole);
    }

    private void createLayout(JPanel layout, JFrame parentConsole) {
        layout.removeAll();

        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        sortFilterPane = createSortFilterPane(parentConsole);
        layout.add(sortFilterPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane createSortFilterPane(JFrame parentConsole) {
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("datasetsTable");

        JScrollPane scrollPane = new JScrollPane(panel);
        panel.setPreferredSize(new Dimension(450, 120));

        return scrollPane;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        panel.add(createRefreshPanel(), BorderLayout.EAST);

        return panel;
    }

    private JPanel createRefreshPanel() {
        ResourceBundle bundle = ResourceBundle.getBundle("images");
        URL url = StatusWindow.class.getResource(bundle.getString("refresh"));
        ImageIcon icon = new ImageIcon(url, "Refresh Datasets");

        JButton button = new JButton(icon);
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

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createRightControlPanel(), BorderLayout.LINE_END);
        controlPanel.add(createLeftControlPanel(), BorderLayout.LINE_START);

        return controlPanel;
    }

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        JButton newDataset = new Button("New", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    doNewDataset();
                } catch (EmfException e) {
                    showError("Could not open Import window (for creation of a new dataset)");
                }
            }
        });
        panel.add(newDataset);

        JButton properties = new Button("Properties", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doShowMetadata();
            }
        });
        panel.add(properties);

        return panel;
    }

    protected void doNewDataset() throws EmfException {
        ImportWindow importView = new ImportWindow(session.getExImServices(), desktop);
        // windowLayoutManager.add(importView); FIXME: needs layout
        desktop.add(importView);

        ImportPresenter importPresenter = new ImportPresenter(session.getUser(), session.getExImServices());
        presenter.doNew(importView, importPresenter);
    }

    private JPanel createRightControlPanel() {
        JPanel panel = new JPanel();

        JButton exportButton = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        panel.add(exportButton);

        JButton closeButton = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        panel.add(closeButton);

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

    protected void doShowMetadata() {
        List datasets = getSelectedDatasets();

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            MetadataWindow view = new MetadataWindow(session, this, parentConsole);
            getDesktopPane().add(view);

            presenter.doShowMetadata(view, (EmfDataset) iter.next());
        }
    }

    public void showError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }

    public void refreshLayout() {
        super.validate();
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

        this.refreshLayout();
    }

    public void showMessage(String message) {
        messagePanel.setMessage(message);
        refreshLayout();
    }

    public void clearMessage() {
        messagePanel.clear();
        refreshLayout();
    }

}

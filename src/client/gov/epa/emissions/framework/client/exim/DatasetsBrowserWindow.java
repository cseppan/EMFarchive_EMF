package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.meta.MetadataWindow;
import gov.epa.emissions.framework.client.status.StatusWindow;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class DatasetsBrowserWindow extends EmfInteralFrame implements DatasetsBrowserView {

    private DatasetsBrowserTableModel model;

    private SortFilterSelectModel selectModel;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private JFrame parentConsole;

    private JScrollPane sortFilterPane;

    // FIXME: this is very similar to UserManagerWindow. Can we refactory &
    // reuse ?
    public DatasetsBrowserWindow(DataServices services, JFrame parentConsole) throws EmfException {
        super("Datasets Browser");

        // FIXME: change the type from Dataset to EmfDataset
        model = new DatasetsBrowserTableModel((EmfDataset[]) services.getDatasets());
        selectModel = new SortFilterSelectModel(model);
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
        this.setSize(new Dimension(750, 300));

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
        SortFilterSelectionPanel sortFilterSelectionPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectionPanel);
        sortFilterSelectionPanel.setPreferredSize(new Dimension(450, 120));

        return scrollPane;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        panel.add(createRefreshButton(), BorderLayout.EAST);

        return panel;
    }

    private JButton createRefreshButton() {
        ResourceBundle bundle = ResourceBundle.getBundle("images");
        URL url = StatusWindow.class.getResource(bundle.getString("refresh"));
        ImageIcon icon = new ImageIcon(url, "Refresh Datasets");

        JButton button = new JButton(icon);
        button.setToolTipText("Refresh Datasets");
        button.setBorderPainted(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.notifyRefresh();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });

        return button;
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

        JButton metadata = new Button("Metadata", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doShowMetadata();
            }
        });
        panel.add(metadata);

        return panel;
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
                presenter.notifyClose();
            }
        });
        panel.add(closeButton);

        return panel;
    }

    protected void exportSelectedDatasets() {

        // FIXME: notify the Presenter. Let it handle a no-select
        // if (selected.length == 0)
        // return;

        List datasets = getSelectedDatasets();

        try {
            presenter.notifyExport((EmfDataset[]) datasets.toArray(new EmfDataset[0]));
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private List getSelectedDatasets() {
        int[] selected = selectModel.getSelectedIndexes();

        List datasets = new ArrayList();
        for (int i = 0; i < selected.length; i++) {
            EmfDataset dataset = model.getDataset(selected[i]);
            datasets.add(dataset);
        }

        return datasets;
    }

    protected void doShowMetadata() {
        List datasets = getSelectedDatasets();

        for (Iterator iter = datasets.iterator(); iter.hasNext();) {
            MetadataWindow view = new MetadataWindow();
            getDesktopPane().add(view);

            presenter.notifyShowMetadata(view, (EmfDataset) iter.next());
        }
    }

    private void showError(String message) {
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

    public void showExport(EmfDataset[] datasets, ExportPresenter exportPresenter) {
        ExportWindow exportView = new ExportWindow(datasets);
        exportPresenter.observe(exportView);

        getDesktopPane().add(exportView);
        exportView.display();
    }

    public void refresh(EmfDataset[] datasets) {
        model.populate(datasets);
        selectModel.refresh();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(layout, parentConsole);

        this.refreshLayout();
    }

}

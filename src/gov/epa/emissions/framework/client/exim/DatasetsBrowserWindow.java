package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.DefaultButton;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DataServices;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
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

        //FIXME: change the type from Dataset to EmfDataset
        model = new DatasetsBrowserTableModel((EmfDataset[])services.getDatasets());
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

        layout.add(createTopPanel(layout), BorderLayout.NORTH);
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

    private JPanel createTopPanel(JPanel layout) {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        JButton refresh = new DefaultButton("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.notifyRefresh();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });
        panel.add(refresh, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel closePanel = new JPanel();

        JButton exportButton = new DefaultButton("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        closePanel.add(exportButton);

        JButton closeButton = new DefaultButton("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null) {
                    presenter.notifyClose();
                }
            }
        });
        closePanel.add(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    protected void exportSelectedDatasets() {
        if (presenter == null)
            return;
        int[] selected = selectModel.getSelectedIndexes();
        if (selected.length == 0)
            return;

        for (int i = 0; i < selected.length; i++) {
            try {
                EmfDataset dataset = model.getDataset(selected[i]);
                presenter.notifyExport(dataset);
            } catch (EmfException e) {
                showError(e.getMessage());
                break;// TODO: should continue ?
            }
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

    public void showExport(EmfDataset dataset, ExportPresenter exportPresenter) throws EmfException {
        ExportWindow exportView = new ExportWindow(dataset);
        exportPresenter.observe(exportView);

        getDesktopPane().add(exportView);
        exportView.display();
    }

    public void refresh(EmfDataset[] datasets) {
        model.populate(datasets);
        selectModel.refresh();
        
//      TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        createLayout(layout, parentConsole);
        
        this.refreshLayout();
    }
}

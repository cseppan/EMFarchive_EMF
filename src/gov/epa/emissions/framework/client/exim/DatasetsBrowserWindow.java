package gov.epa.emissions.framework.client.exim;

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
import java.awt.event.ActionListener;

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

    // FIXME: this is very similar to UserManagerWindow. Can we refactory &
    // reuse ?
    public DatasetsBrowserWindow(DataServices services, JFrame parentConsole) throws EmfException {
        super("Datasets Browser");

        model = new DatasetsBrowserTableModel(services);
        selectModel = new SortFilterSelectModel(model);

        layout = new JPanel();
        this.getContentPane().add(layout);

        // FIXME: OverallTableModel has a bug w/ respect to row-count &
        // cannot refresh itself. So, we will regen the layout on every
        // refresh - it's a HACK. Will need to be addressed
        createLayout(parentConsole);
    }

    private void createLayout(JFrame parentConsole) {
        layout.removeAll();
        SortFilterSelectionPanel sortFilterSelectPanel = new SortFilterSelectionPanel(parentConsole, selectModel);
        createLayout(layout, sortFilterSelectPanel);

        int parentWidth = (int) parentConsole.getSize().getWidth();
        this.setSize(new Dimension(parentWidth - 10, 300));
    }

    private void createLayout(JPanel layout, JPanel sortFilterSelectPanel) {
        layout.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(sortFilterSelectPanel);
        sortFilterSelectPanel.setPreferredSize(new Dimension(450, 120));

        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(scrollPane, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel closePanel = new JPanel();

        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        closePanel.add(exportButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (presenter != null) {
                    presenter.notifyCloseView();
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
                messagePanel.setError(e.getMessage());
                refresh();
                break;// TODO: should continue ?
            }
        }
    }

    private void refresh() {
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
}

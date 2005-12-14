package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DataViewWindow extends DisposableInteralFrame implements DataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataViewPresenter presenter;

    private EmfDataset dataset;

    private JPanel labelPanel;

    public DataViewWindow(EmfDataset dataset) {
        super("Dataset Viewer - Dataset: " + dataset.getName(), new Dimension(900, 750));
        this.dataset = dataset;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(dataset), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    private JPanel topPanel(EmfDataset dataset) {
        JPanel panel = new JPanel(new BorderLayout());

        labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(new JLabel("    Dataset:    " + dataset.getName()));
        panel.add(labelPanel, BorderLayout.LINE_START);

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        return panel;
    }

    public void observe(DataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table, DataEditorService service) {
        updateTitle(version, table);

        JPanel container = new JPanel(new BorderLayout());

        container.add(tablePanel(version, table, service), BorderLayout.CENTER);
        container.add(controlsPanel(), BorderLayout.PAGE_END);

        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private void updateTitle(Version version, String table) {
        super.setTitle(super.getTitle() + ". Version: " + version.getName() + ". Table: " + table);
        labelPanel.add(new JLabel("    Version:    " + version.getName()));
        labelPanel.add(new JLabel("    Table:       " + table));
    }

    private JPanel tablePanel(Version version, String table, DataEditorService service) {
        TableViewPanel tableView = new TableViewPanel(source(table, dataset.getInternalSources()), messagePanel);
        TablePresenter tablePresenter = new TablePresenter(version, table, tableView, service);
        tablePresenter.observe();

        try {
            tablePresenter.doDisplayFirst();
        } catch (EmfException e) {
            messagePanel.setError("Could not display table: " + table + ". Reason: " + e.getMessage());
        }

        return tableView;
    }

    private InternalSource source(String table, InternalSource[] sources) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getTable().equals(table))
                return sources[i];
        }

        return null;
    }

    private JPanel controlsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    messagePanel.setError("Could not close. Reason: " + e.getMessage());
                }
            }
        });
        panel.add(close, BorderLayout.LINE_END);

        return panel;
    }

}

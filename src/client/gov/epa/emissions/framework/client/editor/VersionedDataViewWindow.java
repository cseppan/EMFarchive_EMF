package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class VersionedDataViewWindow extends DisposableInteralFrame implements VersionedDataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private VersionedDataViewPresenter presenter;

    private EmfDataset dataset;

    public VersionedDataViewWindow(EmfDataset dataset) {
        super("Dataset: " + dataset.getName(), new Dimension(900, 750));
        this.dataset = dataset;

        layout = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    public void observe(VersionedDataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table) {
        super.setTitle(super.getTitle() + ". Version: " + version.getVersion() + ". Table: " + table);

        JPanel container = new JPanel(new BorderLayout());

        container.add(tablePanel(version, table), BorderLayout.CENTER);
        container.add(controlsPanel(), BorderLayout.PAGE_END);

        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private JPanel tablePanel(Version version, String table) {
        VersionedTableViewPanel tableView = new VersionedTableViewPanel(source(table, dataset.getInternalSources()),
                messagePanel);
        VersionedTablePresenter tablePresenter = new VersionedTablePresenter(version, table, tableView, null);

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

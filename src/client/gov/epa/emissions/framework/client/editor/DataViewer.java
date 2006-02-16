package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.notes.NewNoteDialog;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.Dimensions;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class DataViewer extends DisposableInteralFrame implements DataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataViewPresenter presenter;

    private EmfDataset dataset;

    private EmfConsole parent;

    public DataViewer(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager) {
        super("Data Viewer [Dataset:" + dataset.getName(), desktopManager);
        setDimension();
        this.dataset = dataset;
        this.parent = parent;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    private void setDimension() {
        Dimension dim = new Dimensions().getSize(0.7, 0.7);
        setSize(dim);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        return panel;
    }

    public void observe(DataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table, DataAccessService service) {
        updateTitle(version, table);
        super.setName("dataViewer:" + version.getDatasetId() + ":" + version.getId());

        JPanel container = new JPanel(new BorderLayout());
        container.add(tablePanel(version, table, service), BorderLayout.CENTER);
        container.add(controlsPanel(), BorderLayout.PAGE_END);
        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private void updateTitle(Version version, String table) {
        String label = super.getTitle();
        label += ", Version: " + version.getName();
        label += ", Table: " + table + "]";
        super.setTitle(label);
    }

    private JPanel tablePanel(Version version, String table, DataAccessService service) {
        NonEditableTableViewPanel tableView = new NonEditableTableViewPanel(
                source(table, dataset.getInternalSources()), messagePanel);
        TablePresenter tablePresenter = new ViewableTablePresenter(version, table, tableView, service);
        tablePresenter.observe();

        try {
            tablePresenter.doDisplayFirst();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
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
        panel.add(leftPanel(), BorderLayout.LINE_START);
        panel.add(rightPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel leftPanel() {
        JPanel leftPanel = new JPanel();
        Button addNote = new Button("Add Note", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAddNote();
            }
        });
        leftPanel.add(addNote);
        return leftPanel;
    }

    private JPanel rightPanel() {
        JPanel rightPanel = new JPanel();
        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    messagePanel.setError("Could not close. Reason: " + e.getMessage());
                }
            }
        });
        rightPanel.add(close);
        return rightPanel;
    }

    private void doAddNote() {
        NewNoteDialog view = new NewNoteDialog(parent);
        try {
            presenter.doAddNote(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

}

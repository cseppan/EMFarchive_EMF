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
import gov.epa.emissions.framework.ui.Dimensions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EditableDataViewWindow extends DisposableInteralFrame implements EditableDataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private EditableDataViewPresenter presenter;

    private EmfDataset dataset;

    private JPanel labelPanel;

    public EditableDataViewWindow(EmfDataset dataset) {
        super("Dataset Editor - Dataset: " + dataset.getName());
        setDimension();
        this.dataset = dataset;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(dataset), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    private void setDimension() {
        Dimension dim = new Dimensions().getSize(0.7, 0.7);
        setSize(dim);
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

    public void observe(EditableDataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table, DataEditorService service) {
        updateTitle(version, table);

        JPanel container = new JPanel(new BorderLayout());

        container.add(tablePanel(version, table, service), BorderLayout.CENTER);
        container.add(controlPanel(), BorderLayout.PAGE_END);

        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private void updateTitle(Version version, String table) {
        super.setTitle(super.getTitle() + ". Version: " + version.getName() + ". Table: " + table);
        labelPanel.add(new JLabel("    Version:    " + version.getName()));
        labelPanel.add(new JLabel("    Table:       " + table));
    }

    private JPanel tablePanel(Version version, String table, DataEditorService service) {
        InternalSource source = source(table, dataset.getInternalSources());
        EditableTableViewPanel tableView = new EditableTableViewPanel(dataset, version, source, messagePanel);
        TablePresenter tablePresenter = new EditableTablePresenter(version, table, tableView, service);
        tablePresenter.observe();

        try {
            tablePresenter.doDisplayFirst();
        } catch (EmfException e) {
            displayError("Could not display table: " + table + ". Reason: " + e.getMessage());
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

    private JPanel controlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(leftControlPanel(), BorderLayout.LINE_START);
        panel.add(rightControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel leftControlPanel() {
        JPanel panel = new JPanel();
        panel.add(markFinalButton());

        return panel;
    }

    private JPanel rightControlPanel() {
        JPanel panel = new JPanel();

        panel.add(discardButton());
        panel.add(saveButton());
        panel.add(closeButton());

        return panel;
    }

    private Button closeButton() {
        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    displayError("Could not Close. Reason: " + e.getMessage());
                }
            }

        });
        close.setToolTipText("Close without Saving your changes");
        return close;
    }

    private Button saveButton() {
        Button save = new Button("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doSave();
                } catch (EmfException e) {
                    displayError("Could not Save. Reason: " + e.getMessage());
                }
            }

        });
        save.setToolTipText("Save your changes");
        return save;
    }

    private Button discardButton() {
        // TODO: prompts for Discard and Close (if changes exist)
        Button discard = new Button("Discard", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDiscard();
                } catch (EmfException e) {
                    displayError("Could not Discard. Reason: " + e.getMessage());
                }
            }

        });
        discard.setToolTipText("Discard your changes");
        return discard;
    }

    private Button markFinalButton() {
        // TODO: prompt for submit changes, save, and mark final - all in one step
        Button markFinal = new Button("Mark Final", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doMarkFinal();
                } catch (EmfException e) {
                    displayError("Could not Mark version as Final. Reason: " + e.getMessage());
                }
            }

        });
        markFinal.setToolTipText("Save changes, Mark version as Final, and Close editor.");
        return markFinal;
    }

    private void displayError(String message) {
        messagePanel.setError(message);
        refreshLayout();
    }
}

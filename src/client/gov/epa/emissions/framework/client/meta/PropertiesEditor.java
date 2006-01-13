package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTab;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTab;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PropertiesEditor extends DisposableInteralFrame implements PropertiesEditorView {

    private PropertiesEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private DatasetsBrowserView datasetsBrowser;

    public PropertiesEditor(EmfSession session, DatasetsBrowserView datasetsBrowser, EmfConsole parentConsole) {
        super("Properties Editor", new Dimension(700, 550));
        this.session = session;
        this.datasetsBrowser = datasetsBrowser;
        this.parentConsole = parentConsole;
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset, messagePanel));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Logs", createLogsTab(dataset, parentConsole));
        tabbedPane.addTab("Info", createInfoTab(dataset, parentConsole));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        try {
            EditableSummaryTab view = new EditableSummaryTab(dataset, session.dataCommonsService(), messagePanel);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab. Reason - " + e.getMessage());
            return createErrorTab("Could not load Summary Tab. Reason - " + e.getMessage());
        }
    }

    private JPanel createInfoTab(EmfDataset dataset, EmfConsole parentConsole) {
        InfoTab view = new InfoTab(parentConsole);
        InfoTabPresenter presenter = new InfoTabPresenter(view, dataset);
        presenter.doDisplay();

        return view;
    }

    private JPanel createKeywordsTab() {
        EditableKeywordsTab view = new EditableKeywordsTab();
        try {
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Keyword Tab. Reason - " + e.getMessage());
            return createErrorTab("Could not load Keyword Tab. Reason - " + e.getMessage());
        }
    }

    private JPanel createLogsTab(EmfDataset dataset, EmfConsole parentConsole) {
        try {
            LogsTab view = new LogsTab(parentConsole);
            LogsTabPresenter presenter = new LogsTabPresenter(view, dataset, session.loggingService());
            presenter.doDisplay();

            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Logs tab. Failed communication with remote Logging Services.");
            return createErrorTab("Could not load Logs tab. Failed communication with remote Logging Services.");
        }
    }

    private JPanel createErrorTab(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    }

    public void display(EmfDataset dataset) {
        super.setTitle("Properties Editor: " + dataset.getName());
        super.setName("Properties Editor: " + dataset.getName());
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(dataset, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        contentPane.add(panel);

        super.display();
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button save = new Button("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doSave(datasetsBrowser);
            }
        });
        buttonsPanel.add(save);

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    public void observe(PropertiesEditorPresenter presenter) {
        this.presenter = presenter;
    }

    // FIXME: should this be mandatory for all EmfViews ?
    public void showError(String message) {
        // TODO: error should go away at some point. when ?
        messagePanel.setError(message);
    }

    public boolean shouldContinueLosingUnsavedChanges() {
        int option = JOptionPane.showConfirmDialog(this,
                "Would you like to Close(without saving and lose the updates)?", "Close", JOptionPane.YES_NO_OPTION);
        return (option == 0);
    }

}

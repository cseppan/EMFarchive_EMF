package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.LoggingServices;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PropertiesEditor extends DisposableInteralFrame implements PropertiesEditorView {

    private PropertiesEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfFrame parentConsole;

    private EmfSession session;

    private DatasetsBrowserView datasetsBrowser;

    public PropertiesEditor(EmfSession session, DatasetsBrowserView datasetsBrowser, EmfFrame parentConsole) {
        super("Dataset Properties Editor");
        this.session = session;
        this.datasetsBrowser = datasetsBrowser;
        this.parentConsole = parentConsole;

        super.setSize(new Dimension(700, 485));
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset, messagePanel));
        tabbedPane.addTab("Data", createTab());
        tabbedPane.addTab("Keywords", createTab());
        tabbedPane.addTab("Logs", createLogsTab(dataset, session.getLoggingServices(), parentConsole));
        tabbedPane.addTab("Info", createTab());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createLogsTab(EmfDataset dataset, LoggingServices services, EmfFrame parentConsole) {
        try {
            LogsTab view = new LogsTab(dataset, services, parentConsole);

            // FIXME2: activate the presenter on tab-click
            // LogsTabPresenter presenter = new LogsTabPresenter(view, dataset,
            // session.getLoggingServices());
            // presenter.display();

            return view;
        } catch (EmfException e) {
            messagePanel.setError("could not load Logs tab. Failed communication with remote Logging Services.");
        }

        return createTab();
    }

    private JPanel createSummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        try {
            SummaryTab view = new SummaryTab(dataset, session.getDataServices(), messagePanel);
            presenter.add(view);

            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab. Reason - " + e.getMessage());
        }

        return createTab();
    }

    // TODO: other tabs
    private JPanel createTab() {
        JPanel panel = new JPanel(false);

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
        panel.add(createButtonsPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createButtonsPanel() {
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

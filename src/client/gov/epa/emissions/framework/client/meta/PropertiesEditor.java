package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.client.editor.DataViewWindow;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PropertiesEditor extends DisposableInteralFrame implements PropertiesEditorView {

    private PropertiesEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfFrame parentConsole;

    private EmfSession session;

    private DatasetsBrowserView datasetsBrowser;

    private JDesktopPane desktop;

    public PropertiesEditor(EmfSession session, DatasetsBrowserView datasetsBrowser, EmfFrame parentConsole,
            JDesktopPane desktop) {
        super("Properties Editor", new Dimension(700, 485));
        this.session = session;
        this.datasetsBrowser = datasetsBrowser;
        this.parentConsole = parentConsole;
        this.desktop = desktop;
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset, messagePanel));
        tabbedPane.addTab("Data", createDataTab(dataset, parentConsole));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Logs", createLogsTab(dataset, parentConsole));
        tabbedPane.addTab("Info", createErrorTab("TODO"));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        try {
            SummaryTab view = new SummaryTab(dataset, session.getDataServices(), messagePanel);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab. Reason - " + e.getMessage());
            return createErrorTab("Could not load Summary Tab. Reason - " + e.getMessage());
        }
    }

    private JPanel createDataTab(EmfDataset dataset, EmfFrame parentConsole) {
        DataTab view = new DataTab(parentConsole);
        DataTabPresenter presenter = new DataTabPresenter(view, dataset);
        presenter.doDisplay();

        return view;
    }

    private JPanel createKeywordsTab() {
        KeywordsTab view = new KeywordsTab();
        try {
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Keyword Tab. Reason - " + e.getMessage());
            return createErrorTab("Could not load Keyword Tab. Reason - " + e.getMessage());
        }
    }

    private JPanel createLogsTab(EmfDataset dataset, EmfFrame parentConsole) {
        try {
            LogsTab view = new LogsTab(parentConsole);

            // FIXME: activate the presenter on tab-click
            LogsTabPresenter presenter = new LogsTabPresenter(view, dataset, session.getLoggingServices());
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
        panel.add(createBottomPanel(dataset), BorderLayout.PAGE_END);

        contentPane.add(panel);

        super.display();
    }

    private JPanel createBottomPanel(EmfDataset dataset) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createDataPanel(dataset), BorderLayout.LINE_START);
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createDataPanel(final EmfDataset dataset) {
        JPanel panel = new JPanel();
        Button showData = new Button("Show Data", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                DataViewWindow view = new DataViewWindow();
                desktop.add(view);
                presenter.doDisplayData(view);
            }
        });
        panel.add(showData);

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

package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTab;
import gov.epa.emissions.framework.client.meta.summary.SummaryTab;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class PropertiesViewWindow extends DisposableInteralFrame implements PropertiesView {

    private PropertiesViewPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public PropertiesViewWindow(EmfSession session, EmfConsole parentConsole) {
        super("Properties View", new Dimension(700, 500));
        this.session = session;
        this.parentConsole = parentConsole;
    }

    public void display(EmfDataset dataset) {
        super.setTitle("Properties View: " + dataset.getName());
        super.setName("Properties View: " + dataset.getName());

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(dataset), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        super.getContentPane().add(panel);

        super.display();
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset));
        tabbedPane.addTab("Data", createDataTab(dataset, parentConsole));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Logs", createLogsTab(dataset, parentConsole));
        tabbedPane.addTab("Info", createInfoTab(dataset, parentConsole));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset) {
        SummaryTab view = new SummaryTab(dataset);
        presenter.set(view);
        return view;
    }

    private JPanel createDataTab(EmfDataset dataset, EmfConsole parentConsole) {
        ServiceLocator serviceLocator = session.serviceLocator();
        DataEditorService dataEditorService = serviceLocator.dataEditorService();

        DataTab view = new DataTab(parentConsole);
        DataTabPresenter presenter = new DataTabPresenter(view, dataset, dataEditorService);
        presenter.doDisplay();

        return view;
    }

    private JPanel createInfoTab(EmfDataset dataset, EmfConsole parentConsole) {
        InfoTab view = new InfoTab(parentConsole);
        InfoTabPresenter presenter = new InfoTabPresenter(view, dataset);
        presenter.doDisplay();

        return view;
    }

    private JPanel createKeywordsTab() {
        KeywordsTab view = new KeywordsTab();
        presenter.set(view);
        return view;
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

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    public void observe(PropertiesViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void showError(String message) {
        messagePanel.setError(message);
    }

}

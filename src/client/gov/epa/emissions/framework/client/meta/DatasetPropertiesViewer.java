package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.info.InfoTab;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTab;
import gov.epa.emissions.framework.client.meta.notes.NotesTab;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTab;
import gov.epa.emissions.framework.client.meta.summary.SummaryTab;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DatasetPropertiesViewer extends DisposableInteralFrame implements PropertiesView {

    private PropertiesViewPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    //private EmfSession session;

    public DatasetPropertiesViewer(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Properties View", new Dimension(700, 500), desktopManager);
        //this.session = session;
        this.parentConsole = parentConsole;
    }

    public void display(EmfDataset dataset) {
        super.setTitle("Dataset Properties View: " + dataset.getName());
        super.setName("datasetPropertiesView:" + dataset.getId());
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
        tabbedPane.addTab("Data", createDataTab(parentConsole));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Notes", createNotesTab(parentConsole));
        tabbedPane.addTab("Revisions", createRevisionsTab(parentConsole));
        tabbedPane.addTab("Logs", createLogsTab(parentConsole));
        tabbedPane.addTab("Tables", createInfoTab(parentConsole));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset) {
        SummaryTab view = new SummaryTab(dataset);
        presenter.set(view);
        return view;
    }

    private JPanel createDataTab(EmfConsole parentConsole) {
        DataTab view = new DataTab(parentConsole, desktopManager);
        presenter.set(view);
        return view;
    }

    private JPanel createInfoTab(EmfConsole parentConsole) {
        InfoTab view = new InfoTab(parentConsole);
        presenter.set(view);
        return view;
    }

    private JPanel createKeywordsTab() {
        KeywordsTab view = new KeywordsTab();
        presenter.set(view);
        return view;
    }

    private JPanel createNotesTab(EmfConsole parentConsole) {
        try {
            NotesTab view = new NotesTab(parentConsole);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Notes tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Notes tab. Failed communication with remote EMF Services.");
        }
    }

    private JPanel createRevisionsTab(EmfConsole parentConsole) {
        try {
            RevisionsTab view = new RevisionsTab(parentConsole);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Revisions tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Revisions tab. Failed communication with remote EMF Services.");
        }
    }

    private JPanel createLogsTab(EmfConsole parentConsole) {
        try {
            LogsTab view = new LogsTab(parentConsole);
            presenter.set(view);
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

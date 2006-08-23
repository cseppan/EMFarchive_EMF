package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.client.meta.info.InfoTab;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTab;
import gov.epa.emissions.framework.client.meta.qa.EditableQATab;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTab;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTab;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DatasetPropertiesEditor extends DisposableInteralFrame implements DatasetPropertiesEditorView {

    private PropertiesEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EditableKeywordsTab keywordsTab;

    private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    public DatasetPropertiesEditor(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Properties Editor", new Dimension(700, 510), desktopManager);
        this.session = session;
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, Version[] versions, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset, versions, messagePanel));
        tabbedPane.addTab("Data", createDataTab(parentConsole));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Notes", createNotesTab(parentConsole));
        tabbedPane.addTab("Revisions", createRevisionsTab(parentConsole));
        tabbedPane.addTab("Logs", createLogsTab(dataset, parentConsole));
        tabbedPane.addTab("Tables", createInfoTab(dataset, parentConsole));
        tabbedPane.addTab("QA", createQATab());

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset, Version[] versions, MessagePanel messagePanel) {
        try {
            EditableSummaryTab view = new EditableSummaryTab(dataset, versions, session.dataCommonsService(),
                    messagePanel, this);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }
    }
    
    private JPanel createDataTab(EmfConsole parentConsole) {
        DataTab view = new DataTab(parentConsole, desktopManager);
        presenter.set(view);
        return view;
    }

    private JPanel createQATab() {
        EditableQATab view = new EditableQATab(parentConsole, desktopManager, this, messagePanel);
        try {
            presenter.set(view);
        } catch (EmfException e) {
            showError("Could not load QA Tab." + e.getMessage());
            return createErrorTab("Could not load QA Tab." + e.getMessage());
        }
        return view;
    }

    private JPanel createInfoTab(EmfDataset dataset, EmfConsole parentConsole) {
        InfoTab view = new InfoTab(parentConsole);
        InfoTabPresenter presenter = new InfoTabPresenter(view, dataset);
        presenter.doDisplay();

        return view;
    }

    private JPanel createKeywordsTab() {
        keywordsTab = new EditableKeywordsTab(this, parentConsole);
        try {
            presenter.set(keywordsTab);
            return keywordsTab;
        } catch (EmfException e) {
            showError("Could not load Keyword Tab." + e.getMessage());
            return createErrorTab("Could not load Keyword Tab." + e.getMessage());
        }
    }

    private JPanel createNotesTab(EmfConsole parentConsole) {
        try {
            EditNotesTab view = new EditNotesTab(parentConsole, this, messagePanel, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Notes tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Notes tab. Failed communication with remote EMF Services.");
        }
    }

    private JPanel createRevisionsTab(EmfConsole parentConsole) {
        try {
            RevisionsTab view = new RevisionsTab(parentConsole, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Revisions tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Revisions tab. Failed communication with remote EMF Services.");
        }
    }
    
    private JPanel createLogsTab(EmfDataset dataset, EmfConsole parentConsole) {
        try {
            LogsTab view = new LogsTab(parentConsole);
            LogsTabPresenter presenter = new LogsTabPresenter(view, dataset, session.loggingService());
            presenter.display();

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

    public void display(EmfDataset dataset, Version[] versions) {
        super.setTitle("Dataset Properties Editor: " + dataset.getName());
        super.setName("datasetPropertiesEditor:" + dataset.getId());
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(dataset, versions, messagePanel), BorderLayout.CENTER);
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

        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        buttonsPanel.add(save);

        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
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

    public void notifyLockFailure(EmfDataset dataset) {
        String message = "Cannot edit Properties of Dataset: " + dataset.getName()
                + System.getProperty("line.separator") + " as it was locked by User: " + dataset.getLockOwner()
                + "(at " + format(dataset.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return dateFormat.format(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }

    
    private void doSave() {
        keywordsTab.commit();
        try {
            presenter.doSave();
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

}

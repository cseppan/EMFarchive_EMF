package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.info.InfoTab;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTab;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTab;
import gov.epa.emissions.framework.services.EmfDataset;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class DatasetPropertiesEditor extends DisposableInteralFrame implements DatasetPropertiesEditorView {

    private PropertiesEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private EditableKeywordsTab keywordsTab;

    private ChangeablesList changeablesList;

    public DatasetPropertiesEditor(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Properties Editor", new Dimension(700, 550), desktopManager);
        this.session = session;
        this.parentConsole = parentConsole;
        this.changeablesList = new ChangeablesList(this);
    }

    private JTabbedPane createTabbedPane(EmfDataset dataset, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(dataset, messagePanel));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Notes", createNotesTab(parentConsole));
        tabbedPane.addTab("Logs", createLogsTab(dataset, parentConsole));
        tabbedPane.addTab("Tables", createInfoTab(dataset, parentConsole));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSummaryTab(EmfDataset dataset, MessagePanel messagePanel) {
        try {
            EditableSummaryTab view = new EditableSummaryTab(dataset, session.dataCommonsService(), messagePanel,
                    changeablesList);
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
        keywordsTab = new EditableKeywordsTab();
        try {
            presenter.set(keywordsTab);
            return keywordsTab;
        } catch (EmfException e) {
            showError("Could not load Keyword Tab. Reason - " + e.getMessage());
            return createErrorTab("Could not load Keyword Tab. Reason - " + e.getMessage());
        }
    }

    private JPanel createNotesTab(EmfConsole parentConsole) {
        try {
            EditNotesTab view = new EditNotesTab(parentConsole);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Notes tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Notes tab. Failed communication with remote EMF Services.");
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
        super.setTitle("Dataset Properties Editor: " + dataset.getName());
        super.setName("datasetPropertiesEditor:" + dataset.getId());
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
                keywordsTab.commit();
                try {
                    presenter.doSave();
                } catch (EmfException e) {
                    showError("Could not save dataset. Reason: " + e.getMessage());
                }
            }
        });
        buttonsPanel.add(save);

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doClose();
                } catch (EmfException e) {
                    showError("Could not close. Reason - " + e.getMessage());
                }
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
        String message = "Would you like to Close(without saving and lose the updates)?";
        String title = "Close";
        return new ConfirmDialog(message, title, this).confirm();
    }

    public void notifyLockFailure(EmfDataset dataset) {
        String message = "Cannot edit Properties of Dataset: " + dataset.getName() + " as it was locked by User:"
                + dataset.getLockOwner() + "(at " + format(dataset.getLockDate()) + ")";
        JOptionPane.showMessageDialog(this, message);
    }

    private String format(Date lockDate) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        return dateFormat.format(lockDate);
    }

    public void windowClosing() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close. Reason - " + e.getMessage());
            return;
        }
    }

}

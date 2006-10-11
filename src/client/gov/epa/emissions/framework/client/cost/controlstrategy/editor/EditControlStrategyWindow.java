package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.RunButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.gui.buttons.StopButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class EditControlStrategyWindow extends DisposableInteralFrame implements EditControlStrategyView {

    private EditControlStrategyPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    private ControlStrategy controlStrategy;

    private DesktopManager desktopManager;

    private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    private Button saveButton;

    private Button runButton;

    public EditControlStrategyWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Control Strategy", new Dimension(650, 490), desktopManager);
        super.setMinimumSize(new Dimension(10, 10));
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    public void observe(EditControlStrategyPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
        super.setLabel(super.getTitle() + ": " + controlStrategy.getName());

        this.controlStrategy = controlStrategy;

        doLayout(controlStrategy, controlStrategyResults);
        pack();
        super.display();
    }

    private void doLayout(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(controlStrategy, controlStrategyResults));
        layout.add(createButtonsPanel(), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(controlStrategy, controlStrategyResults));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // These are just added to illustrate what is coming later
        tabbedPane.addTab("Filters", createFilterTab(controlStrategy));
        tabbedPane.addTab("Outputs", outputPanel(controlStrategyResults));

        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlStrategy controlStrategy, ControlStrategyResult controlStrategyResults) {
        try {
            EditControlStrategySummaryTabView view = new EditControlStrategySummaryTab(controlStrategy,
                    controlStrategyResults, session, this, messagePanel, parentConsole);
            this.presenter.set(view);
            return (JPanel) view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }

    }

    private JPanel createFilterTab(ControlStrategy controlStrategy) {
        EditControlStrategyTabView view = new EditControlStrategyFilterTab(controlStrategy, this);
        this.presenter.set(view);
        return (JPanel) view;
    }

    private JPanel outputPanel(ControlStrategyResult controlStrategyResults) {
        EditControlStrategyOutputTabView view = new EditControlStrategyOutputTab(controlStrategy,
                controlStrategyResults, messagePanel, desktopManager, parentConsole);
        this.presenter.set(view);
        return (JPanel) view;
    }

    private JPanel createErrorTab(String message) {// TODO
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    }

    private void showError(String message) {
        messagePanel.setError(message);
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        saveButton = new SaveButton(saveAction());
        container.add(saveButton);

        Button copyButton = new CopyButton(null);
        copyButton.setEnabled(false);
        container.add(copyButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        runButton = new RunButton(runAction());
        container.add(runButton);

        Button refreshButton = new Button("Refresh", refreshAction());
        container.add(refreshButton);

        Button stopButton = new StopButton(stopAction());
        stopButton.setEnabled(false);
        container.add(stopButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action refreshAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                enableButtons(true);
                try {
                    presenter.doRefresh();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };
    }

    private Action stopAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                try {
                    presenter.stopRun();
                } catch (EmfException e) {
                    messagePanel.setError("Error stopping running strategy: " + e.getMessage());
                }
            }
        };
    }

    private Action runAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    save();
                    enableButtons(false);
                    presenter.setResults(controlStrategy);
                    presenter.runStrategy();
                    messagePanel
                            .setMessage("Please examine the status window for progress, and reopen this window after completion to see results");
                } catch (EmfException e) {
                    enableButtons(true);
                    messagePanel.setError("Error running strategy: " + e.getMessage());
                }
            }
        };
    }

    private void enableButtons(boolean enable) {
        saveButton.setEnabled(enable);
        runButton.setEnabled(enable);
    }

    protected void save() throws EmfException {
        clearMessage();
        presenter.doSave();
        resetChanges();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    save();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void notifyLockFailure(ControlStrategy controlStrategy) {
        String message = "Cannot edit Properties of Control Strategy: " + controlStrategy
                + System.getProperty("line.separator") + " as it was locked by User: " + controlStrategy.getLockOwner()
                + "(at " + format(controlStrategy.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return dateFormat.format(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

}

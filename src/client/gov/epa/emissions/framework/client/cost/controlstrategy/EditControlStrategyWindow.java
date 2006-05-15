package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTab;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
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

    private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    public EditControlStrategyWindow(DesktopManager desktopManager, EmfSession session) {
        super("Edit a Control Strategy", new Dimension(800, 510), desktopManager);
        this.session = session;
    }

    public void observe(EditControlStrategyPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(ControlStrategy controlStrategy) {
        super.setLabel("Edit a ControlStrategy");

        doLayout(controlStrategy);
        pack();
        super.display();
    }

    private void doLayout(ControlStrategy controlStrategy) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(controlStrategy));
        layout.add(createButtonsPanel(), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlStrategy controlStrategy) {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab(controlStrategy));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // These are just added to illustrate what is coming later
        tabbedPane.addTab("Filters", new JPanel());
        tabbedPane.addTab("Parameters", new JPanel());
        tabbedPane.addTab("Outputs", new JPanel());

        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlStrategy controlStrategy) {
        try {
            EditControlStrategySummaryTab view = new EditControlStrategySummaryTab(controlStrategy, session, this,
                    messagePanel);
            this.presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not load Summary Tab." + e.getMessage());
        }

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
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new Button("Save", saveAction());
        container.add(saveButton);
        container.add(new Button("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(40));
        container.add(new Button("Run", runAction()));
        
        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action runAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                //TODO:
            }
        };
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
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
                resetChanges();

                try {
                    presenter.doSave();
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

}

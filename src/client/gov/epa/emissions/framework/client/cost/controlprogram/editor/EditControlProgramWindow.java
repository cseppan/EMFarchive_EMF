package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class EditControlProgramWindow extends DisposableInteralFrame implements EditControlProgramView {

    private EditControlProgramPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

//    private ControlProgram controlProgram;
//
//    private DesktopManager desktopManager;

    private Button saveButton;

    private EditControlProgramMeasuresTab measuresTabView;

    private EditControlProgramSummaryTab summaryTabView;

    public EditControlProgramWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Control Program", new Dimension(700, 580), desktopManager);
//        this.setMinimumSize(new Dimension(700, 300));
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    public void observe(EditControlProgramPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(ControlProgram controlProgram) {
        super.setLabel(super.getTitle() + ": " + controlProgram.getName());

//        this.controlProgram = controlProgram;

        doLayout(controlProgram);
//        pack();
        super.display();
//        super.resetChanges();
    }

    private void doLayout(ControlProgram controlProgram) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(controlProgram));
        layout.add(createButtonsPanel(), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlProgram controlProgram) {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Summary", createSummaryTab(controlProgram));
        tabbedPane.addTab("Measures", createMeasuresTab(controlProgram));
        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlProgram controlProgram) {
        try {
            summaryTabView = new EditControlProgramSummaryTab(controlProgram, session,
                    this, messagePanel, parentConsole);
            this.presenter.set(summaryTabView);
            return summaryTabView;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not create Summary Tab." + e.getMessage());
        }
    }

    private JPanel createMeasuresTab(ControlProgram controlProgram) {
        try {
            measuresTabView = new EditControlProgramMeasuresTab(controlProgram, this,  
                    messagePanel, parentConsole, 
                    session);
            this.presenter.set(measuresTabView);
        } catch (EmfException e) {
            showError("Could not create Measures tab.");
        }
        
        return measuresTabView;
    }
    
    private JPanel createErrorTab(String message) {
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

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    protected void save() throws EmfException {
        clearMessage();
        presenter.doSave();
        messagePanel
            .setMessage("Program was saved successfully.");
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
            //first check whether cs is running before checking the discard changes
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

    public void notifyLockFailure(ControlProgram controlProgram) {
        String message = "Cannot edit Properties of Control Program: " + controlProgram
                + System.getProperty("line.separator") + " as it was locked by User: " + controlProgram.getLockOwner()
                + "(at " + format(controlProgram.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
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

    public void startControlMeasuresRefresh() {
        if (measuresTabView != null)
            measuresTabView.startControlMeasuresRefresh();
    }

    public void endControlMeasuresRefresh() {
        if (measuresTabView != null)
            measuresTabView.endControlMeasuresRefresh();
    }

    public void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        //
    }

    public void stopRun()  {
        //
    }

    public void refresh(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub
        
    }
}
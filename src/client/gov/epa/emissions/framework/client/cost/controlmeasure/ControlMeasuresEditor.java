package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ControlMeasuresEditor extends DisposableInteralFrame implements ControlMeasuresEditorView {

    private ControlMeasuresEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    private static int count = 0;

    public ControlMeasuresEditor(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Control Measure Editor", new Dimension(700, 510), desktopManager);
        this.desktopManager = desktopManager;
        this.session = session;
    }

    private JTabbedPane createTabbedPane(ControlMeasure measure, MessagePanel messagePanel, String newOrEdit) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(measure, messagePanel, newOrEdit));

        tabbedPane.addTab("Efficiencies", createEfficiencyTab(measure, messagePanel, newOrEdit));

        tabbedPane.addTab("SCCs", createSCCTab(measure, messagePanel, newOrEdit));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private Component createSCCTab(ControlMeasure measure, MessagePanel messagePanel2, String newOrEdit) {
        return new JPanel();
    }

    private Component createEfficiencyTab(ControlMeasure measure, MessagePanel messagePanel2, String newOrEdit) {
        return new JPanel();
    }

    private JPanel createSummaryTab(ControlMeasure measure, MessagePanel messagePanel, String newOrEdit) {
        EditableCMSummaryTab view = new EditableCMSummaryTab(measure, session, messagePanel, this, newOrEdit);
        presenter.set(view);
        return view;
    }

    public void display(ControlMeasure measure, String newOrEdit) {
        setWindowTitle(newOrEdit, measure);
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(measure, messagePanel, newOrEdit), BorderLayout.CENTER);
        panel.add(createBottomPanel(newOrEdit), BorderLayout.PAGE_END);

        contentPane.add(panel);
        super.display();
        super.resetChanges();
    }

    private void setWindowTitle(String newOrEdit, ControlMeasure measure) {
        if(newOrEdit.equalsIgnoreCase("new")) {
            int temp = ++count;
            super.setTitle("New Control Measure " + temp);
            super.setName("newControlMeasure" + temp);
        }
        
        if(newOrEdit.equalsIgnoreCase("edit")) {
            super.setTitle("Edit Control Measure: " + measure.getName());
            super.setName("editControlMeasure" + measure.getId());
        }
        
        if(newOrEdit.equalsIgnoreCase("view")) {
            super.setTitle("View Control Measure: " + measure.getName());
            super.setName("viewControlMeasure" + measure.getId());
        }
    }

    private JPanel createBottomPanel(String newOrEdit) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(newOrEdit), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel(String newOrEdit) {
        JPanel buttonsPanel = new JPanel();

        Button save = new Button("Save", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        if (!newOrEdit.equalsIgnoreCase("view"))
            buttonsPanel.add(save);

        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    public void observe(ControlMeasuresEditorPresenter presenter) {
        this.presenter = presenter;
    }

    // FIXME: should this be mandatory for all EmfViews ?
    public void showError(String message) {
        // TODO: error should go away at some point. when ?
        messagePanel.setError(message);
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
        try {
            presenter.doSave();
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    public void notifyLockFailure(ControlMeasure measure) {
        String message = "Cannot edit Properties of ControlMeasure: " + measure.getName()
                + System.getProperty("line.separator") + " as it was locked by User: " + measure.getLockOwner()
                + "(at " + dateFormat.format(measure.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

}

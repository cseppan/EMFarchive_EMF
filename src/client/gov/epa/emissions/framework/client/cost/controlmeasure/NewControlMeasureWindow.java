package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.ErrorPanel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class NewControlMeasureWindow extends DisposableInteralFrame implements ControlMeasureView {

    private ControlMeasurePresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parent;
    
    private static int count = 0;

    //private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

    public NewControlMeasureWindow(EmfConsole parent, EmfSession session, DesktopManager desktopManager) {
        super("New Control Measure", new Dimension(700, 510), desktopManager);
        this.desktopManager = desktopManager;
        this.session = session;
        this.parent = parent;
    }

    public void display(ControlMeasure measure) {
        setWindowTitle();
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(measure, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        contentPane.add(panel);
        super.display();
        super.resetChanges();
    }

    private JTabbedPane createTabbedPane(ControlMeasure measure, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(measure, messagePanel));

        tabbedPane.addTab("Efficiencies", createEfficiencyTab(measure, messagePanel));

        tabbedPane.addTab("SCCs", createSCCTab(measure, messagePanel));
        
        tabbedPane.addTab("Costs", createCostsTab(measure, messagePanel));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createSCCTab(ControlMeasure measure, MessagePanel messagePanel) {
        EditableCMTabView view;
        try {
            view = new EditableCMSCCTab(measure, session,this, messagePanel, parent);
            presenter.set(view);
            return (JPanel) view;
        } catch (EmfException e) {
            return new ErrorPanel("Could not create SCC tab\n"+e.getMessage());
        }
        
    }

    private Component createEfficiencyTab(ControlMeasure measure, MessagePanel messagePanel) {
        EditableEfficiencyTab view = new EditableEfficiencyTab(measure, this, parent, desktopManager, messagePanel);
        presenter.set(view);
        
        return view;
    }

    private JPanel createSummaryTab(ControlMeasure measure, MessagePanel messagePanel) {
        EditableCMSummaryTab view = new EditableCMSummaryTab(measure, session, messagePanel, this);
        view.populateDefaultValues();
        presenter.set(view);
        return view;
    }
    
    private Component createCostsTab(ControlMeasure measure, MessagePanel messagePanel) {
        EditableCostsTabView view = new EditableCostsTab(measure, this, parent, desktopManager, messagePanel);
        presenter.set(view);
        return (JPanel) view;
    }

    private void setWindowTitle() {
        int number = ++count;
        super.setTitle("New Control Measure: " + number);
        super.setName("newControlMeasure" + number);
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
                doSave();
            }
        });

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

    public void observe(ControlMeasurePresenter presenter) {
        this.presenter = presenter;
    }

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
        // NOTE Auto-generated method stub
    }
    
}

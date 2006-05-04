package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditControlStrategyWindow extends DisposableInteralFrame implements EditControlStrategyView {

    private ControlStrategy controlStrategy;

    private JPanel layout;

    private EditControlStrategyPresenter presenter;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextArea description;

    public EditControlStrategyWindow(ControlStrategy controlStrategy, DesktopManager desktopManager) {
        super("Edit a Control Strategy", new Dimension(500, 200), desktopManager);
        this.controlStrategy = controlStrategy;

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);

    }

    public void observe(EditControlStrategyPresenter presenter) {
        this.presenter = presenter;

    }

    public void display() {
        super.setLabel("Edit a ControlStrategy");
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel());
        layout.add(createButtonsPanel());
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", 30);
        addChangeable(name);
        name.setText(controlStrategy.getName());
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        description = new TextArea("Description", "", 30, 4);
        addChangeable(description);
        description.setText(controlStrategy.getDescription());
        layoutGenerator.addLabelWidgetPair("Description:",
                ScrollableComponent.createWithVerticalScrollBar(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
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

        panel.add(container, BorderLayout.CENTER);

        return panel;
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
        DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        return dateFormat.format(lockDate);
    }

    public void update(ControlStrategy controlStrategy) {
        controlStrategy.setName(name.getText());
        controlStrategy.setDescription(description.getText());
    }

}

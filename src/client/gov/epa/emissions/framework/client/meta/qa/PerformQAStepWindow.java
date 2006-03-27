package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.FormattedTextField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class PerformQAStepWindow extends DisposableInteralFrame implements PerformQAStepView {

    private EditableComboBox program;

    private TextArea programArguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private PerformQAStepPresenter presenter;

    private QAStep step;

    private TextField who;

    private TextArea result;

    private TextField status;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private FormattedTextField when;

    public PerformQAStepWindow(DesktopManager desktopManager) {
        super("Perform QA Step", new Dimension(550, 500), desktopManager);
    }

    public void display(QAStep step, EmfDataset dataset) {
        this.step = step;
        super.setLabel(super.getTitle() + ": " + step.getName());

        JPanel layout = createLayout(step, dataset);
        super.getContentPane().add(layout);
        super.display();
    }

    public void observe(PerformQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout(QAStep step, EmfDataset dataset) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel(step, dataset));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStep step, EmfDataset dataset) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name", new Label(step.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Dataset", new Label(dataset.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Version", new Label(step.getVersion() + ""), panel);

        String[] programs = { "EmisView", "Smkreport", "Smkinven" };
        program = new EditableComboBox(programs);
        program.setSelectedItem(step.getProgram());
        addChangeable(program);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programArguments = new TextArea("", step.getProgramArguments(), 40, 2);
        addChangeable(programArguments);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programArguments);
        layoutGenerator.addLabelWidgetPair("Arguments", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        CheckBox required = new CheckBox("", false);
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 4);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        addChangeable(description);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        String username = step.getWho() != null ? step.getWho().getUsername() : "";
        who = new TextField("who", username, 20);
        addChangeable(who);
        layoutGenerator.addLabelWidgetPair("User", who, panel);

        when = new FormattedTextField("startDateTime", step.getWhen(), DATE_FORMATTER, messagePanel);
        addChangeable(when);
        layoutGenerator.addLabelWidgetPair("Date", when, panel);

        result = new TextArea("Comment", step.getResult(), 40, 2);
        addChangeable(result);
        layoutGenerator.addLabelWidgetPair("Comment", result, panel);

        status = new TextField("Status", step.getStatus(), 20);
        addChangeable(status);
        layoutGenerator.addLabelWidgetPair("Status", status, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 12, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions();
            }

            public void keyReleased(KeyEvent e) {
                keyActions();
            }

            public void keyPressed(KeyEvent e) {
                keyActions();
            }
        };
    }

    private void keyActions() {
        try {
            messagePanel.clear();
            Float.parseFloat(order.getText());
        } catch (NumberFormatException ex) {
            messagePanel.setError("Order should be a floating point number");
        }
    }

    private AbstractAction orderAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        };
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected void doClose() {
        presenter.doClose();
    }

    public void doEdit() {
        step.setProgram((String) program.getSelectedItem());
        step.setProgramArguments(programArguments.getText());
        step.setOrder(Float.parseFloat(order.getText()));
        step.setDescription(description.getText().trim());

        step.setStatus(status.getText());
        step.setResult(result.getText());

        doClose();
    }

}

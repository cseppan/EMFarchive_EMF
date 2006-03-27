package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.FormattedTextField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
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

public class PerformQAStepWindow extends EmfInternalFrame implements PerformQAStepView {

    private EditableComboBox program;

    private TextArea programParameters;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private PerformQAStepPresenter presenter;

    private QAStep step;

    private TextField who;

    private TextField result;

    private TextField status;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private FormattedTextField when;

    public PerformQAStepWindow(DesktopManager desktopManager) {
        super("Perform QA Step", desktopManager);
        super.setSize(new Dimension(550, 350));

        super.setResizable(false);
    }

    public void display(QAStep step) {
        this.step = step;
        super.setTitle(super.getTitle() + ": " + step.getName());

        JPanel layout = createLayout(step);
        super.getContentPane().add(layout);
        super.display();
    }

    public void windowClosing() {
        doClose();
    }

    public void observe(PerformQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel createLayout(QAStep step) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel(step));
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        String[] defaultProgram = { "EmisView", "Smkreport", "Smkinven" };

        layoutGenerator.addLabelWidgetPair("Name", new Label(step.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Dataset", new Label(step.getDatasetId() + ""), panel);
        layoutGenerator.addLabelWidgetPair("Version", new Label(step.getVersion() + ""), panel);

        program = new EditableComboBox(defaultProgram);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Arguments", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, orderAction());
        order.setText(step.getOrder() + "");
        order.addKeyListener(keyListener());
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        CheckBox required = new CheckBox("", false);
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 10);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        who = new TextField("who", step.getWho().getUsername(), 40);
        layoutGenerator.addLabelWidgetPair("User", who, panel);

        when = new FormattedTextField("startDateTime", step.getWhen(), DATE_FORMATTER, messagePanel);
        layoutGenerator.addLabelWidgetPair("Date", when, panel);

        result = new TextField("Comment", step.getResult(), 40);
        layoutGenerator.addLabelWidgetPair("Comment", result, panel);

        status = new TextField("Status", step.getStatus(), 40);
        layoutGenerator.addLabelWidgetPair("Status", status, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
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
        step.setProgramArguments(programParameters.getText());
        step.setOrder(Float.parseFloat(order.getText()));
        step.setDescription(description.getText().trim());

        step.setStatus(status.getText());
        step.setResult(result.getText());
    }

}

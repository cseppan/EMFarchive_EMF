package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.FormattedDateField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class SetQAStatusDialog extends Dialog implements SetQAStatusView {

    private boolean shouldSetStatus;

    private JComboBox status;

    private JTextField who;

    private FormattedDateField when;

    private JTextArea comment;

    private EmfConsole parent;

    private SingleLineMessagePanel messagePanel;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private String[] statusList = { "Start", "Skipped", "In Progress", "Complete", "Failure" };

    public SetQAStatusDialog(EmfConsole parent) {
        super("Status for Selected QA Steps", parent);
        super.setSize(new Dimension(550, 350));
        super.center();

        this.parent = parent;
    }

    public void display() {
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        status = new JComboBox(statusList);
        layoutGenerator.addLabelWidgetPair("Status", status, panel);

        when = new FormattedDateField("When", new Date(), DATE_FORMATTER, messagePanel);
        layoutGenerator.addLabelWidgetPair("When", when, panel);

        who = new JTextField(parent.getUser().getName(), 40);
        layoutGenerator.addLabelWidgetPair("Who", who, panel);

        comment = new TextArea("", "", 40, 10);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        JScrollPane commentPane = new ScrollableComponent(comment);
        layoutGenerator.addLabelWidgetPair("Comment", commentPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setStatus();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldSetStatus = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void setStatus() {
        shouldSetStatus = true;
        close();
    }

    public QAStep qaStepStub() {
        QAStep step = new QAStep();
        step.setStatus(status.getSelectedItem().toString());
        step.setWhen(when.value());
        step.setWho(who.getText());
        step.setResult(comment.getText());

        return step;
    }

    public boolean shouldSetStatus() {
        return shouldSetStatus;
    }

}

package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.FormattedDateField;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;


public class SetQAStatusWindow extends DisposableInteralFrame implements SetQAStatusView {

    private ComboBox status;

    private JTextField who;

    private FormattedDateField date;

    private JTextArea comment;

    private SingleLineMessagePanel messagePanel;

    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    private QASteps steps;

    private User user;

    private SetQAStatusPresenter presenter;

    public SetQAStatusWindow(DesktopManager desktop) {
        super("Status for Selected QA Steps", new Dimension(550, 350), desktop);
    }

    public void display(QAStep[] steps, User user) {
        this.steps = new QASteps(steps);
        this.user = user;

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

        layoutGenerator.addLabelWidgetPair("Steps", new Label(steps.namesList()), panel);

        layoutGenerator.addLabelWidgetPair("Status", status(), panel);

        date = new FormattedDateField("When", new Date(), DATE_FORMATTER, messagePanel);
        layoutGenerator.addLabelWidgetPair("When", date, panel);

        who = new TextField(user.getName(), 40);
        layoutGenerator.addLabelWidgetPair("Who", who, panel);

        comment = new TextArea("", "", 40, 4);
        comment.setLineWrap(true);
        comment.setWrapStyleWord(true);
        JScrollPane commentPane = new ScrollableComponent(comment);
        layoutGenerator.addLabelWidgetPair("Comment", commentPane, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private ComboBox status() {
        QAProperties qaProperties = new QAProperties();
        status = new ComboBox("Choose a status", qaProperties.status());

        status.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                date.setValue(new Date());
            }
        });

        return status;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doOk();
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

    private void doOk() {
        for (int i = 0; i < steps.size(); i++) {
            QAStep step = steps.get(i);
            step.setStatus(status.getSelectedItem().toString());
            step.setDate(date.value());
            step.setWho(who.getText());

            step.setComments(currentComment(step) + comment.getText());
        }
        presenter.doSave();
    }

    private String currentComment(QAStep step) {
        return step.getComments() != null ? step.getComments() + System.getProperty("line.separator") : "";
    }

    private void doClose() {
        presenter.doClose();
    }

    public void observe(SetQAStatusPresenter presenter) {
        this.presenter = presenter;
    }

}

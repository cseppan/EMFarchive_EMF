package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.ParseException;
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

public class QAStatusDialog extends Dialog implements QAStatusView {

    private boolean shouldSetStatus;

    private JComboBox status;

    private JTextField who;
    
    private JTextField when;
    
    private JTextArea comment;
    
    private EmfConsole parent;
    
    private SingleLineMessagePanel messagePanel;
    
    private String[] statusList = {"Start", "Skipped", "In Progress", "Complete", "Failure" };

    public QAStatusDialog(EmfConsole parent) {
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

        when = new JTextField(new Date().toString(), 40);
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
        step.setWhen(getDate());
        step.setWho(getUser());
        step.setResult(comment.getText());
        
        return step;
    }

    private User getUser() {
        User user = new User();
        user.setName(who.getText());
        
        return user;
    }

    private Date getDate() {
        String dateformat = "MM/dd/yyyy hh:mm";
        String time = when.getText();
        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
        try {
            Date date = sdf.parse(time);
            return date;
        } catch (ParseException e) {
            messagePanel.setError("Date format should be MM/dd/yyyy hh:mm");
            return null;
        }
    }

    public boolean shouldSetStatus() {
        return shouldSetStatus;
    }

}

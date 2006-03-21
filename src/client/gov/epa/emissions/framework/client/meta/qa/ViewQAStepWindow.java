package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewQAStepWindow extends DisposableInteralFrame implements ViewQAStepView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private CheckBox required;

    private TextField order;
    
    private TextField when;
    
    private TextField who;
    
    private TextField status;

    private TextField version;

    private JPanel layout;

    private TextArea result;

    private SingleLineMessagePanel messagePanel;

    public ViewQAStepWindow(String title, DesktopManager desktopManager) {
        super("View QA Step", new Dimension(550, 450), desktopManager);
        super.setTitle(super.getTitle() + ": " + title);
        super.setName(super.getTitle() + ": " + title);
        layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(inputPanel());
        panel.add(buttonsPanel(), BorderLayout.PAGE_END);

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        name.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        version = new TextField("", 40);
        version.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Version", version, panel);
        
        program = new TextField("", 40);
        program.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        programParameters.setEditable(false);
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        order = new TextField("", 40);
        order.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        required = new CheckBox("required");
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        when = new TextField("", 40);
        when.setEditable(false);
        layoutGenerator.addLabelWidgetPair("When", when, panel);
        
        who = new TextField("", 40);
        who.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Who", who, panel);
        
        status = new TextField("", 40);
        status.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Status", status, panel);
        
        result = new TextArea("", "", 40, 8);
        result.setEditable(false);
        ScrollableTextArea scrollableDesc = ScrollableTextArea.createWithVerticalScrollBar(result);
        layoutGenerator.addLabelWidgetPair("Result", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 10, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        return panel;
    }

    public void display(QAStep step) {
        version.setText(step.getVersion() + "");
        name.setText(step.getName());
        program.setText(step.getProgram());
        programParameters.setText(step.getProgramArguments());
        required.setSelected(step.isRequired());
        order.setText(step.getOrder() + "");
        if(step.getWhen() != null)
            when.setText(step.getWhen().toString());
        if(step.getWho() != null)
            who.setText(step.getWho().getName());
        status.setText(step.getStatus());
        result.setText(step.getResult());
    }
    
}

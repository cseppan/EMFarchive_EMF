package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.QAStep;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewQAStepWindow extends DisposableInteralFrame implements QAStepView {
    private JPanel layout;

    public ViewQAStepWindow(DesktopManager desktopManager) {
        super("View QA Step", new Dimension(550, 450), desktopManager);

        layout = new JPanel();
        super.getContentPane().add(layout);
    }

    public void display(QAStep step) {
        super.setLabel(super.getTitle() + " : " + step.getName());
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        layout.add(inputPanel(step));
        layout.add(buttonsPanel(), BorderLayout.PAGE_END);

        super.display();
    }

    private JPanel inputPanel(QAStep step) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        TextField name = new TextField("", step.getName(), 40);
        name.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        TextField version = new TextField("", step.getVersion() + "", 40);
        version.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Version", version, panel);

        TextField program = new TextField("", step.getProgram(), 40);
        program.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        TextArea programParameters = new TextArea("", step.getProgramArguments(), 40, 3);
        programParameters.setEditable(false);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        TextField order = new TextField("", step.getOrder() + "", 40);
        order.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        CheckBox required = new CheckBox("", step.isRequired());
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        TextField when = new TextField("", 40);
        if (step.getWhen() != null)
            when.setText(step.getWhen().toString());
        when.setEditable(false);
        layoutGenerator.addLabelWidgetPair("When", when, panel);

        TextField who = new TextField("", step.getWho(), 40);
        who.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Who", who, panel);

        TextField status = new TextField("", step.getStatus(), 40);
        status.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Status", status, panel);

        TextArea result = new TextArea("", step.getResult(), 40, 10);
        result.setLineWrap(true);
        result.setWrapStyleWord(true);
        result.setEditable(false);
        ScrollableComponent scrollableComment = ScrollableComponent.createWithVerticalScrollBar(result);
        layoutGenerator.addLabelWidgetPair("Comment", scrollableComment, panel);

        TextArea description = new TextArea("", step.getDescription(), 40, 10);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        result.setEditable(false);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 11, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button close = new Button("Close", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        getRootPane().setDefaultButton(close);
        panel.add(close);

        return panel;
    }

}

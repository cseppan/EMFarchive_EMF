package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableQAStepTemplateWindow extends DisposableInteralFrame implements ViewableQAStepTemplateView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private CheckBox required;

    private TextField order;

    private JPanel layout;

    private String title;

    private TextArea description;

    public ViewableQAStepTemplateWindow(String title, DesktopManager desktopManager) {
        super("QA Step Template", new Dimension(550, 300), desktopManager);
        this.title = title;
    }

    public void display(DatasetType type, QAStepTemplate template) {
        super.setTitle(super.getTitle() + ": " + type.getName() + " " + title);
        super.setName(super.getTitle() + ": " + type.getName() + " " + title);
        layout = createLayout(type);
        display(template);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(DatasetType type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel(type));

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        name.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

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

        description = new TextArea("", "", 40, 3);
        description.setEditable(false);
        ScrollableTextArea scrollableDesc = ScrollableTextArea.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel(final DatasetType type) {
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

    public void display(QAStepTemplate template) {
        name.setText(template.getName());
        program.setText(template.getProgram());
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setText(template.getOrder() + "");
        description.setText(template.getDescription());
    }
    
}

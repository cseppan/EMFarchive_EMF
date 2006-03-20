package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewQAStepTemplateDialog extends Dialog implements NewQAStepTemplateView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private JCheckBox required;

    private NumberFormattedTextField order;

    private TextArea description;

    private boolean shouldCreate;

    private SingleLineMessagePanel messagePanel;

    public NewQAStepTemplateDialog(EmfConsole parent) {
        super("New QA Step Template", parent);
        super.setSize(new Dimension(550, 300));
        super.center();
    }

    public void display(DatasetType type) {
        super.setTitle(super.getTitle() + ": " + type.getName());

        JPanel layout = createLayout(type);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(DatasetType type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        panel.add(inputPanel());
        panel.add(buttonsPanel(type));

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        program = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        });
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        required = new JCheckBox();
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 3);
        ScrollableTextArea scrollableDesc = ScrollableTextArea.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    protected boolean verifyInput(DatasetType type) {
        String templatename = name.getText().trim();
        if (templatename.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter Name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (duplicate(templatename, type)) {
            JOptionPane.showMessageDialog(super.getParent(), "Name is duplicate. Please enter a different name.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean duplicate(String name, DatasetType type) {
        QAStepTemplate[] templates = type.getQaStepTemplates();
        for (int i = 0; i < templates.length; i++) {
            if (templates[i].getName().equals(name))
                return true;
        }

        return false;
    }

    private JPanel buttonsPanel(final DatasetType type) {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNew(type);
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doNew(DatasetType type) {
        if (verifyInput(type)) {
            shouldCreate = true;
            close();
        }
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public QAStepTemplate template() {
        QAStepTemplate template = new QAStepTemplate();
        template.setName(name.getText().trim());
        template.setProgram(program.getText().trim());
        template.setProgramArguments(programParameters.getText());
        template.setRequired(required.isSelected());

        if (!order.isEmpty())
            template.setOrder(Float.parseFloat(order.getText()));

        template.setDescription(description.getText().trim());

        return template;
    }

}

package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
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
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditQAStepTemplateWindow extends DisposableInteralFrame implements EditQAStepTemplateView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private JCheckBox required;

    private TextField order;

    private boolean shouldCreate;

    private JPanel layout;

    private String title;

    private EditQAStepTemplatesPresenter presenter;

    private QAStepTemplate template;

    public EditQAStepTemplateWindow(String title, DesktopManager desktopManager) {
        super("Edit QA Step Template", new Dimension(550, 300), desktopManager);
        this.title = title;
    }

    public void display(DatasetType type) {
        super.setTitle(super.getTitle() + ": " + type.getName() + " " + title);
        super.setName(super.getTitle() + ": " + type.getName() + " " + title);
        layout = createLayout(type);
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
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        program = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        order = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        required = new JCheckBox();
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private boolean verifyInput(DatasetType type) {
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
            if (templates[i].getName().equals(name) && !template.getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    private JPanel buttonsPanel(final DatasetType type) {
        JPanel panel = new JPanel();
        Button ok = new Button("OK", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit(type);
                close();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new Button("Cancel", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doEdit(DatasetType type) {
        verifyInput(type);
        presenter.doEdit(this);
    }

    public void windowClosing() {
        super.close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditQAStepTemplatesPresenter presenter) {
        this.presenter = presenter;
    }

    public void setTemplate() {
        if (template != null) {
            template.setName(name.getText().trim());
            template.setProgram(program.getText().trim());
            template.setProgramArguments(programParameters.getText());
            template.setRequired(required.isSelected());
            template.setOrder(Float.parseFloat(order.getText()));
        }
    }

    public void setEditTemplate(QAStepTemplate template) {
        this.template = template;
        name.setText(template.getName());
        program.setText(template.getProgram());
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setText(template.getOrder() + "");
    }

}

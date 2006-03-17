package gov.epa.emissions.framework.client.meta.qa;

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
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditQAStepTemplateWindow extends DisposableInteralFrame implements EditQAStepTemplateView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private CheckBox required;

    private NumberFormattedTextField order;

    private boolean shouldCreate;

    private JPanel layout;

    private String title;

    private EditQAStepTemplatesPresenter presenter;

    private QAStepTemplate template;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

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
        addChangeable(name);
        name.addKeyListener();
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        program = new TextField("", 40);
        addChangeable(program);
        program.addKeyListener();
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        addChangeable(programParameters);
        programParameters.addKeyListener();
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
        addChangeable(order);
        order.addKeyListener();
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        required = new CheckBox("required");
        addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 3);
        addChangeable(description);
        description.addKeyListener();
        ScrollableTextArea scrollableDesc = ScrollableTextArea.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
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
                checkChangesAndCloseWindow();
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
        checkChangesAndCloseWindow();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditQAStepTemplatesPresenter presenter) {
        this.presenter = presenter;
    }

    public void loadTemplate() {
        template.setName(name.getText().trim());
        template.setProgram(program.getText().trim());
        template.setProgramArguments(programParameters.getText());
        template.setRequired(required.isSelected());
        if (!order.isEmpty())
            template.setOrder(Float.parseFloat(order.getText()));
        template.setDescription(description.getText().trim());
    }

    public void display(QAStepTemplate template) {
        this.template = template;
        name.setText(template.getName());
        program.setText(template.getProgram());
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setText(template.getOrder() + "");
        description.setText(template.getDescription());
    }
    
    private void checkChangesAndCloseWindow() {
        if (checkChanges())
            super.close();
    }

}

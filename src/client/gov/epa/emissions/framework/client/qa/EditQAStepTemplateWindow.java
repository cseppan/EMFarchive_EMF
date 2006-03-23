package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditQAStepTemplateWindow extends DisposableInteralFrame implements EditQAStepTemplateView {
    private TextField name;

    private EditableComboBox program;

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
    
    private Button ok;

    public EditQAStepTemplateWindow(String title, DesktopManager desktopManager) {
        super("Edit QA Step Template", new Dimension(550, 380), desktopManager);
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
        String[] defaultProgram = { "EmisView", "Smkreport", "Smkinven" };

        name = new TextField("", 40);
        addChangeable(name);
        name.addKeyListener();
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        program = new EditableComboBox(defaultProgram);
        addChangeable(program);
        layoutGenerator.addLabelWidgetPair("Program", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        addChangeable(programParameters);
        programParameters.addKeyListener();
        ScrollableTextArea scrollableDetails = ScrollableTextArea.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, getFormatAction());
        
        addChangeable(order);
        order.addEditTrackingListener();
        order.addKeyListener(getKeyVerifyListener());
        layoutGenerator.addLabelWidgetPair("Order", order, panel);

        required = new CheckBox("");
        addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 10);
        addChangeable(description);
        description.addKeyListener();
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ScrollableTextArea scrollableDesc = ScrollableTextArea.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private AbstractAction getFormatAction() {
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

    private KeyListener getKeyVerifyListener() {
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
            ok.setEnabled(true);
        } catch (NumberFormatException ex) {
            ok.setEnabled(false);
            messagePanel.setError("Order should be a floating point number");
        }
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

        ok = new Button("OK", new AbstractAction() {
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
        try {
            validateOrder();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        template.setName(name.getText().trim());
        template.setProgram((String)program.getSelectedItem());
        template.setProgramArguments(programParameters.getText());
        template.setRequired(required.isSelected());
        template.setOrder(Float.parseFloat(order.getText()));
        template.setDescription(description.getText().trim());
    }

    public void display(QAStepTemplate template) {
        this.template = template;
        name.setText(template.getName());
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setText(template.getOrder() + "");
        description.setText(template.getDescription());
    }
    
    private void validateOrder() throws EmfException {
        if(!order.isValid() || order.isEmpty()) 
            throw new EmfException("Order field can only be a number");
    }
    
    private void checkChangesAndCloseWindow() {
        if (checkChanges())
            super.close();
    }

}

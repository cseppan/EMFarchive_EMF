package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditQAStepTemplateWindow extends DisposableInteralFrame implements EditQAStepTemplateView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditQAStepTemplatesPresenter presenter;

    private QAStepTemplate template;

    private MessagePanel messagePanel;

    private Button ok;

    private QAStepTemplatePanel templatePanel;

    public EditQAStepTemplateWindow(DesktopManager desktopManager) {
        super("Edit QA Step Template", new Dimension(550, 480), desktopManager);
    }

    public void display(DatasetType type) {
        super.setLabel(super.getTitle() + ": " + type.getName());
        layout = createLayout(type);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(DatasetType type) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.templatePanel = new QAStepTemplatePanel(messagePanel, this);
        panel.add(templatePanel);
        panel.add(buttonsPanel(type));

        return panel;
    }

    private boolean verifyInput(DatasetType type) {
        String templatename = templatePanel.getTemplateName().trim();
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
                doClose();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doEdit(DatasetType type) {
        clearMessage();
        verifyInput(type);
        try {
            presenter.doEdit_WRONG_PRESENTER(this);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditQAStepTemplatesPresenter presenter) {
        this.presenter = presenter;
    }

    public void loadTemplate() {
        template.setName(templatePanel.getTemplateName().trim());
        template.setProgram(templatePanel.getProgramName());
        template.setProgramArguments(templatePanel.getProgramArgs());
        template.setRequired(templatePanel.getRequired());
        template.setOrder(Float.parseFloat(templatePanel.getOrder()));
        template.setDescription(templatePanel.getDescription().trim());
    }

    public void display(QAStepTemplate template) {
        this.template = template;
        templatePanel.setFields(template);
        resetChanges();
    }

    private void doClose() {
        if (shouldDiscardChanges())
            super.close();
    }

}

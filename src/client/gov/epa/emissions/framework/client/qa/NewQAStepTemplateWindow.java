package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class NewQAStepTemplateWindow extends DisposableInteralFrame implements NewQAStepTemplateView {

    private MessagePanel messagePanel;

    private QAStepTemplatePanel templatePanel;

    private NewQAStepTemplatePresenter presenter;

    public NewQAStepTemplateWindow(DesktopManager desktopManager) {
        super("New QA Step Template", new Dimension(550, 480), desktopManager);
    }

    public void display(DatasetType type) {
        super.setLabel(super.getTitle() + ": " + type.getName());
        JPanel layout = createLayout(type);
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

    protected boolean verifyInput(DatasetType type) {
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
                disposeView();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doNew(DatasetType type) {
        if (verifyInput(type)) {
            presenter.addNew(template());
            disposeView();
        }
    }

    public QAStepTemplate template() {
        QAStepTemplate template = new QAStepTemplate();
        template.setName(templatePanel.getTemplateName().trim());
        template.setProgram(templatePanel.getProgramName().trim());
        template.setProgramArguments(templatePanel.getProgramArgs());
        template.setRequired(templatePanel.getRequired());

        if (!templatePanel.getOrder().equals(""))
            template.setOrder(Float.parseFloat(templatePanel.getOrder()));

        template.setDescription(templatePanel.getDescription().trim());

        return template;
    }

    public void observe(NewQAStepTemplatePresenter presenter) {
        this.presenter = presenter;
    }

}

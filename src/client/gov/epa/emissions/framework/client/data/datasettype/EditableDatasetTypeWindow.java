package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepTemplatesPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class EditableDatasetTypeWindow extends DisposableInteralFrame implements EditableDatasetTypeView {

    private EditableDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextField sortOrder;

    private TextArea description;

    private EditableKeyValueTableData keywordsTableData;

    private DatasetTypeKeywordsPanel keywordsPanel;

    private DesktopManager desktopManager;

    private EmfConsole parent;

    public EditableDatasetTypeWindow(EmfConsole parent, DesktopManager desktopManager) {
        super("Edit Dataset Type", new Dimension(600, 500), desktopManager);

        this.desktopManager = desktopManager;
        this.parent = parent;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(EditableDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type, Keyword[] keywords) {
        super.setTitle("Edit Dataset Type: " + type.getName());
        super.setName("datasetTypeEditor:" + type.getId());

        layout.removeAll();
        doLayout(layout, type, keywords);

        super.display();
    }

    private void doLayout(JPanel layout, DatasetType type, Keyword[] keywords) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel(type));
        layout.add(createKeywordsPanel(type, keywords));
        layout.add(createQAStepTemplatesPanel(type));
        layout.add(createButtonsPanel());
    }

    private JPanel createInputPanel(DatasetType type) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", type.getName(), 40);
        addChangeable(name);
        name.addTextListener();
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        description = new TextArea("description", type.getDescription(), 40);
        addChangeable(description);
        description.addTextListener();
        ScrollableTextArea descScrollableTextArea = new ScrollableTextArea(description);
        descScrollableTextArea.setMinimumSize(new Dimension(80, 80));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, panel);

        sortOrder = new TextField("sortOrder", type.getDefaultSortOrder(), 40);
        addChangeable(sortOrder);
        sortOrder.addTextListener();
        layoutGenerator.addLabelWidgetPair("Default Sort Order:", sortOrder, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createKeywordsPanel(DatasetType type, Keyword[] keywords) {
        keywordsTableData = new EditableKeyValueTableData(type.getKeyVals(), new Keywords(keywords));
        keywordsPanel = new DatasetTypeKeywordsPanel(keywordsTableData, keywords, this);
        keywordsPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        return keywordsPanel;
    }

    private JPanel createQAStepTemplatesPanel(DatasetType type) {
        EditableQAStepTemplateTableData tableData = new EditableQAStepTemplateTableData(type.getQaStepTemplates());
        QAStepTemplatesPanel panel = new QAStepTemplatesPanel(type, tableData, this, parent, desktopManager);

        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, panel);
        presenter.display();

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new Button("Save", saveAction());
        container.add(saveButton);
        container.add(new Button("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (name.getText().equals(""))
                        messagePanel.setError("Name field should be a non-empty string.");
                    else {
                        keywordsPanel.commit();
                        resetChanges();
                        presenter.doSave(name.getText(), description.getText(), keywordsTableData.sources(), sortOrder
                                .getText());
                    }
                } catch (EmfException e) {
                    messagePanel.setError("Could not save: " + e.getMessage());
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        checkChangesAndCloseWindow();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                checkChangesAndCloseWindow();
            }
        };

        return action;
    }

    private void checkChangesAndCloseWindow() {
        try {
            if (checkChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

}

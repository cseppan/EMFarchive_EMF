package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueTableData;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.client.qa.EditQAStepTemplatesPresenter;
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

    private EditQAStepTemplatesPanel qaStepTemplatesPanel;

    public EditableDatasetTypeWindow(EmfConsole parent, DesktopManager desktopManager) {
        super("Edit Dataset Type", new Dimension(600, 550), desktopManager);

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
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        description = new TextArea("description", type.getDescription(), 40);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(new Dimension(80, 80));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, panel);

        sortOrder = new TextField("sortOrder", type.getDefaultSortOrder(), 40);
        addChangeable(sortOrder);
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
        qaStepTemplatesPanel = new EditQAStepTemplatesPanel(type, this, parent, desktopManager);
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenter(type, qaStepTemplatesPanel);
        presenter.display();

        return qaStepTemplatesPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
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
                doSave();
            }
        };

        return action;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        try {
<<<<<<< EditableDatasetTypeWindow.java
            if (shouldDiscardChanges())
                presenter.doClose();
=======
            presenter.doClose();
>>>>>>> 1.20
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private void doSave() {
        if (name.getText().equals("")) {
            messagePanel.setError("Name should be specified.");
            return;
        }

        resetChanges();
        try {
            keywordsPanel.commit();
            qaStepTemplatesPanel.commit();
            presenter.doSave(name.getText(), description.getText(), keywordsTableData.sources(), sortOrder.getText());
        } catch (EmfException e) {
            messagePanel.setError("Could not save: " + e.getMessage());
        }
    }

}

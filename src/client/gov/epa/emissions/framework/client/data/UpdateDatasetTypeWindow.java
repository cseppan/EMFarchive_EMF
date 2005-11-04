package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;

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

public class UpdateDatasetTypeWindow extends DisposableInteralFrame implements UpdateDatasetTypeView {

    private UpdateDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextArea description;

    private DatasetTypeKeywordsTableData keywordsTableData;

    private DatasetTypesManagerView manager;

    public UpdateDatasetTypeWindow(DatasetTypesManagerView manager) {
        super("Update DatasetType");

        this.manager = manager;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);

        super.setSize(new Dimension(400, 400));
    }

    public void observe(UpdateDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type, Keyword[] keywords) {
        layout.removeAll();
        doLayout(layout, type, keywords);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, DatasetType type, Keyword[] keywords) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel(type));
        layout.add(createKeywordsPanel(type, keywords));
        layout.add(createButtonsPanel());
    }

    private JPanel createInputPanel(DatasetType type) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", type.getName(), 20);
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        description = new TextArea("description", type.getDescription(), 25);
        layoutGenerator.addLabelWidgetPair("Description", new ScrollableTextArea(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createKeywordsPanel(DatasetType type, Keyword[] keywords) {
        keywordsTableData = new DatasetTypeKeywordsTableData(type.getKeywords(), new MasterKeywords(keywords));
        JPanel panel = new DatasetTypeKeywordsPanel(keywordsTableData, keywords);
        panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

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
                    presenter.doSave(name.getText(), description.getText(), keywordsTableData.sources(), manager);
                } catch (EmfException e) {
                    messagePanel.setError("Could not save. Reason: " + e.getMessage());
                }
            }
        };

        return action;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        };

        return action;
    }

}

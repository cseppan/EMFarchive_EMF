package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

public class ViewableDatasetTypeWindow extends DisposableInteralFrame implements ViewableDatasetTypeView {

    private ViewableDatasetTypePresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    public ViewableDatasetTypeWindow() {
        super("View DatasetType", new Dimension(600, 500));

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(ViewableDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type, Keyword[] keywords) {
        super.setTitle("View DatasetType: " + type.getName());
        layout.removeAll();
        doLayout(layout, type, keywords);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, DatasetType type, Keyword[] keywords) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createBasicDataPanel(type));
        layout.add(createKeywordsPanel(keywords));
        layout.add(createButtonsPanel());

        messagePanel.setMessage(lockStatus(type));
    }

    private String lockStatus(DatasetType type) {
        if (!type.isLocked())
            return "";

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        return "Locked by " + type.getLockOwner() + " at " + dateFormat.format(type.getLockDate());
    }

    private JPanel createBasicDataPanel(DatasetType type) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name", new Label("name", type.getName()), panel);

        TextArea description = new TextArea("description", type.getDescription(), 40);
        description.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Description", new ScrollableTextArea(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createKeywordsPanel(Keyword[] keywords) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        TableData tableData = new ViewableKeywordsTableData(keywords);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(20);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Button closeButton = new Button("Close", closeAction());
        panel.add(closeButton, BorderLayout.LINE_END);
        getRootPane().setDefaultButton(closeButton);

        return panel;
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

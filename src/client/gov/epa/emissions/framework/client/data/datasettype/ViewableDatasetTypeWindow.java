package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
    
    private DesktopManager desktopManager;

    public ViewableDatasetTypeWindow(DesktopManager desktopManager) {
        super("View Dataset Type", new Dimension(600, 500), desktopManager);

        this.desktopManager = desktopManager;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(ViewableDatasetTypePresenter presenter) {
        this.presenter = presenter;
    }

    public void display(DatasetType type) {
        super.setTitle("View Dataset Type: " + type.getName());
        super.setName("datasetTypeView:" + type.getId());

        layout.removeAll();
        doLayout(layout, type);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, DatasetType type) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createBasicDataPanel(type));
        layout.add(createKeywordsPanel(type.getKeyVals()));
        layout.add(createQAStepTemplatesPanel(type));
        layout.add(createButtonsPanel());

        messagePanel.setMessage(lockStatus(type));
    }

    private String lockStatus(DatasetType type) {
        if (!type.isLocked())
            return "";

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        return "Locked by User: " + type.getLockOwner() + " at " + dateFormat.format(type.getLockDate());
    }

    private JPanel createBasicDataPanel(DatasetType type) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        TextField name = new TextField("name", type.getName(), 40);
        name.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        TextArea description = new TextArea("description", type.getDescription(), 40);
        description.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMinimumSize(new Dimension(80, 80));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, panel);

        TextField sortOrder = new TextField("sortOrder", type.getDefaultSortOrder(), 40);
        sortOrder.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Default Sort Order:", sortOrder, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createKeywordsPanel(KeyVal[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords"));

        TableData tableData = new DatasetTypeKeyValueTableData(vals);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createQAStepTemplatesPanel(DatasetType type) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("QA Step Templates"));

        EditableQAStepTemplateTableData tableData = new EditableQAStepTemplateTableData(type.getQaStepTemplates());
        JTable table = new JTable(new EmfTableModel(tableData)){
            public String getToolTipText(MouseEvent e) { return getCellTip(e, this); }
        };
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createViewButton(tableData, type), BorderLayout.SOUTH);

        return panel;
    }
    
    private JPanel createViewButton(final EditableQAStepTemplateTableData data, final DatasetType type) {
        JPanel panel = new JPanel(new BorderLayout());
        
        Button view = new Button("View", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showTemplateWindows(data, type);
            }
        });
        
        panel.add(view, BorderLayout.LINE_START);
        
        return panel;
    }
    
    private String getCellTip(MouseEvent e, JTable table) {
        Point p = e.getPoint();
        int rowIndex = table.rowAtPoint(p);
        int colIndex = table.columnAtPoint(p);
        
        return table.getValueAt(rowIndex, colIndex).toString();
    }

    private void showTemplateWindows(EditableQAStepTemplateTableData data, DatasetType type) {
        QAStepTemplate[] selected = data.getSelected();
        
        for(int i = 0; i < selected.length; i++) {
            ViewableQAStepTemplateView view = new ViewableQAStepTemplateWindow(selected[i].getName(), desktopManager);
            ViewableQAStepTemplatePresenter presenter = new ViewableQAStepTemplatePresenter(view, selected[i], type);
            presenter.display();
        }
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

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

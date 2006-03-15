package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.client.meta.qa.NewQAStepTemplateDialog;
import gov.epa.emissions.framework.client.meta.qa.QAStepTemplatePanelPresenter;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DatasetTypeQAStepTemplatesPanel extends JPanel implements QAStepTemplatesPanelView,Editor {

    protected EditableEmfTableModel tableModel;

    protected EditableTable table;

    protected ManageChangeables changeablesList;
    
    protected DatasetType type;
    
    protected QAStepTemplatePanelPresenter presenter;
    
    private DesktopManager desktopManager;
    
    public DatasetTypeQAStepTemplatesPanel(DatasetType type, EditableQAStepTemplateTableData tableData, 
            ManageChangeables changeablesList, DesktopManager desktopManager) {
        this.changeablesList = changeablesList;
        this.type = type;
        this.desktopManager = desktopManager;
        super.setLayout(new BorderLayout());
        super.add(doLayout("QAStepTemplates", tableData), BorderLayout.CENTER);
    }

    private JPanel doLayout(String label, InlineEditableTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());

        container.add(labelPanel(label), BorderLayout.PAGE_START);
        container.add(table(tableData), BorderLayout.CENTER);
        container.add(buttonsPanel(tableData), BorderLayout.PAGE_END);

        return container;
    }

    private JPanel labelPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());

        Label label = new Label(name);
        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    protected JScrollPane table(InlineEditableTableData tableData) {
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        changeablesList.addChangeable(table);

        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
    }

    private JPanel buttonsPanel(final InlineEditableTableData tableData) {
        JPanel container = new JPanel();

        JButton add = new JButton("Add");
        add.setMargin(new Insets(2, 2, 2, 2));
        add.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.addBlankRow();
                refresh();
            }
        });
        container.add(add);

        JButton remove = new JButton("Remove");
        remove.setMargin(new Insets(2, 2, 2, 2));
        remove.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                tableData.removeSelected();
                refresh();
            }
        });
        container.add(remove);
        
        JButton update = new JButton("Update");
        update.setMargin(new Insets(2, 2, 2, 2));
        update.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                update();
            }
        });
        container.add(update);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void update() {
        int[] selectedRows = table.getSelectedRows();
        String title = "Create New QAStepTemplate: row(";
        for(int i = 0; i < selectedRows.length; i++) {
            title += selectedRows[i] + ")";
            NewQAStepTemplateDialog dialog = new NewQAStepTemplateDialog(title, desktopManager);
            presenter.doNewQAStepTemplate(dialog, type, selectedRows[i]);
        }
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }
    
    public void setColumnEditor(TableCellEditor editor, int columnIndex, String toolTip) {
        TableColumnModel colModel = table.getColumnModel();
        TableColumn col = colModel.getColumn(columnIndex);
        col.setCellEditor(editor);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText(toolTip);
        col.setCellRenderer(renderer);
    }

    public void invalidate() {
        setColumnWidths(table.getColumnModel());
        super.invalidate();
    }

    public void commit() {
        table.commit();
    }

    public void addListener(KeyListener keyListener) {
        table.addKeyListener(keyListener);
    }

    public void setTableData(QAStepTemplate template, int row) {
        tableModel.setValueAt(template.getName(), row, 1);
        tableModel.setValueAt(template.getProgram(), row, 2);
        tableModel.setValueAt(template.getProgramArguments(), row, 3);
        tableModel.setValueAt(Boolean.valueOf(template.isRequired()), row, 4);
        tableModel.setValueAt(template.getOrder(), row, 5);
    }

    public void observe(QAStepTemplatePanelPresenter presenter) {
        this.presenter = presenter;
    }

}
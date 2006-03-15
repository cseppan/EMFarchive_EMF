package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.BorderlessButton;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.qa.EditQAStepTemplatesPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepTemplateTableData;
import gov.epa.emissions.framework.client.meta.qa.NewQAStepTemplateDialog;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class EditQAStepTemplatesPanel extends JPanel implements EditQAStepTemplatesView, Editor {

    protected EditableEmfTableModel tableModel;

    protected EditableTable table;

    protected ManageChangeables changeablesList;

    protected DatasetType type;

    protected EditQAStepTemplatesPresenter presenter;

    private EditableQAStepTemplateTableData tableData;

    private EmfConsole parent;

    public EditQAStepTemplatesPanel(DatasetType type, EditableQAStepTemplateTableData tableData,
            ManageChangeables changeablesList, EmfConsole parent, DesktopManager desktopManager) {
        this.changeablesList = changeablesList;
        this.type = type;
        this.tableData = tableData;
        this.parent = parent;

        createLayout();
    }

    private void createLayout() {
        setBorder(new Border("QAStepTemplates"));
        super.setLayout(new BorderLayout());
        super.add(centerPanel(), BorderLayout.CENTER);
    }

    private JPanel centerPanel() {
        JPanel container = new JPanel(new BorderLayout());

        container.add(table(), BorderLayout.CENTER);
        container.add(buttonsPanel(), BorderLayout.PAGE_END);

        return container;
    }

    protected JScrollPane table() {
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        changeablesList.addChangeable(table);

        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
    }

    private JPanel buttonsPanel() {
        JPanel container = new JPanel();

        Button add = new BorderlessButton("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doAdd();
            }
        });
        container.add(add);

        Button remove = new BorderlessButton("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doRemove();
            }
        });
        container.add(remove);

        Button update = new BorderlessButton("Update", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
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
        for (int i = 0; i < selectedRows.length; i++) {
            title += selectedRows[i] + ")";
            // TODO: display the UpdateWindows
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
        type.setQaStepTemplates(tableData.sources());
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

    public void observe(EditQAStepTemplatesPresenter presenter) {
        this.presenter = presenter;
    }

    private void doRemove() {
        tableData.removeSelected();
        refresh();
    }

    private void doAdd() {
        presenter.doAdd(new NewQAStepTemplateDialog(parent));
    }

    public void add(QAStepTemplate template) {
        tableData.add(template);
        refresh();
    }

}
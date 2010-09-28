package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.awt.BorderLayout;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TargetPollutantsPanel extends JPanel implements Editor {
    protected EditableEmfTableModel tableModel;
    
    private String name;

    protected EditableTable table;

    protected ManageChangeables changeablesList;

    public TargetPollutantsPanel(String label, InlineEditableTableData tableData, ManageChangeables changeablesList, @SuppressWarnings("unused") EmfConsole parent) {
        this.changeablesList = changeablesList;
        super.setLayout(new BorderLayout());
        super.add(doLayout(tableData), BorderLayout.CENTER);
        this.name = label;
    }

    private JPanel doLayout(InlineEditableTableData tableData) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(table(tableData), BorderLayout.CENTER);

        return container;
    }

    protected JScrollPane table(InlineEditableTableData tableData) {
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        
        if (changeablesList == null) 
            table.setEnabled(false);
        else
            changeablesList.addChangeable(table);
        
        table.setRowHeight(16);
        
        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn col = model.getColumn(0);
        col.setMaxWidth(250);
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
    
    public String getId() {
        return name;
    }

}


package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.TableHeader;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class EmfTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    private EmfTableData tableData;

    public EmfTableModel(EmfTableData tableData) {
        refresh(tableData);
    }

    public int getRowCount() {
        return rows.size();
    }

    public String getColumnName(int index) {
        return header.columnName(index);
    }

    public int getColumnCount() {
        return header.columnsSize();
    }

    public Object getValueAt(int row, int column) {
        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void refresh() {
        this.rows = tableData.rows();
        super.fireTableDataChanged();
    }

    public boolean isCellEditable(int row, int col) {
        return tableData.isEditable(col);
    }

    public Object element(int row) {
        return tableData.element(row);
    }

    // FIXME: TBT (to be tested)
    public List elements(int[] selected) {
        return tableData.elements(selected);
    }

    // FIXME: how does this differ from refresh() ?
    public void refresh(EmfTableData tableData) {
        this.tableData = tableData;
        this.header = new TableHeader(tableData.columns());
        refresh();
    }

    public Class getColumnClass(int index) {
        return getValueAt(0, index).getClass();
    }

    public void setValueAt(Object value, int row, int col) {
        // TODO:
    }

}

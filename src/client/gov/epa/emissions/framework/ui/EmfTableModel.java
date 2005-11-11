package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.TableHeader;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class EmfTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    private TableData tableData;

    public EmfTableModel(TableData tableData) {
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
        this.header = new TableHeader(tableData.columns());

        super.fireTableDataChanged();
    }

    public void refresh(TableData tableData) {
        this.tableData = tableData;
        refresh();
    }

    public boolean isCellEditable(int row, int col) {
        return tableData.isEditable(col);
    }

    public Object element(int row) {
        return tableData.element(row);
    }

    public List elements(int[] selected) {
        return tableData.elements(selected);
    }

    public Class getColumnClass(int col) {
        return tableData.getColumnClass(col);
    }

    public void setValueAt(Object value, int row, int col) {
        Row rowObj = (Row) rows.get(row);
        rowObj.setValueAt(value, col);
    }

}

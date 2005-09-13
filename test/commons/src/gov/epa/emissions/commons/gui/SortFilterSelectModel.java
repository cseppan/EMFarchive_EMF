package gov.epa.emissions.commons.gui;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

/**
 * <p>
 * A table that adds the 'selectable' behavior to a MultiRowHeaderTableModel.
 * Uses the GOF's <pattern>Decorator</pattern> pattern. Delegates all behavior
 * (except selectable) to the underlying delegate model
 * </p>
 * 
 * @author Craig Mattocks
 * @version $Id: SortFilterSelectModel.java,v 1.6 2005/06/28 14:30:24 parthee
 *          Exp $
 *  
 */
public class SortFilterSelectModel extends MultiRowHeaderTableModel {

    private static final String SELECT_COL_NAME = "Select";

    private Boolean[] selects;

    private RefreshableTableModel delegate;

    public SortFilterSelectModel(RefreshableTableModel delegate) {
        this.delegate = delegate;

        resetSelections();

        setColumnHeaders(getDelegateColumnNames());
    }

    private void resetSelections() {
        this.selects = new Boolean[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            selects[i] = Boolean.FALSE;
        }
    }

    void setColumnHeaders(String[] columnNames) {
        String[][] columnHeaders = new String[1][];

        String[] firstColumnHeaderRow = new String[columnNames.length + 1];
        firstColumnHeaderRow[0] = SELECT_COL_NAME;
        System.arraycopy(columnNames, 0, firstColumnHeaderRow, 1, columnNames.length);

        columnHeaders[0] = firstColumnHeaderRow;

        // contains - Select + delegate columns
        super.columnHeaders = transposeArray(columnHeaders);

        super.columnRowHeaders = new String[1];
        super.columnRowHeaders[0] = "#";
    }

    public int getColumnCount() {
        return 1 + delegate.getColumnCount();
    }

    public String getColumnName(int col) {
        if (col == 0)
            return SELECT_COL_NAME;

        return delegate.getColumnName(col - 1);//minus the Select col
    }

    public int getRowCount() {
        return delegate.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            return selects[row];
        }

        return delegate.getValueAt(row, col - 1);
    }

    public int getBaseModelRowIndex(int rowIndex) {
        return rowIndex;
    }

    public boolean isCellEditable(int row, int col) {
        return (col == findColumn(SELECT_COL_NAME));//only 'Select' is editable
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            selects[row] = (Boolean) value;
            fireTableCellUpdated(row, col);
        }
    }

    public String getSelectableColumnName() {
        return SELECT_COL_NAME;
    }

    String[] getDelegateColumnNames() {
        List names = new ArrayList();
        for (int i = 0; i < delegate.getColumnCount(); i++) {
            names.add(delegate.getColumnName(i));
        }

        return (String[]) names.toArray(new String[0]);
    }

    public int[] getSelectedIndexes() {
        IntList indexes = new ArrayIntList();
        for (int i = 0; i < selects.length; i++) {
            if(selects[i] == Boolean.TRUE) indexes.add(i);
        }
        
        return indexes.toArray();
    }

    public void refresh() {
        delegate.refresh();
        resetSelections();        
    }
}

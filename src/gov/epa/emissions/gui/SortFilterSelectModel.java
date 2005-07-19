package gov.epa.emissions.gui;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;

import java.util.ArrayList;

/**
 * <p>
 * A table that adds the 'selectable' behavior to a MultiRowHeaderTableModel
 * </p>
 * 
 * @author Craig Mattocks
 * @version $Id: SortFilterSelectModel.java,v 1.6 2005/06/28 14:30:24 parthee
 *          Exp $
 *  
 */
public class SortFilterSelectModel extends MultiRowHeaderTableModel {

    /** The variable associated with the "Select" col */
    private static final String SELECT_COL_NAME = "Select";

    /**
     * Instance variables
     */
    /** datalist contains the EmisView objects */
    private Object[][] data;

    /** temporarly stores whether the rows are selected or not */
    private Boolean[] selValues;

    /**
     * @param data
     *            two dimensional array: Array of row data
     * @pre each row data of 'data' should be of equal length
     * @pre each column should have the same type as the first cell in the
     *      column
     */
    public SortFilterSelectModel(Object[][] data, String[] columnnHeaders) {
        setColHeaders(columnnHeaders);
        this.data = data;

        this.selValues = new Boolean[getRowCount()];

        for (int i = 0; i < getRowCount(); i++) {
            selValues[i] = new Boolean(false);
        }
    }

    private void setColHeaders(String[] attributes) {
        String[][] colHeaders = new String[1][];

        String[] tempColHeaders = new String[attributes.length + 1];
        tempColHeaders[0] = SELECT_COL_NAME;
        System.arraycopy(attributes, 0, tempColHeaders, 1, attributes.length);

        colHeaders[0] = tempColHeaders;

        super.columnHeaders = transposeArray(colHeaders);

        super.columnRowHeaders = new String[1];
        super.columnRowHeaders[0] = "#";
    }

    private void initializeSelVars() {
        selValues = new Boolean[getRowCount()];

        for (int i = 0; i < getRowCount(); i++) {
            selValues[i] = new Boolean(false);
        }
    }

    public int getColumnCount() {
        if (columnHeaders == null) {
            return 0;
        }

        return columnHeaders.length;
    }

    public String getColumnName(int col) {
        return super.columnHeaders[col][0];
    }

    public int getRowCount() {
        return data.length;
    }

    public Object getValueAt(int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            return selValues[row];
        } else if (col < findColumn(SELECT_COL_NAME)) {
            return data[row][col];
        }

        return data[row][col - 1];
    }

    public int getBaseModelRowIndex(int rowIndex) {
        return rowIndex;
    }

    public boolean isCellEditable(int row, int col) {
        return (col == findColumn(SELECT_COL_NAME));//only 'Select' is editable
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            selValues[row] = (Boolean) value;
            fireTableCellUpdated(row, col);
        }
    }

    public Object[] getSelectedValues(int col) {
        ArrayList result = new ArrayList();

        for (int i = 0; i < getRowCount(); i++) {
            if (selValues[i].booleanValue()) {
                result.add(getValueAt(i, col));
            }
        }

        return result.toArray();
    }

    public String getSelectableColumnName() {
        return SELECT_COL_NAME;
    }
}

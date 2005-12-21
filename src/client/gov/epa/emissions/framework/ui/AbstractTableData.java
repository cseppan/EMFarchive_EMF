package gov.epa.emissions.framework.ui;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTableData implements TableData {

    /**
     * sets the new value on the row + col. Override it, if any special handling is needed.
     */
    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        List rows = rows();

        Row rowObj = (Row) rows.get(row);
        rowObj.setValueAt(value, col);
    }

    public Object element(int row) {
        Row rowObj = (Row) rows().get(row);
        return rowObj.source();
    }

    public List elements(int[] rows) {
        List datasets = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            datasets.add(element(rows[i]));
        }

        return datasets;
    }
}

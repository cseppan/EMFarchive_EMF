package gov.epa.emissions.framework.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AbstractTableData implements TableData {

    private DateFormat dateFormat;

    public AbstractTableData() {
        dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    }

    final protected String format(Date date) {
        return (date == null) ? "N/A" : dateFormat.format(date);
    }

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

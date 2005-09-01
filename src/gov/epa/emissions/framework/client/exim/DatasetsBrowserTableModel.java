package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class DatasetsBrowserTableModel extends AbstractTableModel {

    private TableHeader header;

    private List rows;

    public DatasetsBrowserTableModel(Dataset[] datasets) {
        this.header = new TableHeader(new String[] { "Name", "Time Period", "Region", "Creator" });
        createRows(datasets);
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

    private void createRows(Dataset[] datasets) {
        this.rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            Row row = new Row(datasets[i]);
            rows.add(row);
        }
    }

    private class Column {

        private Object value;

        public Column(Object value) {
            this.value = value;
        }

    }

    private class Row {
        private Map columns;

        private Dataset dataset;

        public Row(Dataset dataset) {
            this.dataset = dataset;
            
            columns = new HashMap();
            columns.put(new Integer(0), new Column(dataset.getName()));
            columns.put(new Integer(1), new Column(dataset.getStartDateTime() + "-" + dataset.getStopDateTime()));
            columns.put(new Integer(2), new Column(dataset.getRegion()));
            columns.put(new Integer(3), new Column(dataset.getCreator()));
        }

        public Object getValueAt(int column) {
            Column columnHolder = (Column) columns.get(new Integer(column));
            return columnHolder.value;
        }
    }

}

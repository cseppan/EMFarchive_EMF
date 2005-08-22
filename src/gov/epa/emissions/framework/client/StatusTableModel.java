package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class StatusTableModel extends AbstractTableModel {

    private Status[] statuses;

    private TableHeader header;

    private List rows;

    public StatusTableModel() {
        this.header = new TableHeader(new String[] { "Username", "Message Type", "Message", "Timestamp" });
        this.statuses = new Status[0];
    }

    public int getRowCount() {
        return statuses.length;
    }

    public int getColumnCount() {
        return header.columnsSize();
    }

    public String getColumnName(int i) {
        return header.columnName(i);
    }

    public Object getValueAt(int row, int column) {
        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void refresh(Status[] statuses) {
        this.statuses = statuses;

        this.rows = new ArrayList();
        for (int i = 0; i < statuses.length; i++) {
            Row row = new Row(statuses[i]);
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

        public Row(Status status) {
            Column username = new Column(status.getUserName());
            Column messageType = new Column(status.getMessageType());
            Column message = new Column(status.getMessage());
            Column timestamp = new Column(status.getTimestamp());

            columns = new HashMap();
            columns.put(new Integer(0), username);
            columns.put(new Integer(1), messageType);
            columns.put(new Integer(2), message);
            columns.put(new Integer(3), timestamp);
        }

        public Object getValueAt(int column) {
            Column columnHolder = (Column) columns.get(new Integer(column));
            return columnHolder.value;
        }
    }

}

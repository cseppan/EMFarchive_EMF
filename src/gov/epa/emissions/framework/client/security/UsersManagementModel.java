package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UsersManagementModel extends AbstractTableModel {

    private HeaderRow header;

    private List rows;

    public UsersManagementModel(List users) {
        this.header = new HeaderRow(new String[] { "Select", "#", "Username",
                "Name", "Email" });

        this.rows = new ArrayList();
        int i = 1;
        for (Iterator iter = users.iterator(); iter.hasNext();) {
            User user = (User) iter.next();
            Row row = new Row(user, i++);
            rows.add(row);
        }
    }

    public String getColumnName(int i) {
        return header.columnName(i);
    }

    public int getColumnCount() {
        return header.columnsSize();
    }

    public int getRowCount() {
        return rows.size();
    }

    public Object getValueAt(int row, int column) {
        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void setValueAt(Object value, int row, int column) {
        Row rowObj = (Row) rows.get(row);
        rowObj.setValue(value, column);
    }

    public Class getColumnClass(int column) {
        if (column > getColumnCount())
            return null;

        return header.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {
        return (column == 0 || column == 3 || column == 4) ? true : false;
    }

    // inner classes
    private class HeaderRow {
        private String[] columnNames;

        public HeaderRow(String[] columnNames) {
            this.columnNames = columnNames;
        }

        public Class getColumnClass(int column) {
            return (column == 0) ? Boolean.class : String.class;
        }

        public int columnsSize() {
            return columnNames.length;
        }

        public String columnName(int i) {
            return columnNames[i];
        }
    }

    private class Row {
        private User user;

        private int index;

        private Boolean selected = Boolean.FALSE;

        public Row(User user, int index) {
            this.user = user;
            this.index = index;
        }

        public void setValue(Object value, int column) {
            switch (column) {
            case 0:
                this.selected = ((Boolean)value);
                break;
            case 3:
                user.setFullName((String) value);
                break;
            case 4:
                user.setEmailAddr((String) value);                
            }

        }

        public Object getValueAt(int column) {
            switch (column) {
            case 0:
                return selected;
            case 1:
                return "" + index;
            case 2:
                return user.getUserName();
            case 3:
                return user.getFullName();
            case 4:
                return user.getEmailAddr();
            }
            return null;
        }
    }

}

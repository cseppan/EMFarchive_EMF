package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UsersManagementModel extends AbstractTableModel {
    private String[] columnNames = { "Select", "#", "Username", "Name", "Email"};

    private List users;

    private Boolean selected;

    public UsersManagementModel(List users) {
        this.users = users;
        this.selected = Boolean.FALSE;
    }

    public String getColumnName(int i) {
        return columnNames[i];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return users != null ? users.size() : 0;
    }

    public Object getValueAt(int row, int column) {
        if (row > users.size())
            return null;

        User user = (User) users.get(row);

        switch (column) {
        case 0:
            return selected;//TODO: selected functionality
        case 1:
            return "" + (row + 1);
        case 2:
            return user.getUserName();
        case 3:
            return user.getFullName();
        case 4:
            return user.getEmailAddr();
        }

        return null;
    }

    public void setValueAt(Object value, int row, int column) {
        if (row > users.size())
            return;

        User user = (User) users.get(row);

        switch (column) {
        case 3:
            user.setFullName((String) value);
        case 4:
            user.setEmailAddr((String) value);
        }
    }

    public Class getColumnClass(int column) {
        if (column > 4)
            return null;

        switch (column) {
        case 0:
            return Boolean.class;
        case 1:
        case 2:
        case 3:
        case 4:
            return String.class;
        }

        return null;
    }
}

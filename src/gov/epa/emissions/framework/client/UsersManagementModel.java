package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UsersManagementModel extends AbstractTableModel {
    private String[] columnNames = { "#", "Username", "Name", "Email", "Select" };

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
            return "" + (row + 1);
        case 1:
            return user.getUserName();
        case 2:
            return user.getFullName();
        case 3:
            return user.getEmailAddr();
        case 4:
            return selected;//TODO: selected functionality
        }

        return null;
    }

    public void setValueAt(Object value, int row, int column) {
        if (row > users.size())
            return;

        User user = (User) users.get(row);

        switch (column) {
        case 2:
            user.setFullName((String) value);
        case 3:
            user.setEmailAddr((String) value);
        }
    }

}

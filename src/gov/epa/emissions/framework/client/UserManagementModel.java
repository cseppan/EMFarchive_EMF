package gov.epa.emissions.framework.client;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UserManagementModel extends AbstractTableModel {
    private String[] columnNames = { "#", "Username", "Name", "Email", "Group" };
    private List users;

    public UserManagementModel(List users) {
        this.users = users;
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

    public Object getValueAt(int arg0, int arg1) {
        return null;
    }
}

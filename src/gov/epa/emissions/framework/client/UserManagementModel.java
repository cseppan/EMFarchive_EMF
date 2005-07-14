package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UserManagementModel extends AbstractTableModel {
    private String[] columnNames = { "#", "Username", "Name", "Email" };
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

    public Object getValueAt(int row, int column) {
        if(users.size() < row)
            return null;
        
        User user = (User) users.get(row);
        
        switch(column) {
        	case 0: return "" + (row + 1);
        	case 1: return user.getUserName();        	
        	case 2: return user.getFullName();
        	case 3: return user.getEmailAddr();
        }
        
        return null;
    }
}

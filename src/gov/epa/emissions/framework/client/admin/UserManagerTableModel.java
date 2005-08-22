package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UserManagerTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    private UserServices userAdmin;

    public UserManagerTableModel(UserServices userAdmin) {
        this.header = new TableHeader(new String[] { "Username", "Name", "Email" });
        this.userAdmin = userAdmin;

        createRows(this.userAdmin);
    }

    public void refresh() {
        this.createRows(this.userAdmin);
    }

    private void createRows(UserServices admin) {
        this.rows = new ArrayList();
        User[] users;
        try {
            users = admin.getUsers();
        } catch (EmfException e) {// TODO: need to write exception handlers
            throw new RuntimeException("could not fetch users");
        }
        for (int i = 0; i < users.length; i++) {
            User user = users[i];
            Row row = new Row(user);
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
        return (column == 1 || column == 2) ? true : false;
    }

    private class Row {
        private User user;

        public Row(User user) {
            this.user = user;
        }

        public void setValue(Object value, int column) {
            switch (column) {
            case 1:
                try {
                    user.setFullName((String) value);
                } catch (UserException e) {
                    // TODO: attach a error handler
                    throw new RuntimeException(e.getMessage());
                }
                break;
            case 2:
                try {
                    user.setEmailAddr((String) value);
                } catch (UserException e) {
                    // TODO: attach a error handler
                    throw new RuntimeException(e.getMessage());
                }
            }
        }

        public Object getValueAt(int column) {
            switch (column) {
            case 0:
                return user.getUserName();
            case 1:
                return user.getFullName();
            case 2:
                return user.getEmailAddr();
            }

            return null;
        }
    }

    public User getUser(int index) {
        Row row = (Row) rows.get(index);

        return row.user;
    }

}

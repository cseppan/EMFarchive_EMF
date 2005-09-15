package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class UserManagerTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    private UserServices userAdmin;

    public UserManagerTableModel(UserServices userAdmin) {
        this.header = new TableHeader(new String[] { "Username", "Name", "Email", "Is Admin ?" });
        this.userAdmin = userAdmin;

        createRows(this.userAdmin);
    }

    public void refresh() {
        this.createRows(this.userAdmin);
        super.fireTableDataChanged();
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
        if (!isCellEditable(row, column))
            return;

        Row rowObj = (Row) rows.get(row);
        rowObj.setValue(value, column);
    }

    public Class getColumnClass(int column) {
        if (column > getColumnCount())
            return null;

        return header.getColumnClass(column);
    }

    public boolean isCellEditable(int row, int column) {
        return (column == 1 || column == 2 || column == 3) ? true : false;
    }

    private abstract class Column {

        private Object value;

        public Column(Object value) {
            this.value = value;
        }

        void setValue(Object value) {
            this.value = value;
            this.setUnderlyingValue(value);
        }

        abstract void setUnderlyingValue(Object value);
    }

    private class Row {
        private User user;

        private Map columns;

        public Row(final User user) {
            this.user = user;
            columns = new HashMap();

            columns.put(new Integer(0), new Column(user.getUsername()) {
                void setUnderlyingValue(Object value) {// uneditable
                }
            });

            columns.put(new Integer(1), new Column(user.getFullName()) {
                void setUnderlyingValue(Object value) {
                    try {
                        user.setFullName((String) value);
                    } catch (UserException e) {
                        throw new RuntimeException(e);// TODO: verify
                    }
                }
            });
            columns.put(new Integer(2), new Column(user.getEmail()) {
                void setUnderlyingValue(Object value) {
                    try {
                        user.setEmail((String) value);
                    } catch (UserException e) {
                        throw new RuntimeException(e);// TODO: verify
                    }
                }
            });
            columns.put(new Integer(3), new Column(Boolean.valueOf(user.isInAdminGroup())) {
                void setUnderlyingValue(Object value) {
                    Boolean bool = (Boolean) value;
                    user.setInAdminGroup(bool.booleanValue());
                }
            });
        }

        public void setValue(Object value, int column) {
            Column colObj = (Column) columns.get(new Integer(column));
            colObj.setValue(value);
        }

        public Object getValueAt(int column) {
            Column columnHolder = (Column) columns.get(new Integer(column));
            return columnHolder.value;
        }
    }

    public User getUser(int index) {
        Row row = (Row) rows.get(index);

        return row.user;
    }

}

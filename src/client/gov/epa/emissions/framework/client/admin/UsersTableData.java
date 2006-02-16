package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.SelectableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UsersTableData extends AbstractTableData {

    private List rows;

    private User[] values;

    public UsersTableData(User[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Select", "Username", "Name", "Email", "Is Admin ?" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;
        if (col == 4)
            return Boolean.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows(User[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(User user) {
        return new SelectableRow(new UserRowSource(user));
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public User[] selected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            SelectableRow row = (SelectableRow) iter.next();
            if (row.isSelected())
                selected.add(row.source());
        }

        return (User[]) selected.toArray(new User[0]);
    }

    public User[] getValues() {
        return values;
    }

}

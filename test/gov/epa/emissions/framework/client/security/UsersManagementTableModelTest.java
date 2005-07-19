package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.client.security.UsersManagementTableModel;
import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

public class UsersManagementTableModelTest extends TestCase {

    private List users;

    private User joe;

    private UsersManagementTableModel model;

    protected void setUp() {
        users = new ArrayList();

        joe = new User();
        joe.setUserName("joe");
        joe.setFullName("Joe Diller");
        joe.setEmailAddr("joe@joe.org");

        users.add(joe);

        model = new UsersManagementTableModel(users);
    }

    public void testShouldReturnColumnsNames() {
        TableModel model = new UsersManagementTableModel(new ArrayList());

        assertEquals(3, model.getColumnCount());

        assertEquals("Username", model.getColumnName(0));
        assertEquals("Name", model.getColumnName(1));
        assertEquals("Email", model.getColumnName(2));
    }

    public void testShouldReturnRowsEqualingNumberOfUsers() {
        List users = new ArrayList();
        users.add(new User());
        users.add(new User());

        TableModel model = new UsersManagementTableModel(users);

        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(joe.getUserName(), model.getValueAt(0, 0));
        assertEquals(joe.getFullName(), model.getValueAt(0, 1));
        assertEquals(joe.getEmailAddr(), model.getValueAt(0, 2));
    }

    public void testShouldBeAbleToUpdateUserOnSettingValuesAtSpecifiedIndexes() {
        model.setValueAt("Joey", 0, 0);//username unchanged
        assertEquals(joe.getUserName(), model.getValueAt(0, 0));

        model.setValueAt("Joe Jumper", 0, 1);
        assertEquals("Joe Jumper", model.getValueAt(0, 1));
        assertEquals("Joe Jumper", joe.getFullName());

        model.setValueAt("joe@jumper.net", 0, 2);
        assertEquals("joe@jumper.net", model.getValueAt(0, 2));
        assertEquals("joe@jumper.net", joe.getEmailAddr());
    }

    public void testShouldReturnStringAsClassForAllColumns() {
        assertEquals(String.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(String.class, model.getColumnClass(2));
    }

    public void testShouldMarkNameColumnAsEditable() {
        assertTrue("Name column should be editable", model.isCellEditable(0, 1));
    }

    public void testShouldMarkEmailColumnAsEditable() {
        assertTrue("Email column should be editable", model
                .isCellEditable(0, 2));
    }

}

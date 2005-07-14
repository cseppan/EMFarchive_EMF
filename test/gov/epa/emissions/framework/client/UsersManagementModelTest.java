package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

public class UsersManagementModelTest extends TestCase {

    private List users;

    private User joe;

    private UsersManagementModel model;

    protected void setUp() {
        users = new ArrayList();

        joe = new User();
        joe.setUserName("joe");
        joe.setFullName("Joe Diller");
        joe.setEmailAddr("joe@joe.org");

        users.add(joe);

        model = new UsersManagementModel(users);
    }

    public void testShouldReturnColumnsNames() {
        TableModel model = new UsersManagementModel(new ArrayList());

        assertEquals(5, model.getColumnCount());

        assertEquals("Select", model.getColumnName(0));
        assertEquals("#", model.getColumnName(1));
        assertEquals("Username", model.getColumnName(2));
        assertEquals("Name", model.getColumnName(3));
        assertEquals("Email", model.getColumnName(4));
    }

    public void testShouldReturnRowsEqualingNumberOfUsers() {
        List users = new ArrayList();
        users.add(new User());
        users.add(new User());

        TableModel model = new UsersManagementModel(users);

        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(Boolean.FALSE, model.getValueAt(0, 0));
        assertEquals("1", model.getValueAt(0, 1));
        assertEquals(joe.getUserName(), model.getValueAt(0, 2));
        assertEquals(joe.getFullName(), model.getValueAt(0, 3));
        assertEquals(joe.getEmailAddr(), model.getValueAt(0, 4));
    }

    public void testShouldBeAbleToUpdateUserOnSettingValuesAtSpecifiedIndexes() {
        model.setValueAt("Joey", 0, 2);//username unchanged
        assertEquals(joe.getUserName(), model.getValueAt(0, 2));

        model.setValueAt("Joe Jumper", 0, 3);
        assertEquals("Joe Jumper", model.getValueAt(0, 3));
        assertEquals("Joe Jumper", joe.getFullName());

        model.setValueAt("joe@jumper.net", 0, 4);
        assertEquals("joe@jumper.net", model.getValueAt(0, 4));
        assertEquals("joe@jumper.net", joe.getEmailAddr());
    }

    public void testShouldReturnBooleanAsClassForSelectColumn() {
        assertEquals(Boolean.class, model.getColumnClass(0));
    }

    public void testShouldMarkSelectColumnAsEditable() {
        assertTrue("Select column should be editable", model.isCellEditable(0,
                0));
    }

    public void testShouldMarkNameColumnAsEditable() {
        assertTrue("Name column should be editable", model.isCellEditable(0, 3));
    }

    public void testShouldMarkEmailColumnAsEditable() {
        assertTrue("Email column should be editable", model
                .isCellEditable(0, 4));
    }

}

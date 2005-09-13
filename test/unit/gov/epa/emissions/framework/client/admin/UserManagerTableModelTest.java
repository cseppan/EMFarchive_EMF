package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UserManagerTableModelTest extends MockObjectTestCase {

    private List users;

    private User joe;

    private UserManagerTableModel model;

    private Mock emfUserAdmin;

    private User admin;

    protected void setUp() throws EmfException {
        users = new ArrayList();

        joe = new User();
        setUserAttributes(joe, "joe", "Joe Diller", "joe@joe.org", false);
        users.add(joe);

        admin = new User();
        setUserAttributes(admin, "admin", "EMF Admin", "admin@emf.org", true);
        users.add(admin);
        
        emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));

        model = new UserManagerTableModel((UserServices) emfUserAdmin.proxy());
    }

    private void setUserAttributes(User joe, String username, String name, String email, boolean isAdmin)
            throws UserException {
        joe.setUserName(username);
        joe.setFullName(name);
        joe.setEmailAddr(email);
        joe.setInAdminGroup(isAdmin);
    }

    public void testShouldReturnFourColumns() {
        assertEquals(4, model.getColumnCount());

        assertEquals("Username", model.getColumnName(0));
        assertEquals("Name", model.getColumnName(1));
        assertEquals("Email", model.getColumnName(2));
        assertEquals("Is Admin ?", model.getColumnName(3));
    }

    public void testShouldReturnRowsEqualingNumberOfUsers() throws EmfException {
        List users = new ArrayList();
        User user1 = new User();
        user1.setUserName("user1");
        users.add(user1);

        User user2 = new User();
        user2.setUserName("user2");
        users.add(user2);

        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));
        model = new UserManagerTableModel((UserServices) emfUserAdmin.proxy());

        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(joe.getUserName(), model.getValueAt(0, 0));
        assertEquals(joe.getFullName(), model.getValueAt(0, 1));
        assertEquals(joe.getEmailAddr(), model.getValueAt(0, 2));
    }

    public void testShouldBeAbleToUpdateUserOnSettingValuesAtSpecifiedIndexes() {
        model.setValueAt("Joey", 0, 0);// username unchanged
        assertEquals(joe.getUserName(), model.getValueAt(0, 0));

        model.setValueAt("Joe Jumper", 0, 1);
        assertEquals("Joe Jumper", model.getValueAt(0, 1));
        assertEquals("Joe Jumper", joe.getFullName());

        model.setValueAt("joe@jumper.net", 0, 2);
        assertEquals("joe@jumper.net", model.getValueAt(0, 2));
        assertEquals("joe@jumper.net", joe.getEmailAddr());
        
        model.setValueAt(Boolean.TRUE, 0, 3);
        assertEquals(Boolean.TRUE, model.getValueAt(0, 3));
        assertEquals(true, joe.isInAdminGroup());
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
        assertTrue("Email column should be editable", model.isCellEditable(0, 2));
    }

    public void testShouldMarkAdminColumnAsEditable() {
        assertTrue("Admin column should be editable", model.isCellEditable(0, 3));
    }

    public void testShouldReturnUserAtSpecifiedIndex() {
        User jill = new User();
        users.add(jill);

        emfUserAdmin = mock(UserServices.class);
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));

        model = new UserManagerTableModel((UserServices) emfUserAdmin.proxy());

        assertEquals(3, model.getRowCount());
        assertSame(joe, model.getUser(0));
        assertSame(jill, model.getUser(2));
    }

    public void testShouldRecreateRowsOnRefresh() {
        assertEquals(2, model.getRowCount());

        // remove user (implicit)
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(new User[0]));

        model.refresh();

        assertEquals(0, model.getRowCount());// model recreates rows on
                                                // updates to EMFUserAdmin
    }
}

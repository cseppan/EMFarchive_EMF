package gov.epa.emissions.framework.client.security;

import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersManagementTableModelTest extends MockObjectTestCase {

    private List users;

    private User joe;

    private UsersManagementTableModel model;

    private Mock emfUserAdmin;

    protected void setUp() {
        users = new ArrayList();

        joe = new User();
        joe.setUserName("joe");
        joe.setFullName("Joe Diller");
        joe.setEmailAddr("joe@joe.org");

        users.add(joe);

        emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));

        model = new UsersManagementTableModel((EMFUserAdmin) emfUserAdmin.proxy());
    }

    public void testShouldReturnColumnsNames() {
        assertEquals(3, model.getColumnCount());

        assertEquals("Username", model.getColumnName(0));
        assertEquals("Name", model.getColumnName(1));
        assertEquals("Email", model.getColumnName(2));
    }

    public void testShouldReturnRowsEqualingNumberOfUsers() {
        List users = new ArrayList();
        User user1 = new User();
        user1.setUserName("user1");
        users.add(user1);        
        
        User user2 = new User();
        user2.setUserName("user2");
        users.add(user2);

        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));
        model = new UsersManagementTableModel((EMFUserAdmin) emfUserAdmin.proxy());

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
        assertTrue("Email column should be editable", model.isCellEditable(0, 2));
    }
    
    public void testShouldReturnUserAtSpecifiedIndex() {
        User jill = new User();
        users.add(jill);

        emfUserAdmin = mock(EMFUserAdmin.class);
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(users.toArray(new User[0])));

        model = new UsersManagementTableModel((EMFUserAdmin) emfUserAdmin.proxy());

        assertEquals(2, model.getRowCount());
        assertSame(joe, model.getUser(0));
        assertSame(jill, model.getUser(1));
    }

    public void testShouldRecreateRowsOnRefresh() {
        assertEquals(1, model.getRowCount());
        
        //remove user (implicit)
        emfUserAdmin.stubs().method("getUsers").withNoArguments().will(returnValue(new User[0]));
        
        model.refresh();
        
        assertEquals(0, model.getRowCount());//model recreates rows on updates to EMFUserAdmin
    }
}

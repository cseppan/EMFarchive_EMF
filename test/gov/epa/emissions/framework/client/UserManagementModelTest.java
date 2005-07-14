package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

public class UserManagementModelTest extends TestCase {

    public void testShouldReturnColumnsNamesOf() {
        TableModel model = new UserManagementModel(null);

        assertEquals(4, model.getColumnCount());

        assertEquals("#", model.getColumnName(0));
        assertEquals("Username", model.getColumnName(1));
        assertEquals("Name", model.getColumnName(2));
        assertEquals("Email", model.getColumnName(3));
    }

    public void testShouldReturnRowsEqualingNumberOfUsers() {
        List users = new ArrayList();
        users.add(new User());
        users.add(new User());

        TableModel model = new UserManagementModel(users);

        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        List users = new ArrayList();

        User joe = new User();
        joe.setUserName("joe");
        joe.setFullName("Joe Diller");
        joe.setEmailAddr("joe@joe.org");
        users.add(joe);

        TableModel model = new UserManagementModel(users);

        assertEquals("1", model.getValueAt(0, 0));
        assertEquals(joe.getUserName(), model.getValueAt(0, 1));
        assertEquals(joe.getFullName(), model.getValueAt(0, 2));
        assertEquals(joe.getEmailAddr(), model.getValueAt(0, 3));

        assertNull("Should not have returned a value for user 100 "
                + "as only 2 users exist in the list", model.getValueAt(100, 1));

        assertNull("Should not have returned a value for column 100 "
                + "as the model has only 5 columns", model.getValueAt(0, 100));    
    }

}

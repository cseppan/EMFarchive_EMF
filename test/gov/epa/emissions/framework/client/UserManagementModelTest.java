package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.commons.User;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import junit.framework.TestCase;

public class UserManagementModelTest extends TestCase {
    
    public void testShouldReturnColumnsNamesOf() {
        TableModel model = new UserManagementModel(null);
        
        assertEquals(5, model.getColumnCount());
        
        assertEquals("#", model.getColumnName(0));
        assertEquals("Username", model.getColumnName(1));
        assertEquals("Name", model.getColumnName(2));
        assertEquals("Email", model.getColumnName(3));
        assertEquals("Group", model.getColumnName(4));
    }
    
    public void testShouldReturnUserAtSpecifiedIndex() {
      List users = new ArrayList();
      
      User joe = new User();
      joe.setUserName("joe");
      users.add(joe);
      
      User mary = new User();
      mary.setUserName("mary");
      users.add(mary);
      
      TableModel model = new UserManagementModel(users);
      
      assertEquals(2, model.getRowCount());
  }
}

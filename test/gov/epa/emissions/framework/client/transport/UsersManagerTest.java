package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UsersManagerTest extends MockObjectTestCase {
    
    public void testShouldDelegateCreateUserToEMFUserAdmin() {
        User user = new User();
        
        Mock userAdmin = mock(EMFUserAdmin.class);
        userAdmin.expects(once()).method("createUser").with(same(user));
        
        UsersManager manager = new UsersManager((EMFUserAdmin)userAdmin.proxy());
        
        manager.createUser(user);
    }
}

package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class CreateUserPresenterTest extends MockObjectTestCase {
    
    public void testShouldCreateUserWithAllFieldsPopulatedOnCreate() throws EmfException {
        User user = new User();
        user.setFullName("Jim Jom");
        user.setAffiliation("Jim Inc.");
        user.setWorkPhone("123");
        user.setEmailAddr("jim@jom.com");
        
        user.setUserName("jimmy");
        user.setPassword("jimjam1234");
        
        Mock userAdmin = mock(EMFUserAdmin.class);
        userAdmin.expects(once()).method("createUser").with(eq(user));
        userAdmin.expects(once()).method("authenticate").with(eq("jimmy"), eq("jimjam1234"), eq(false)).will(returnValue(null));
        
        CreateUserPresenter presenter = new CreateUserPresenter((EMFUserAdmin) userAdmin.proxy());
        
        presenter.create(user.getFullName(),
                         user.getAffiliation(),
                         user.getWorkPhone(), 
                         user.getEmailAddr(), 
                         user.getUserName(),
                         user.getPassword(),
                         user.getPassword());
    }
}

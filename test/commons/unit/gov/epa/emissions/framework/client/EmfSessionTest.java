package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class EmfSessionTest extends MockObjectTestCase {

    public void testGetUser() throws UserException {
        User user = new User();
        user.setUserName("user");
        EmfSession session = new EmfSession(user, null);

        assertEquals(user, session.getUser());
    }

    public void testGetServiceLocator() {
        Mock locator = mock(ServiceLocator.class);
        ServiceLocator locatorProxy = (ServiceLocator) locator.proxy();
        EmfSession session = new EmfSession(null, locatorProxy);

        assertEquals(locatorProxy, session.getServiceLocator());
    }

    public void testGetExImServices() {
        Mock exim = mock(ExImServices.class);

        Mock locator = mock(ServiceLocator.class);
        ExImServices eximProxy = (ExImServices) exim.proxy();
        locator.stubs().method("getExImServices").will(returnValue(eximProxy));

        EmfSession session = new EmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(eximProxy, session.getExImServices());
    }

    public void testGetDataServices() {
        Mock services = mock(DataServices.class);

        Mock locator = mock(ServiceLocator.class);
        DataServices servicesProxy = (DataServices) services.proxy();
        locator.stubs().method("getDataServices").will(returnValue(servicesProxy));

        EmfSession session = new EmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.getDataServices());
    }
}

package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultEmfSessionTest extends MockObjectTestCase {

    private String folder;

    private EmfSession session;

    private Mock locator;

    private Mock eximServices;

    protected void setUp() throws EmfException {
        eximServices = mock(ExImServices.class);
        folder = "folder/blah";
        eximServices.stubs().method("getExportBaseFolder").will(returnValue(folder));

        locator = mock(ServiceLocator.class);
        locator.stubs().method("getExImServices").will(returnValue(eximServices.proxy()));

        session = new DefaultEmfSession(null, (ServiceLocator) locator.proxy());
    }

    public void testGetUser() throws EmfException {
        User user = new User();
        user.setUsername("user");
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator) locator.proxy());

        assertEquals(user, session.getUser());
    }

    public void testGetServiceLocator() throws EmfException {
        ServiceLocator locatorProxy = (ServiceLocator) locator.proxy();
        EmfSession session = new DefaultEmfSession(null, locatorProxy);

        assertEquals(locatorProxy, session.getServiceLocator());
    }

    public void testGetExImServices() throws EmfException {
        ExImServices proxy = (ExImServices) eximServices.proxy();
        locator.stubs().method("getExImServices").will(returnValue(proxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(proxy, session.getExImServices());
    }

    public void testGetDataServices() throws EmfException {
        Mock services = mock(DataServices.class);

        DataServices servicesProxy = (DataServices) services.proxy();
        locator.stubs().method("getDataServices").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.getDataServices());
    }

    public void testGetUserServices() throws EmfException {
        Mock services = mock(UserServices.class);

        UserServices servicesProxy = (UserServices) services.proxy();
        locator.stubs().method("getUserServices").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.getUserServices());
    }

    public void testGetLoggingServices() throws EmfException {
        Mock services = mock(LoggingServices.class);

        LoggingServices servicesProxy = (LoggingServices) services.proxy();
        locator.stubs().method("getLoggingServices").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.getLoggingServices());
    }
    
    public void testCacheMostRecentExportFolder() throws EmfException {
        EmfSession session = new DefaultEmfSession(null, (ServiceLocator) locator.proxy());

        session.setMostRecentExportFolder("folder/blah");
        assertEquals("folder/blah", session.getMostRecentExportFolder());

        session.setMostRecentExportFolder("folder/foo");
        assertEquals("folder/foo", session.getMostRecentExportFolder());
    }

    public void testShouldReturnDefaultExportFolderAsTheMostRecentExportFolderOnFirstInvocationOnly() {
        assertEquals(folder, session.getMostRecentExportFolder());

        session.setMostRecentExportFolder("folder/baz");
        assertEquals("folder/baz", session.getMostRecentExportFolder());
    }
}

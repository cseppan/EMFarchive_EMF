package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultEmfSessionTest extends MockObjectTestCase {

    private String folder;

    private EmfSession session;

    private Mock locator;

    private Mock eximServices;

    protected void setUp() throws Exception {
        eximServices = mock(ExImService.class);
        folder = "folder/blah";
        eximServices.stubs().method("getExportBaseFolder").will(returnValue(folder));

        locator = mock(ServiceLocator.class);
        locator.stubs().method("eximService").will(returnValue(eximServices.proxy()));

        session = new DefaultEmfSession(null, (ServiceLocator) locator.proxy());
    }

    public void testGetUser() throws Exception {
        User user = new User();
        user.setUsername("user");
        EmfSession session = new DefaultEmfSession(user, (ServiceLocator) locator.proxy());

        assertEquals(user, session.user());
    }

    public void testGetServiceLocator() throws Exception {
        ServiceLocator locatorProxy = (ServiceLocator) locator.proxy();
        EmfSession session = new DefaultEmfSession(null, locatorProxy);

        assertEquals(locatorProxy, session.serviceLocator());
    }

    public void testGetExImServices() throws Exception {
        ExImService proxy = (ExImService) eximServices.proxy();
        locator.stubs().method("eximService").will(returnValue(proxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(proxy, session.eximService());
    }

    public void testGetDataServices() throws Exception {
        Mock services = mock(DataService.class);

        DataService servicesProxy = (DataService) services.proxy();
        locator.stubs().method("dataService").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.dataService());
    }

    public void testGetUserServices() throws Exception {
        Mock services = mock(UserService.class);

        UserService servicesProxy = (UserService) services.proxy();
        locator.stubs().method("userService").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.userService());
    }

    public void testGetLoggingServices() throws Exception {
        Mock services = mock(LoggingService.class);

        LoggingService servicesProxy = (LoggingService) services.proxy();
        locator.stubs().method("loggingService").will(returnValue(servicesProxy));

        EmfSession session = new DefaultEmfSession(null, ((ServiceLocator) locator.proxy()));

        assertEquals(servicesProxy, session.loggingService());
    }

    public void testCacheMostRecentExportFolder() throws Exception {
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

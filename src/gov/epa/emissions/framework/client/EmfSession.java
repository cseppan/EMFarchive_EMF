package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.User;

public final class EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    public EmfSession(User user, ServiceLocator locator) {
        serviceLocator = locator;
        this.user = user;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public User getUser() {
        return user;
    }
}

package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public class DefaultEmfSession implements EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    private String mostRecentExportFolder;

    public DefaultEmfSession(User user, ServiceLocator locator) throws EmfException {
        serviceLocator = locator;
        this.user = user;
        mostRecentExportFolder = locator.getExImServices().getExportBaseFolder();
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public User getUser() {
        return user;
    }

    public ExImServices getExImServices() {
        return serviceLocator.getExImServices();
    }

    public DataServices getDataServices() {
        return serviceLocator.getDataServices();
    }

    public String getMostRecentExportFolder() {
        return mostRecentExportFolder;
    }

    public void setMostRecentExportFolder(String mostRecentExportFolder) {
        this.mostRecentExportFolder = mostRecentExportFolder;
    }

    public UserServices getUserServices() {
        return serviceLocator.getUserServices();
    }

    public LoggingServices getLoggingServices() {
        return serviceLocator.getLoggingServices();
    }

    public DatasetTypesServices getDatasetTypesServices() {
        return serviceLocator.getDatasetTypesServices();
    }
}

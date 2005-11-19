package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserService;

public class DefaultEmfSession implements EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    private String mostRecentExportFolder;

    public DefaultEmfSession(User user, ServiceLocator locator) throws EmfException {
        serviceLocator = locator;
        this.user = user;
        mostRecentExportFolder = locator.getExImService().getExportBaseFolder();
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public User getUser() {
        return user;
    }

    public ExImService getExImServices() {
        return serviceLocator.getExImService();
    }

    public DataService getDataServices() {
        return serviceLocator.getDataService();
    }

    public String getMostRecentExportFolder() {
        return mostRecentExportFolder;
    }

    public void setMostRecentExportFolder(String mostRecentExportFolder) {
        this.mostRecentExportFolder = mostRecentExportFolder;
    }

    public UserService getUserServices() {
        return serviceLocator.getUserService();
    }

    public LoggingService getLoggingServices() {
        return serviceLocator.getLoggingService();
    }

    public DatasetTypeService getDatasetTypesServices() {
        return serviceLocator.getDatasetTypesService();
    }
}

package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

public class DefaultEmfSession implements EmfSession {

    private User user;

    private ServiceLocator serviceLocator;

    private String mostRecentExportFolder;

    public DefaultEmfSession(User user, ServiceLocator locator) throws EmfException {
        serviceLocator = locator;
        this.user = user;
        mostRecentExportFolder = locator.eximService().getExportBaseFolder();
    }

    public ServiceLocator serviceLocator() {
        return serviceLocator;
    }

    public User user() {
        return user;
    }

    public ExImService eximService() {
        return serviceLocator.eximService();
    }

    public DataService dataService() {
        return serviceLocator.dataService();
    }

    public String getMostRecentExportFolder() {
        return mostRecentExportFolder;
    }

    public void setMostRecentExportFolder(String mostRecentExportFolder) {
        this.mostRecentExportFolder = mostRecentExportFolder;
    }

    public UserService userService() {
        return serviceLocator.userService();
    }

    public LoggingService loggingService() {
        return serviceLocator.loggingService();
    }

    public DatasetTypeService datasetTypesService() {
        return serviceLocator.datasetTypeService();
    }

    public DataCommonsService dataCommonsService() {
        return serviceLocator.dataCommonsService();
    }
}

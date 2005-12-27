package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.UserService;

public interface EmfSession {

    ServiceLocator serviceLocator();

    User getUser();

    ExImService eximService();

    DataService dataService();

    String getMostRecentExportFolder();

    void setMostRecentExportFolder(String mostRecentExportFolder);

    UserService userService();

    LoggingService loggingService();

    DatasetTypeService datasetTypesService();

    DataCommonsService dataCommonsService();

}
package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserService;

public interface EmfSession {

    ServiceLocator getServiceLocator();

    User getUser();

    ExImService getExImServices();

    DataService getDataServices();

    String getMostRecentExportFolder();

    void setMostRecentExportFolder(String mostRecentExportFolder);

    UserService getUserServices();

    LoggingService getLoggingServices();

    DatasetTypeService getDatasetTypesServices();

}
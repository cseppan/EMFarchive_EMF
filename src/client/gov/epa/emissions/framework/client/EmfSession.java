package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public interface EmfSession {

    ServiceLocator getServiceLocator();

    User getUser();

    ExImServices getExImServices();

    DataServices getDataServices();

    String getMostRecentExportFolder();

    void setMostRecentExportFolder(String mostRecentExportFolder);

    UserServices getUserServices();

    LoggingServices getLoggingServices();

    DatasetTypesServices getDatasetTypesServices();

}
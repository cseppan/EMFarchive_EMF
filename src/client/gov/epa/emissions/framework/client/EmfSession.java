package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public interface EmfSession {

    public ServiceLocator getServiceLocator();

    public User getUser();

    public ExImServices getExImServices();

    public DataServices getDataServices();

    public String getMostRecentExportFolder();

    public void setMostRecentExportFolder(String mostRecentExportFolder);

    public UserServices getUserServices();

    public LoggingServices getLoggingServices();

}
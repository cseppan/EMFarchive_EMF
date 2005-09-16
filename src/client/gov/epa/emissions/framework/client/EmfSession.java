package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;
import gov.epa.emissions.framework.services.UserServices;

public interface EmfSession {

    public abstract ServiceLocator getServiceLocator();

    public abstract User getUser();

    public abstract ExImServices getExImServices();

    public abstract DataServices getDataServices();

    public abstract String getMostRecentExportFolder();

    public abstract void setMostRecentExportFolder(String mostRecentExportFolder);

    public abstract UserServices getUserServices();

}
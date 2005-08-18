package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.UserServices;

public interface ServiceLocator {
    public UserServices getUsersService();

    public StatusServices getStatusService();

    public ExImServices getEximService();

}

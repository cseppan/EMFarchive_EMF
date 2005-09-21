package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.UserServices;

public interface ServiceLocator {
    UserServices getUserServices();

    StatusServices getStatusServices();

    ExImServices getExImServices();

    DataServices getDataServices();
    
    LoggingServices getLoggingServices();

}

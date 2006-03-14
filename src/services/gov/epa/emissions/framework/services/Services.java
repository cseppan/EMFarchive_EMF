package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl;

public class Services {
    private StatusServiceImpl status;

    private DataServiceImpl data;

    private LoggingServiceImpl logging;

    public DataServiceImpl getData() {
        return data;
    }

    public void setDataService(DataServiceImpl data) {
        this.data = data;
    }

    public LoggingServiceImpl getLoggingService() {
        return logging;
    }

    public void setLoggingService(LoggingServiceImpl logging) {
        this.logging = logging;
    }

    public StatusServiceImpl getStatus() {
        return status;
    }

    public void setStatusService(StatusServiceImpl status) {
        this.status = status;
    }

}

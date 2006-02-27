package gov.epa.emissions.framework.services.impl;

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

    public void setLogSvc(LoggingServiceImpl logging) {
        this.logging = logging;
    }

    public StatusServiceImpl getStatus() {
        return status;
    }

    public void setStatusService(StatusServiceImpl status) {
        this.status = status;
    }

}

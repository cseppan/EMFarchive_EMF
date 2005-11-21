/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: ServicesHolder.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.StatusService;

/**
 * This is a utility class to hold a reference to each service needed in the
 * Import or Export task object
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class ServicesHolder {
    private StatusService statusSvc = null;

    private DataServiceImpl dataService = null;

    private LoggingServiceImpl loggingService = null;

    public DataServiceImpl getDataServices() {
        return dataService;
    }

    public void setDataSvc(DataServiceImpl dataService) {
        this.dataService = dataService;
    }

    public LoggingServiceImpl getLoggingService() {
        return loggingService;
    }

    public void setLogSvc(LoggingServiceImpl loggingService) {
        this.loggingService = loggingService;
    }

    public StatusService getStatusSvc() {
        return statusSvc;
    }

    public void setStatusSvc(StatusService statusSvc) {
        this.statusSvc = statusSvc;
    }

}

/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: ServicesHolder.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.LoggingService;
import gov.epa.emissions.framework.services.StatusService;

/**
 * This is a utility class to hold a reference to each service needed in the Import or Export task object
 * 
 * @author Conrad F. D'Cruz
 *
 */
public class ServicesHolder {
    private StatusService statusSvc = null;
    private DataServiceImpl dataSvc = null;
    private LoggingService logSvc = null;
    
	public ServicesHolder() {
		super();
	}

	public DataServiceImpl getDataServices() {
		return dataSvc;
	}

	public void setDataSvc(DataServiceImpl dataSvc) {
		this.dataSvc = dataSvc;
	}

	public LoggingService getLogSvc() {
		return logSvc;
	}

	public void setLogSvc(LoggingService logSvc) {
		this.logSvc = logSvc;
	}

	public StatusService getStatusSvc() {
		return statusSvc;
	}

	public void setStatusSvc(StatusService statusSvc) {
		this.statusSvc = statusSvc;
	}

}

/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: ServicesHolder.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.LoggingServices;
import gov.epa.emissions.framework.services.StatusServices;

/**
 * This is a utility class to hold a reference to each service needed in the Import or Export task object
 * 
 * @author Conrad F. D'Cruz
 *
 */
public class ServicesHolder {
    private StatusServices statusSvc = null;
    private DataServices dataSvc = null;
    private LoggingServices logSvc = null;
    
	public ServicesHolder() {
		super();
	}

	public DataServices getDataSvc() {
		return dataSvc;
	}

	public void setDataSvc(DataServices dataSvc) {
		this.dataSvc = dataSvc;
	}

	public LoggingServices getLogSvc() {
		return logSvc;
	}

	public void setLogSvc(LoggingServices logSvc) {
		this.logSvc = logSvc;
	}

	public StatusServices getStatusSvc() {
		return statusSvc;
	}

	public void setStatusSvc(StatusServices statusSvc) {
		this.statusSvc = statusSvc;
	}

}

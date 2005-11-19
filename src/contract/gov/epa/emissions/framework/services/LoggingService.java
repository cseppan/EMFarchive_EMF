/*
 * Creation on Sep 19, 2005
 * Eclipse Project Name: EMF
 * File Name: LoggingServices.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 *
 */
public interface LoggingService {

	public void setAccessLog(AccessLog accesslog) throws EmfException;
    public AccessLog[] getAccessLogs(long datasetid) throws EmfException;

}

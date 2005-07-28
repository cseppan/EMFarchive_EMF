/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.EMFStatus;
import gov.epa.emissions.framework.commons.Status;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFStatusService implements EMFStatus{

    /**
     * 
     */
    public EMFStatusService() {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#getMessages(java.lang.String)
     */
    public Status[] getMessages(String userName) throws EmfException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#getMessages(java.lang.String, java.lang.String)
     */
    public Status[] getMessages(String userName, String type) throws EmfException {
        // TODO Auto-generated method stub
        return null;
    }

}

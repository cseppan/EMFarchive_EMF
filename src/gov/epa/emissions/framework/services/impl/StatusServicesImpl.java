/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;


import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.StatusDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class StatusServicesImpl implements StatusServices{

	private static Log log = LogFactory.getLog(StatusServicesImpl.class);

    /**
     * 
     */
    public StatusServicesImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#getMessages(java.lang.String)
     */
    public Status[] getMessages(String userName) throws EmfException {
    	log.debug("get all status messages of for user " +userName);
        Session session = HibernateUtils.currentSession();
        List allStats = StatusDAO.getMessages(userName,session);
        log.debug("Total number of messages in the List= " + allStats.size());
    	log.debug("get all status messages of for user " +userName);
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#getMessages(java.lang.String, java.lang.String)
     */
    public Status[] getMessages(String userName, String type) throws EmfException {
    	log.debug("get all status messages of type "+type+" for user " +userName);
        Session session = HibernateUtils.currentSession();
        List allStats = StatusDAO.getMessages(userName,type,session);
        log.debug("Total number of messages in the List= " + allStats.size());
    	log.debug("get all status messages of type "+type+" for user " +userName);
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    public void setStatus(Status status) throws EmfException{
    	log.debug("EMFStatusService: setStatus " + status.getUserName());
        Session session = HibernateUtils.currentSession();
        log.debug("EMFStatusService: Before insertStatusMessage");
        StatusDAO.insertStatusMessage(status,session);
        log.debug("EMFStatusService: After insertStatusMessage");
    	log.debug("EMFStatusService: setStatus " + status.getUserName());
    }
    

}

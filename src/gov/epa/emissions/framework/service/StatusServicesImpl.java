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
import gov.epa.emissions.framework.dao.StatusDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusServices;

import java.util.List;

import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class StatusServicesImpl implements StatusServices{

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
        Session session = HibernateUtils.currentSession();
        List allStats = StatusDAO.getMessages(userName,session);
        System.out.println("Total number of messages in the List= " + allStats.size());
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFData#getMessages(java.lang.String, java.lang.String)
     */
    public Status[] getMessages(String userName, String type) throws EmfException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setStatus(Status status) throws EmfException{
        System.out.println("EMFStatusService: setStatus " + status.getUserName());
        Session session = HibernateUtils.currentSession();
        System.out.println("EMFStatusService: Before insertStatusMessage");
        StatusDAO.insertStatusMessage(status,session);
        System.out.println("EMFStatusService: After insertStatusMessage");
    }
    

}

/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.StatusDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class StatusServiceImpl implements StatusService {

    private static Log LOG = LogFactory.getLog(StatusServiceImpl.class);

    public Status[] getMessages(String userName) {
        LOG.debug("get all status messages of for user " + userName);
        // Session session = HibernateUtils.currentSession();
        Session session = EMFHibernateUtil.getSession();
        List allStats = StatusDAO.getMessages(userName, session);
        session.flush();
        session.close();
        LOG.debug("Total number of messages in the List= " + allStats.size());
        LOG.debug("get all status messages of for user " + userName);
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    public Status[] getMessages(String userName, String type) {
        LOG.debug("get all status messages of type " + type + " for user " + userName);
        // Session session = HibernateUtils.currentSession();
        Session session = EMFHibernateUtil.getSession();
        List allStats = StatusDAO.getMessages(userName, type, session);
        session.flush();
        session.close();
        LOG.debug("Total number of messages in the List= " + allStats.size());
        LOG.debug("get all status messages of type " + type + " for user " + userName);
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    public void setStatus(Status status) {
        LOG.debug("EMFStatusService: setStatus " + status.getUsername());
        // Session session = HibernateUtils.currentSession();
        Session session = EMFHibernateUtil.getSession();
        LOG.debug("EMFStatusService: Before insertStatusMessage");

        // FIXME: replace static w/ instance methods
        StatusDAO.insertStatusMessage(status, session);
        session.flush();
        session.close();
        LOG.debug("EMFStatusService: After insertStatusMessage");
        LOG.debug("EMFStatusService: setStatus " + status.getUsername());
    }

}

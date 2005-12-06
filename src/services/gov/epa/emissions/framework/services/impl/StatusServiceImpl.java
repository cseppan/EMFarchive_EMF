package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.StatusDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import java.util.List;

import org.hibernate.Session;

public class StatusServiceImpl implements StatusService {

    private HibernateSessionFactory sessionFactory;

    public StatusServiceImpl() {
        sessionFactory = HibernateSessionFactory.get();
    }

    public Status[] getAll(String userName) {
        Session session = sessionFactory.getSession();
        List allStats = StatusDAO.getMessages(userName, session);
        session.flush();
        session.close();
        
        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    public void create(Status status) {
        Session session = sessionFactory.getSession();

        // FIXME: replace static w/ instance methods
        StatusDAO.insertStatusMessage(status, session);
        session.flush();
        session.close();
    }

}

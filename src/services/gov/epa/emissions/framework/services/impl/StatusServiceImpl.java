package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.StatusDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import java.util.List;

import org.hibernate.Session;

public class StatusServiceImpl implements StatusService {

    private HibernateSessionFactory sessionFactory;

    private StatusDAO dao;

    public StatusServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public StatusServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new StatusDAO();
    }

    public Status[] getAll(String userName) {
        Session session = sessionFactory.getSession();
        List allStats = dao.all(userName, session);
        session.flush();
        session.close();

        return (Status[]) allStats.toArray(new Status[allStats.size()]);
    }

    public void create(Status status) {
        Session session = sessionFactory.getSession();

        dao.add(status, session);
        session.flush();
        session.close();
    }

}

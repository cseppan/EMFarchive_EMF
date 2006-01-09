package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.DataCommonsDAO;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.StatusService;

import java.util.List;

import org.hibernate.Session;

public class StatusServiceImpl implements StatusService {

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public StatusServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public StatusServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public Status[] getAll(String userName) {
        Session session = sessionFactory.getSession();
        List allStats = dao.allStatus(userName, session);
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

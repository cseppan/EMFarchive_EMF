package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.dao.DataCommonsDAO;
import gov.epa.emissions.framework.services.Status;

import org.hibernate.Session;

public class StatusServiceImpl {

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public StatusServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public StatusServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public void create(Status status) {
        Session session = sessionFactory.getSession();

        dao.add(status, session);
        session.flush();
        session.close();
    }

}

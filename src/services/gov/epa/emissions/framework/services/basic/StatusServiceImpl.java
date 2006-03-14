package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.persistence.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

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

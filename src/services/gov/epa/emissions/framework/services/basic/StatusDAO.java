package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Session;

public class StatusDAO {

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public StatusDAO() {
        this(HibernateSessionFactory.get());
    }

    public StatusDAO(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public void add(Status status) {
        Session session = sessionFactory.getSession();

        dao.add(status, session);
        session.flush();
        session.close();
    }

}

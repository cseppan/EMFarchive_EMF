package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UsersDao {

    public List getAll(Session session) {
        List all = new ArrayList();

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            all = session.createCriteria(User.class).list();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return all;
    }
}

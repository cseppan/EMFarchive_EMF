package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.impl.EmfProperty;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class EmfPropertiesDAO {

    public EmfProperty getProperty(String name, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfProperty.class).add(Restrictions.eq("name", name));
            tx.commit();

            return (EmfProperty) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}

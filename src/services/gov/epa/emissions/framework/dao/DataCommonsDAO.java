package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAO {
    private static Log log = LogFactory.getLog(DataCommonsDAO.class);

    private static final String GET_EMF_KEYWORDS_QUERY = "select kw from Keyword as kw order by keyword";

    private static final String GET_COUNTRY_QUERY = "select country from Country as country order by name";

    private LockingScheme lockingScheme;

    public DataCommonsDAO() {
        lockingScheme = new LockingScheme();
    }

    public List getEmfKeywords(Session session) {
        Transaction tx = null;

        ArrayList allKeywords = null;
        try {
            allKeywords = new ArrayList();

            tx = session.beginTransaction();

            Query query = session.createQuery(GET_EMF_KEYWORDS_QUERY);
            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Keyword kw = (Keyword) iter.next();
                allKeywords.add(kw);
            }

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        return allKeywords;
    }

    public List getCountries(Session session) {
        ArrayList countries = null;

        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            countries = new ArrayList();
            Query query = session.createQuery(GET_COUNTRY_QUERY);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Country cntry = (Country) iter.next();
                countries.add(cntry);
            }

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        return countries;
    }

    public List getSectors(Session session) {
        return session.createCriteria(Sector.class).addOrder(Order.asc("name")).list();
    }

    public List getDatasetTypes(Session session) {
        return session.createCriteria(DatasetType.class).addOrder(Order.asc("name")).list();
    }

    public Sector obtainLockedSector(User user, Sector sector, Session session) {
        return (Sector) lockingScheme.getLocked(user, sector, session, getSectors(session));
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type, Session session) {
        return (DatasetType) lockingScheme.getLocked(user, type, session, getDatasetTypes(session));
    }

    public Sector updateSector(Sector sector, Session session) throws EmfException {
        return (Sector) lockingScheme.update(sector, session, getSectors(session));
    }

    public DatasetType updateDatasetType(DatasetType type, Session session) throws EmfException {
        return (DatasetType) lockingScheme.update(type, session, getDatasetTypes(session));
    }

    public Sector releaseLockedSector(Sector locked, Session session) throws EmfException {
        return (Sector) lockingScheme.releaseLock(locked, session, getSectors(session));
    }

    public DatasetType releaseLockedDatasetType(DatasetType locked, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLock(locked, session, getDatasetTypes(session));
    }

    public List allStatus(String username, Session session) {
        removeReadStatus(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username));
            List all1 = crit.list();
            tx.commit();

            return all1;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(Status status, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(status);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void removeReadStatus(String username, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username)).add(
                    Restrictions.eq("msgRead", Boolean.TRUE));

            List read = crit.list();
            for (Iterator iter = read.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
                session.delete(element);
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
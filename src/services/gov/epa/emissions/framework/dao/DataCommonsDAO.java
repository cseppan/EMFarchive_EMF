package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DataCommonsDAO {
    private static Log log = LogFactory.getLog(DataCommonsDAO.class);

    private static final String GET_EMF_KEYWORDS_QUERY = "select kw from Keyword as kw order by keyword";

    private static final String GET_COUNTRY_QUERY = "select country from Country as country order by name";

    private LockingScheme lockingScheme;

    public DataCommonsDAO() {
        lockingScheme = new LockingScheme();
    }

    public List getEmfKeywords(Session session) {
        log.debug("In get emf keywords");
        Transaction tx = null;

        ArrayList allKeywords = null;
        try {
            allKeywords = new ArrayList();

            tx = session.beginTransaction();

            log.debug("The query: " + GET_EMF_KEYWORDS_QUERY);
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
        log.debug("after call to allkeywords: size of list= " + allKeywords.size());
        return allKeywords;
    }

    public List getCountries(Session session) {
        log.debug("In get all Countries with valid session?: " + (session == null));
        ArrayList countries = null;

        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            countries = new ArrayList();
            log.debug("The query: " + GET_COUNTRY_QUERY);
            Query query = session.createQuery(GET_COUNTRY_QUERY);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Country cntry = (Country) iter.next();
                countries.add(cntry);
            }

            tx.commit();
            log.info("Total number of countries retrieved= " + countries.size());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        log.debug("End getSectors");

        return countries;
    }

    public void updateCountry(Country country, Session session) {
        log.debug("updating country: " + country.getId());
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.update(country);
            tx.commit();
            log.debug("updating country: " + country.getId());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public void insertCountry(Country country, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(country);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public List getSectors(Session session) {
        return session.createQuery("SELECT sector FROM Sector as sector ORDER BY name").list();
    }

    public List getDatasetTypes(Session session) {
        return session.createQuery("SELECT dataset_type FROM DatasetType as dataset_type ORDER BY name").list();
    }

    public Sector getSectorLock(User user, Sector sector, Session session) {
        return (Sector) lockingScheme.getLock(user, sector, session, getSectors(session));
    }

    public DatasetType getDatasetTypeLock(User user, DatasetType type, Session session) {
        return (DatasetType) lockingScheme.getLock(user, type, session, getDatasetTypes(session));
    }

    public Sector updateSector(User user, Sector sector, Session session) throws EmfException {
        return (Sector) lockingScheme.update(user, sector, session, getSectors(session));
    }

    public DatasetType updateDatasetType(User user, DatasetType type, Session session) throws EmfException {
        return (DatasetType) lockingScheme.update(user, type, session, getDatasetTypes(session));
    }

    public Sector releaseSectorLock(Sector locked, Session session) throws EmfException {
        return (Sector) lockingScheme.releaseLock(locked, session, getSectors(session));
    }

    public DatasetType releaseDatasetTypeLock(DatasetType locked, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLock(locked, session, getDatasetTypes(session));
    }

}
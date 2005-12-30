package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.EMFConstants;

import java.util.ArrayList;
import java.util.Date;
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

    public List getSectors(Session session) {
        return session.createQuery("SELECT sector FROM Sector as sector ORDER BY name").list();
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

    /**
     * This method will check if the current sector record has a lock. If it does it will return the sector object with
     * the lock parameters to the current user indicating who is using this object. If the lock is older than 12 hours
     * then the current user will be given the lock.
     * 
     * If there is no lock, this user will grab the lock and a modified record indicating the ownership of the lock is
     * set back to the GUI.
     * 
     * The client will cross check those paramters in the returned sector object against the current user in the GUI. If
     * the user is the same the GUI will allow the user to edit. If not the GUI will switch to view mode and a dialog
     * will display the Full Name of the user who has the lock and the date the lock was acquired.
     * 
     */
    public Sector getSectorLock(User user, Sector sector, Session session) {
        Sector current = sector(sector.getId(), session);
        if (!current.isLocked()) {
            grabLock(user, current, session);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getFullName().equals(current.getUsername())) || (elapsed > EMFConstants.EMF_LOCK_TIMEOUT_VALUE)) {
            grabLock(user, current, session);
        }

        return current;
    }

    private void grabLock(User user, Sector sector, Session session) {
        sector.setUsername(user.getFullName());
        sector.setLockDate(new Date());

        Transaction tx = session.beginTransaction();
        try {
            session.update(sector);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    private void grabLock(User user, DatasetType type, Session session) {
        type.setUsername(user.getFullName());
        type.setLockDate(new Date());

        Transaction tx = session.beginTransaction();
        try {
            session.update(type);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public Sector releaseSectorLock(Sector sector, Session session) throws EmfException {
        Sector current = sector(sector.getId(), session);

        if (!current.isLocked())
            throw new EmfException("Cannot update without owning lock");

        Transaction tx = session.beginTransaction();
        try {
            current.setUsername(null);
            current.setLockDate(null);
            session.update(current);

            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        return current;
    }

    public Sector updateSector(User user, Sector sector, Session session) throws EmfException {
        Sector current = sector(sector.getId(), session);
        if (!current.isLocked(user))
            throw new EmfException("Cannot update without owning lock");

        Transaction tx = session.beginTransaction();
        try {
            current.setLockDate(new Date());
            session.update(current);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        return releaseSectorLock(current, session);
    }

    private Sector sector(long id, Session session) {
        List sectors = getSectors(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            if (sector.getId() == id)
                return sector;
        }

        return null;
    }

    public List getDatasetTypes(Session session) {
        return session.createQuery("SELECT dataset_type FROM DatasetType as dataset_type ORDER BY name").list();
    }

    public DatasetType getDatasetTypeLock(User user, DatasetType type, Session session) {
        DatasetType current = type(type, session);
        if (!current.isLocked()) {
            grabLock(user, current, session);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getFullName().equals(current.getUsername())) || (elapsed > EMFConstants.EMF_LOCK_TIMEOUT_VALUE)) {
            grabLock(user, current, session);
        }

        return current;
    }

    private DatasetType type(DatasetType target, Session session) {
        List sectors = getDatasetTypes(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            DatasetType type = (DatasetType) iter.next();
            if (type.getDatasettypeid() == target.getDatasettypeid())
                return type;
        }

        return null;
    }

}
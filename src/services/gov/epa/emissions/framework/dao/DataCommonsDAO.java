package gov.epa.emissions.framework.dao;

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

    private static final String GET_SECTOR_QUERY = "select sector from Sector as sector order by name";

    public static List getEmfKeywords(Session session) {
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

    public static List getCountries(Session session) {
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

    public static List getSectors(Session session) {
        log.debug("In get all Sectors with valid session?: " + (session == null));
        ArrayList sectors = new ArrayList();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            sectors = new ArrayList();

            log.debug("The query: " + GET_SECTOR_QUERY);
            Query query = session.createQuery(GET_SECTOR_QUERY);

            Iterator iter = query.iterate();
            while (iter.hasNext()) {
                Sector sector = (Sector) iter.next();
                sectors.add(sector);
            }

            tx.commit();
            log.info("Total number of sectors retrieved= " + sectors.size());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

        log.debug("End getSectors");
        return sectors;
    }

    public static void updateSector(Sector sector, Session session) {
        log.debug("updating sector: " + sector.getId());
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.update(sector);
            tx.commit();
            log.debug("updating sector: " + sector.getId());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
    }

    public static void updateCountry(Country country, Session session) {
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

    public static void insertCountry(Country country, Session session) {
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

    public static void insertSector(Sector sector, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(sector);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }

    }

    /**
     * This method will check if the current sector record has a lock.  If it does it will
     * return the sector object with the lock parameters to the current user indicating who is using this object.
     * If the lock is older than 12 hours then the current user will be given the lock.
     * 
     * If there is no lock, this user will grab the lock and a modified record indicating the ownership of the lock is
     * set back to the GUI.
     * 
     * The client will cross check those paramters in the returned sector object against the current user in the GUI.  If the user
     * is the same the GUI will allow the user to edit.  If not the GUI will switch to view mode and a dialog will display the Full Name
     * of the user who has the lock and the date the lock was acquired.
     * 
     */
    public static Sector getSectorLock(User user, Sector sector, Session session) {
        log.debug("getting sector lock: " + sector.getName() + " for user: " + user.getFullName());
        Sector modifiedSector;

        // get the record for this object from the database and check if there is a lock in place
        // if there is a lock then return the sector object with the lock parameters to the client
        // if there is no lock then grab the lock for this user.
        
        modifiedSector = DataCommonsDAO.sectors(sector.getId(),session);
        
        if (modifiedSector.getUsername()!= null){
            // get the time difference between now and when the lock was acquired by the other user
           long timeDifference = new Date().getTime() - modifiedSector.getLockDate().getTime();
           if ((modifiedSector.getUsername().equals(user.getFullName())) || (timeDifference>EMFConstants.EMF_LOCK_TIMEOUT_VALUE)){
               modifiedSector = grabLock(user,modifiedSector,session);
           }
        }else{
            modifiedSector = grabLock(user,modifiedSector,session);        
        }
        return modifiedSector;
    }

    private static Sector grabLock(User user, Sector sector, Session session) {
        Transaction tx = null;
        Sector modifiedSector;
        sector.setUsername(user.getFullName());
        sector.setLockDate(new Date());
        
        log.debug("Sector params: " + sector.getUsername() + " :lock date: " + sector.getLockDate());
        
        try {
            tx = session.beginTransaction();
            session.update(sector);
            tx.commit();
            modifiedSector = sectors(sector.getId(),session);
            log.debug("getting sector lock: " + sector.getId());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }            
        return modifiedSector;
    }

    public static Sector releaseSectorLock(User user, Sector sector, Session session) throws EmfException {
        log.debug("releasing sector lock: " + sector.getId());
        Sector modifiedSector;
        Transaction tx = null;

        modifiedSector = DataCommonsDAO.sectors(sector.getId(),session);
        
        if (modifiedSector.getUsername().equals(user.getFullName())){
            modifiedSector.setUsername(null);
            modifiedSector.setLockDate(null);
            
            try {
                tx = session.beginTransaction();
                session.update(modifiedSector);
                tx.commit();
                modifiedSector = sectors(modifiedSector.getId(),session);
                log.debug("releasing sector lock: " + modifiedSector.getId());
            } catch (HibernateException e) {
                log.error(e);
                tx.rollback();
                throw e;
            }
            return modifiedSector;        

        }
            throw new EmfException("Failed operation: Cannot update without owning lock");
    }

    public static Sector updateSector(User user, Sector sector, Session session) throws EmfException {
        log.debug("updating sector with lock: " + sector.getId());
        Sector modifiedSector;
        Transaction tx = null;

        modifiedSector = DataCommonsDAO.sectors(sector.getId(),session);

        if (modifiedSector.getUsername().equals(user.getFullName())){
            modifiedSector.setUsername(user.getFullName());
            modifiedSector.setLockDate(new Date());
            
            try {
                tx = session.beginTransaction();
                session.update(modifiedSector);
                modifiedSector.setUsername(null);            
                modifiedSector.setLockDate(null);
                session.update(modifiedSector);
                tx.commit();
                modifiedSector = sectors(sector.getId(),session);
                log.debug("updating sector with lock: " + sector.getId());
            } catch (HibernateException e) {
                log.error(e);
                tx.rollback();
                throw e;
            }
            return modifiedSector;        
            
        }
        throw new EmfException("Failed operation: Cannot update without owning lock");

    }

    private static Sector sectors(long id,Session session) {
        List sectors = DataCommonsDAO.getSectors(session);
        Iterator iter = sectors.iterator();
        while (iter.hasNext()){
            Sector sector = (Sector)iter.next();
            if (sector.getId()==id){
                return sector;
            }
        }
        return null;
    }


}
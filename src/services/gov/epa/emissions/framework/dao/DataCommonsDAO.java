package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Country;

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

    public static Sector getSectorLock(User user, Sector sector, Session session) {
        log.debug("getting sector lock: " + sector.getName() + " for user: " + user.getFullName());
        Sector modifiedSector;
        Transaction tx = null;

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

    public static Sector releaseSectorLock(User user, Sector sector, Session session) {
        log.debug("releasing sector lock: " + sector.getId());
        Sector modifiedSector;
        Transaction tx = null;

        sector.setUsername(null);
        sector.setLockDate(null);
        
        try {
            tx = session.beginTransaction();
            session.update(sector);
            tx.commit();
            modifiedSector = sectors(sector.getId(),session);
            log.debug("releasing sector lock: " + sector.getId());
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        return modifiedSector;        
    }

    public static Sector updateSector(User user, Sector sector, Session session) {
        log.debug("updating sector with lock: " + sector.getId());
        Sector modifiedSector;
        Transaction tx = null;

        sector.setUsername(user.getFullName());
        sector.setLockDate(new Date());
        
        try {
            tx = session.beginTransaction();
            session.update(sector);
            sector.setUsername(null);            
            sector.setLockDate(null);
            session.update(sector);
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
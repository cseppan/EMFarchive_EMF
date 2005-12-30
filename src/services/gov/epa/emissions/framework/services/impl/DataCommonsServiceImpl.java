package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DataCommonsDAO;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataCommonsServiceImpl implements DataCommonsService {
    private static Log LOG = LogFactory.getLog(DataCommonsServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public DataCommonsServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public DataCommonsServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public Keyword[] getKeywords() throws EmfException {
        List keywords = null;
        try {
            Session session = sessionFactory.getSession();
            keywords = dao.getEmfKeywords(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Keyword[]) keywords.toArray(new Keyword[keywords.size()]);
    }

    public void addCountry(Country country) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.insertCountry(country, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.updateCountry(country, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    public Country[] getCountries() throws EmfException {
        List countries = null;
        try {
            Session session = sessionFactory.getSession();
            countries = dao.getCountries(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Country[]) countries.toArray(new Country[countries.size()]);
    }

    public Sector[] getSectors() throws EmfException {
        List sectors;
        try {
            Session session = sessionFactory.getSession();
            sectors = dao.getSectors(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
    }

    public Sector getSectorLock(User user, Sector sector) throws EmfException {
        Sector lockedSector;
        try {
            Session session = sessionFactory.getSession();
            lockedSector = dao.getSectorLock(user, sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Database error: " + e);
        }
        return lockedSector;
    }

    public Sector updateSector(User user, Sector sector) throws EmfException {
        Sector lockedSector;
        try {
            Session session = sessionFactory.getSession();
            lockedSector = dao.updateSector(user, sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Database error: " + e);
        }
        return lockedSector;
    }

    public Sector releaseSectorLock(User user, Sector sector) throws EmfException {
        Sector lockedSector;
        try {
            Session session = sessionFactory.getSession();
            lockedSector = dao.releaseSectorLock(sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Database error: " + e);
        }
        return lockedSector;
    }

    public DatasetType[] getDatasetTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    public DatasetType getDatasetTypeLock(User user, DatasetType type) {
        // TODO Auto-generated method stub
        return null;
    }

    public DatasetType updateDatasetType(User user, DatasetType type) {
        // TODO Auto-generated method stub
        return null;
    }

    public DatasetType releaseDatasetTypeLock(User user, DatasetType type) {
        // TODO Auto-generated method stub
        return null;
    }

}

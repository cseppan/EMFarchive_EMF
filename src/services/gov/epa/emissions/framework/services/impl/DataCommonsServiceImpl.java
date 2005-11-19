/*
 * Creation on Oct 30, 2005
 * Eclipse Project Name: EMF
 * File Name: KeywordServicesImpl.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.InterDataDAO;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataCommonsServiceImpl implements DataCommonsService {
    private static Log LOG = LogFactory.getLog(DataCommonsServiceImpl.class);

    public Keyword[] getKeywords() throws EmfException {
        List keywords = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            keywords = InterDataDAO.getEmfKeywords(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Keyword[]) keywords.toArray(new Keyword[keywords.size()]);
    }

    public void deleteKeyword(Keyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.deleteEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void insertKeyword(Keyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.insertEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateKeyword(Keyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.updateEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void addCountry(Country country) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.insertCountry(country, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.updateCountry(country, session);
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
            Session session = EMFHibernateUtil.getSession();
            countries = InterDataDAO.getCountries(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Country[]) countries.toArray(new Country[countries.size()]);
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.insertSector(sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateSector(Sector sector) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            InterDataDAO.updateSector(sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    public Sector[] getSectors() throws EmfException {
        List sectors;
        try {
            Session session = EMFHibernateUtil.getSession();
            sectors = InterDataDAO.getSectors(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            LOG.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDAO;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.User;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DataServicesImpl implements DataServices {
    private static Log log = LogFactory.getLog(DataServicesImpl.class);

    public EmfDataset[] getDatasets() throws EmfException {
        List datasets = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            datasets = DatasetDAO.getDatasets(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
    }

    public EmfDataset[] getDatasets(User user) throws EmfException {
        List datasets = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            datasets = DatasetDAO.getDatasets(user, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
    }

    public void insertDataset(EmfDataset aDataset) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.insertDataset(aDataset, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateDataset(EmfDataset aDset) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.updateDataset(aDset, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void addCountry(Country country) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.insertCountry(country, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateCountry(Country country) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.updateCountry(country, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    public Country[] getCountries() throws EmfException {

        List countries = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            countries = DatasetDAO.getCountries(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Country[]) countries.toArray(new Country[countries.size()]);
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.insertSector(sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateSector(Sector sector) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            DatasetDAO.updateSector(sector, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }

    }

    public Sector[] getSectors() throws EmfException {
        List sectors;
        try {
            Session session = EMFHibernateUtil.getSession();
            sectors = DatasetDAO.getSectors(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
    }

}

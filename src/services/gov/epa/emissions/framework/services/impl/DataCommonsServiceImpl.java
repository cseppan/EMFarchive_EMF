package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DataCommonsDAO;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.Status;

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
        try {
            Session session = sessionFactory.getSession();
            List keywords = dao.getEmfKeywords(session);
            session.close();

            return (Keyword[]) keywords.toArray(new Keyword[keywords.size()]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Keywords. Reason: " + e);
            throw new EmfException("Could not get all Keywords");
        }
    }

    public Country[] getCountries() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List countries = dao.getCountries(session);
            session.close();

            return (Country[]) countries.toArray(new Country[countries.size()]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Countries. Reason: " + e);
            throw new EmfException("Could not get all Countries");
        }
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List sectors = dao.getSectors(session);
            session.close();

            return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Sectors. Reason: " + e);
            throw new EmfException("Could not get all Sectors");
        }
    }

    public Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Sector lockedSector = dao.obtainLockedSector(owner, sector, session);
            session.close();

            return lockedSector;
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock for sector: " + sector.getName() + " by owner: " + owner.getUsername()
                    + ".Reason: " + e);
            throw new EmfException("Could not obtain lock for sector: " + sector.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public Sector updateSector(Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Sector released = dao.updateSector(sector, session);
            session.close();

            return released;
        } catch (HibernateException e) {
            LOG.error("Could not update sector: " + sector.getName() + ".Reason: " + e);
            throw new EmfException("Could not update sector: " + sector.getName());
        }
    }

    public Sector releaseLockedSector(Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Sector released = dao.releaseLockedSector(sector, session);
            session.close();

            return released;
        } catch (HibernateException e) {
            LOG.error("Could not release lock for sector: " + sector.getName() + " by owner: " + sector.getLockOwner()
                    + ".Reason: " + e);
            throw new EmfException("Could not release lock for sector: " + sector.getName() + " by owner: "
                    + sector.getLockOwner());
        }
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List list = dao.getDatasetTypes(session);
            session.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (HibernateException e) {
            LOG.error("Could not get all DatasetTypes. Reason: " + e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType locked = dao.obtainLockedDatasetType(user, type, session);
            session.close();

            return locked;
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock for DatasetType: " + type.getName() + ".Reason: " + e);
            throw new EmfException("Could not obtain lock for DatasetType: " + type.getName());
        }
    }

    public DatasetType updateDatasetType(DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType locked = dao.updateDatasetType(type, session);
            session.close();

            return locked;
        } catch (HibernateException e) {
            LOG.error("Could not update DatasetType: " + type.getName() + ".Reason: " + e);
            throw new EmfException("Could not update DatasetType: " + type.getName());
        }
    }

    public DatasetType releaseLockedDatasetType(User user, DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType locked = dao.releaseLockedDatasetType(type, session);
            session.close();

            return locked;
        } catch (HibernateException e) {
            LOG.error("Could not release lock on DatasetType: " + type.getName() + ".Reason: " + e);
            throw new EmfException("Could not release lock on DatasetType: " + type.getName());
        }
    }

    public Status[] getStatuses(String username) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List statuses = dao.getStatuses(username, session);
            session.close();

            return (Status[]) statuses.toArray(new Status[statuses.size()]);
        } catch (HibernateException e) {
            LOG.error("Could not get all Status messages. Reason: " + e);
            throw new EmfException("Could not get all Status messages");
        }
    }

}

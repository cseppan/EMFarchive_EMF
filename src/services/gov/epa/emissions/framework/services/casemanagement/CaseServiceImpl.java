package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CaseServiceImpl implements CaseService {
    private static Log LOG = LogFactory.getLog(CaseServiceImpl.class);

    private CaseDAO dao;

    private HibernateSessionFactory sessionFactory;

    public CaseServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public CaseServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new CaseDAO();
    }

    public Case[] getCases() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List cases = dao.getCases(session);
            session.close();

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        }
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List abbreviations = dao.getAbbreviations(session);
            session.close();

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        }
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List airQualityModels = dao.getAirQualityModels(session);
            session.close();

            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        }
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getCaseCategories(session);
            session.close();

            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        }
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getEmissionsYears(session);
            session.close();

            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        }
    }

    public Grid[] getGrids() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getGrids(session);
            session.close();

            return (Grid[]) results.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        }
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getMeteorlogicalYears(session);
            session.close();

            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Meteorlogical Years", e);
            throw new EmfException("Could not get all Meteorlogical Years");
        }
    }

    public Speciation[] getSpeciations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getSpeciations(session);
            session.close();

            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        }
    }

    public void addCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        }
    }

    public void removeCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not remove Case: " + element, e);
            throw new EmfException("Could not remove Case: " + element);
        }
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case locked = dao.obtainLocked(owner, element, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        }
    }

    public Case releaseLocked(Case locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Case: " + locked + " by owner: " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock for Case: " + locked + " by owner: " + locked.getLockOwner());
        }
    }

    public Case updateCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Case released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Case: " + element, e);
            throw new EmfException("Could not update Case: " + element);
        }
    }

}

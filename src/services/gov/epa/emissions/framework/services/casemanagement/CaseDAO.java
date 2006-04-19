package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    private LockingScheme lockingScheme;

    public CaseDAO() {
        hibernateFacade = new HibernateFacade();
        lockingScheme = new LockingScheme();
    }

    public void add(Abbreviation object, Session session) {
        addObject(object, session);
    }

    public void add(AirQualityModel object, Session session) {
        addObject(object, session);
    }

    public void add(CaseCategory object, Session session) {
        addObject(object, session);
    }

    public void add(EmissionsYear object, Session session) {
        addObject(object, session);
    }

    public void add(Grid object, Session session) {
        addObject(object, session);
    }

    public void add(MeteorlogicalYear object, Session session) {
        addObject(object, session);
    }

    public void add(Speciation object, Session session) {
        addObject(object, session);
    }

    public void add(Case object, Session session) {
        addObject(object, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List getAbbreviations(Session session) {
        return session.createCriteria(Abbreviation.class).addOrder(Order.asc("name")).list();
    }

    public List getAirQualityModels(Session session) {
        return session.createCriteria(AirQualityModel.class).addOrder(Order.asc("name")).list();
    }

    public List getCaseCategories(Session session) {
        return session.createCriteria(CaseCategory.class).addOrder(Order.asc("name")).list();
    }

    public List getEmissionsYears(Session session) {
        return session.createCriteria(EmissionsYear.class).addOrder(Order.asc("name")).list();
    }

    public List getGrids(Session session) {
        return session.createCriteria(Grid.class).addOrder(Order.asc("name")).list();
    }

    public List getMeteorlogicalYears(Session session) {
        return session.createCriteria(MeteorlogicalYear.class).addOrder(Order.asc("name")).list();
    }

    public List getSpeciations(Session session) {
        return session.createCriteria(Speciation.class).addOrder(Order.asc("name")).list();
    }

    public List getCases(Session session) {
        return session.createCriteria(Case.class).addOrder(Order.asc("name")).list();
    }

    public void remove(Case element, Session session) {
        hibernateFacade.remove(element, session);
    }

    public Case obtainLocked(User owner, Case element, Session session) {
        return (Case) lockingScheme.getLocked(owner, element, session, getCases(session));
    }

    public Case releaseLocked(Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(locked, session, getCases(session));
    }

    public Case update(Case locked, Session session) throws EmfException {
        return (Case) lockingScheme.releaseLockOnUpdate(locked, session, getCases(session));
    }
}
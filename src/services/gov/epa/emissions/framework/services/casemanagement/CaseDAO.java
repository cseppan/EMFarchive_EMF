package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    public CaseDAO() {
        hibernateFacade = new HibernateFacade();
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

}
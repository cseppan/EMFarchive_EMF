package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class CaseCommonsDAO {

    private HibernateFacade hibernateFacade;

    public CaseCommonsDAO() {
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

    public List getAbbreviations(Session session) {
        return session.createCriteria(Abbreviation.class).addOrder(Order.asc("name")).list();
    }

    public List getAirQualityModels(Session session) {
        return session.createCriteria(AirQualityModel.class).addOrder(Order.asc("name")).list();
    }

    public List getCaseCategories(Session session) {
        return session.createCriteria(CaseCategory.class).addOrder(Order.asc("name")).list();
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

}
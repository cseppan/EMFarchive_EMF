package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class Pollutants {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List pollutantList;

    public Pollutants(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        pollutantList = pollutants(sessionFactory);
    }

    private List pollutants(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            return facade.getAll(Pollutant.class, Order.asc("name"), session);
        } finally {
            session.close();
        }
    }

    public Pollutant getPollutant(String name) throws ImporterException {
        Pollutant pollutant = new Pollutant(name);
        int index = pollutantList.indexOf(pollutant);
        if (index != -1) {
            return (Pollutant) pollutantList.get(index);
        }
        return saveAndLoad(pollutant);
    }

    private Pollutant saveAndLoad(Pollutant pollutant) throws ImporterException {
        try {
            save(pollutant);
            return load(pollutant.getName());
        } catch (RuntimeException e) {
            throw new ImporterException("Could not add a pollutant - " + pollutant.getName());
        }
    }

    private void save(Pollutant pollutant) {
        Session session = sessionFactory.getSession();
        try {
            facade.add(pollutant, session);
        } finally {
            session.close();
        }
    }

    private Pollutant load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (Pollutant) facade.load(Pollutant.class, Restrictions.eq("name", name), session);
        } finally {
            session.close();
        }
    }

}

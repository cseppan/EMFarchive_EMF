/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: DataServicesImpl.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDAO;
import gov.epa.emissions.framework.services.Country;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Sector;
import gov.epa.emissions.framework.services.User;

import java.util.List;

import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class DataServicesImpl implements DataServices {

    public DataServicesImpl() {
        super();
    }

    public EmfDataset[] getDatasets() {
        Session session = EMFHibernateUtil.getSession();
        List datasets = DatasetDAO.getDatasets(session);
        session.flush();
        session.close();
        return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
    }

    public EmfDataset[] getDatasets(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    public void insertDataset(EmfDataset aDataset) {
        Session session = EMFHibernateUtil.getSession();
        DatasetDAO.insertDataset(aDataset, session);
        session.flush();
        session.close();
    }

	public void updateDataset(EmfDataset aDset) {
        Session session = EMFHibernateUtil.getSession();
        DatasetDAO.updateDataset(aDset, session);
        session.flush();
        session.close();
	}

	public Country[] getCountries() {

        Session session = EMFHibernateUtil.getSession();
        List countries = DatasetDAO.getCountries(session);
        session.flush();
        session.close();
        return (Country[]) countries.toArray(new Country[countries.size()]);
	}

	public Sector[] getSectors() {
        Session session = EMFHibernateUtil.getSession();
        List sectors = DatasetDAO.getSectors(session);
        session.flush();
        session.close();
        return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
	}

	public void addCountry(String country) throws EmfException {
		//FIXME: REMOVE DUMMY LINES BELOW
		if (false) throw new EmfException("");
		
	}

	public void addSector(String sector) throws EmfException {
		//FIXME: REMOVE DUMMY LINES BELOW
		if (false) throw new EmfException("");
		
	}



}

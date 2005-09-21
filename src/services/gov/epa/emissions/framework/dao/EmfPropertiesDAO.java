/*
 * Created on Jul 29, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: StatusDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.services.EmfProperty;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class EmfPropertiesDAO {
    private static Log log = LogFactory.getLog(EmfPropertiesDAO.class);

    private static final String GET_EMF_PROPERTY_QUERY = "select prop from EmfProperty as prop where prop.propertyname=:propertyname";

    // FIXME: Verify if exception needs to be thrown/caught here
    public static String getEmfPropertyValue(String propertyname, Session session) {
        log.debug("In get property for " + propertyname);
        String propertyvalue = null;
        
        Transaction tx = session.beginTransaction();

        Query query = session.createQuery(GET_EMF_PROPERTY_QUERY);
        query.setParameter("propertyname", propertyname, Hibernate.STRING);

        Iterator iter = query.iterate();
        while (iter.hasNext()) {
        	EmfProperty emfProp = (EmfProperty) iter.next();
        	propertyvalue = emfProp.getPropertyvalue();
        }

        tx.commit();
        log.debug("End get property value for " + propertyname + " = " + propertyvalue);
        return propertyvalue;
    }

}

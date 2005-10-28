/*
 * Created on Jul 29, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: StatusDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class EmfKeywordsDAO {
    private static Log log = LogFactory.getLog(EmfKeywordsDAO.class);

    private static final String GET_EMF_PROPERTY_QUERY = "from EmfKeywords as kw";

    public static List getEmfKeywords(Session session){
        log.debug("In get emf keywords for datasetid= ");
        Transaction tx=null;
        
        ArrayList allKeywords = null;
        try {
            allKeywords = new ArrayList();

            tx = session.beginTransaction();

            allKeywords = (ArrayList)session.createQuery(GET_EMF_PROPERTY_QUERY);
            tx.commit();
        } catch (HibernateException e) {
            log.error(e);
            tx.rollback();
            throw e;
        }
        log.debug("after call to allkeywords: size of list= " + allKeywords.size());        
        return allKeywords;
    }

}

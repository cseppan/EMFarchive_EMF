/*
 * Creation on Oct 30, 2005
 * Eclipse Project Name: EMF
 * File Name: KeywordServicesImpl.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.EmfKeywordsDAO;
import gov.epa.emissions.framework.services.EmfKeyword;
import gov.epa.emissions.framework.services.KeywordServices;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class KeywordServicesImpl implements KeywordServices {
    private static Log log = LogFactory.getLog(KeywordServicesImpl.class);

    public KeywordServicesImpl() {
        super();
    }

    public EmfKeyword[] getEmfKeywords() throws EmfException {
        List keywords = null;
        try {
            Session session = EMFHibernateUtil.getSession();
            keywords = EmfKeywordsDAO.getEmfKeywords(session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
        return (EmfKeyword[]) keywords.toArray(new EmfKeyword[keywords.size()]);
    }

    public void deleteEmfKeyword(EmfKeyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            EmfKeywordsDAO.deleteEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void insertEmfKeyword(EmfKeyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            EmfKeywordsDAO.insertEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

    public void updateEmfKeyword(EmfKeyword keyword) throws EmfException {
        try {
            Session session = EMFHibernateUtil.getSession();
            EmfKeywordsDAO.updateEmfKeyword(keyword, session);
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Database error: " + e);
            throw new EmfException("Error communicating with the server");
        }
    }

}

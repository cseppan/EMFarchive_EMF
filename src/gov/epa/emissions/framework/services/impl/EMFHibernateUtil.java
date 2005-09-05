/*
 * Creation on Sep 2, 2005
 * Eclipse Project Name: EMF
 * File Name: EMFHibernateUtil.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class EMFHibernateUtil {

    private static Log log = LogFactory.getLog(EMFHibernateUtil.class);
    private static SessionFactory sessionFactory = null;
   
    static {
        try {
            Configuration configure = new Configuration().configure("hibernate.cfg.xml");
            sessionFactory = configure.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static final ThreadLocal sessionThread = new ThreadLocal();

    public static Session getSession() throws HibernateException {
        log.debug("get session");    	
        Session session = null;
        session = sessionFactory.openSession();
        log.debug("get session: " + (session==null));
        return session;
    }

    public static void closeSession(Session s) throws HibernateException {
        log.debug("closing session");
        if (s != null) {
            s.close();
        }
        log.debug("closing session");
    }


}

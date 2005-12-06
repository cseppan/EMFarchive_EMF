package gov.epa.emissions.framework.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtils {

    private static Log log = LogFactory.getLog(HibernateUtils.class);

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

    public static Session currentSession() throws HibernateException {
        Session session = (Session) sessionThread.get();
        // Open a new Session, if this Thread has none yet
        if (session == null) {
            session = sessionFactory.openSession();
            sessionThread.set(session);
        }
        return session;
    }

    public static void closeSession() throws HibernateException {
        log.debug("closing session");
        Session s = (Session) sessionThread.get();
        sessionThread.set(null);
        if (s != null) {
            s.close();
        }
        log.debug("closing session");
    }

}

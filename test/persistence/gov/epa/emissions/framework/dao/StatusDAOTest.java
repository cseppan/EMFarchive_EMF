package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.Status;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class StatusDAOTest extends ServicesTestCase {

    private StatusDAO dao;

    private UserDAO userDao;

    private Session session;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = new HibernateSessionFactory(sessionFactory());
        dao = new StatusDAO();
        userDao = new UserDAO();
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {// no op
        session.clear();
        session.close();
    }

    public void testShouldGetAllStatusMessages() {
        User emf = userDao.get("emf", session);
        Status status = newStatus(emf);

        try {
            List messages = dao.all(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    private Status newStatus(User emf) {
        Status status = unreadStatus(emf);

        add(status);

        return status(status.getMessage());
    }

    private Status unreadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setMessageType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());
        return status;
    }

    public void testShouldClearPreviousReadMessagesOnGetAll() {
        User emf = userDao.get("emf", session);
        newReadStatus(emf);

        List messages = dao.all(emf.getUsername(), session);
        assertEquals(0, messages.size());
    }

    public void testShouldPersistStatusOnAdd() {
        User emf = userDao.get("emf", session);
        Status status = unreadStatus(emf);

        dao.add(status, session);

        try {
            List messages = dao.all(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    private Status newReadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setMessageType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());

        status.setMsgRead();

        add(status);

        return status(status.getMessage());
    }

    private Status status(String message) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("message", message));
            tx.commit();

            return (Status) crit.uniqueResult();
        } catch (HibernateException e) {
            e.printStackTrace();
            tx.rollback();
            throw e;
        }
    }

    private void add(Status status) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(status);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void remove(Status status) {
        Transaction tx = session.beginTransaction();
        session.delete(status);
        tx.commit();
    }

}

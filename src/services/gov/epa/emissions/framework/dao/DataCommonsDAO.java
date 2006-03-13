package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.IntendedUse;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.services.Status;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAO {

    private LockingScheme lockingScheme;

    private SectorsDAO sectorsDao;

    private HibernateFacade hibernateFacade;

    private KeywordsDAO keywordsDAO;

    public DataCommonsDAO() {
        lockingScheme = new LockingScheme();
        sectorsDao = new SectorsDAO();
        hibernateFacade = new HibernateFacade();
        keywordsDAO = new KeywordsDAO();
    }

    public List getKeywords(Session session) {
        return keywordsDAO.getKeywords(session);
    }

    public void add(Region region, Session session) {
        addObject(region, session);
    }

    public void add(Project project, Session session) {
        addObject(project, session);
    }

    public List getRegions(Session session) {
        return session.createCriteria(Region.class).addOrder(Order.asc("name")).list();
    }

    public List getProjects(Session session) {
        return session.createCriteria(Project.class).addOrder(Order.asc("name")).list();
    }

    public List getCountries(Session session) {
        return session.createCriteria(Country.class).addOrder(Order.asc("name")).list();
    }

    public List getSectors(Session session) {
        return sectorsDao.getAll(session);
    }

    public List getDatasetTypes(Session session) {
        return session.createCriteria(DatasetType.class).addOrder(Order.asc("name").ignoreCase()).list();
    }

    public Sector obtainLockedSector(User user, Sector sector, Session session) {
        return sectorsDao.obtainLocked(user, sector, session);
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type, Session session) {
        return (DatasetType) lockingScheme.getLocked(user, type, session, getDatasetTypes(session));
    }

    public Sector updateSector(Sector sector, Session session) throws EmfException {
        return sectorsDao.update(sector, session);
    }

    public DatasetType updateDatasetType(DatasetType type, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLockOnUpdate(type, session, getDatasetTypes(session));
    }

    public Sector releaseLockedSector(Sector locked, Session session) throws EmfException {
        return sectorsDao.releaseLocked(locked, session);
    }

    public DatasetType releaseLockedDatasetType(DatasetType locked, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLock(locked, session, getDatasetTypes(session));
    }

    public List getStatuses(String username, Session session) {
        removeReadStatus(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username));
            List all = crit.list();

            // mark read
            for (Iterator iter = all.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
                element.markRead();
                session.save(element);

            }
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(Status status, Session session) {
        addObject(status, session);
    }

    private void removeReadStatus(String username, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username)).add(
                    Restrictions.eq("read", Boolean.TRUE));

            List read = crit.list();
            for (Iterator iter = read.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
                session.delete(element);
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getIntendedUses(Session session) {
        return session.createCriteria(IntendedUse.class).addOrder(Order.asc("name")).list();
    }

    public void add(IntendedUse intendedUse, Session session) {
        addObject(intendedUse, session);
    }

    public void add(Country country, Session session) {
        addObject(country, session);
    }

    public void add(DatasetType datasetType, Session session) {
        addObject(datasetType, session);
    }

    public void add(Sector sector, Session session) {
        addObject(sector, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List getNoteTypes(Session session) {
        return session.createCriteria(NoteType.class).list();
    }

    public void add(Revision revision, Session session) {
        addObject(revision, session);
    }

    public void add(Note note, Session session) {
        addObject(note, session);
    }

    public List getRevisions(long datasetId, Session session) {
        return session.createCriteria(Revision.class).add(Restrictions.eq("datasetId", new Long(datasetId))).list();
    }

    public List getNotes(long datasetId, Session session) {
        return session.createCriteria(Note.class).add(Restrictions.eq("datasetId", new Long(datasetId))).list();
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public Object current(long id, Class clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(long id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean canUpdate(Sector sector, Session session) {
        return sectorsDao.canUpdate(sector, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        if (!exists(datasetType.getId(), DatasetType.class, session)) {
            return false;
        }

        DatasetType current = (DatasetType) current(datasetType.getId(), DatasetType.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(datasetType.getName()))
            return true;

        return !nameUsed(datasetType.getName(), DatasetType.class, session);
    }

}
package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.data.IntendedUse;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAO {

    private SectorsDAO sectorsDao;

    private HibernateFacade hibernateFacade;

    private KeywordsDAO keywordsDAO;

    private DatasetTypesDAO datasetTypesDAO;

    private PollutantsDAO pollutantsDAO;

    private SourceGroupsDAO sourceGroupsDAO;

    public DataCommonsDAO() {
        sectorsDao = new SectorsDAO();
        hibernateFacade = new HibernateFacade();
        keywordsDAO = new KeywordsDAO();
        datasetTypesDAO = new DatasetTypesDAO();
        pollutantsDAO = new PollutantsDAO();
        sourceGroupsDAO = new SourceGroupsDAO();
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
        return datasetTypesDAO.getAll(session);
    }

    public Sector obtainLockedSector(User user, Sector sector, Session session) {
        return sectorsDao.obtainLocked(user, sector, session);
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type, Session session) {
        return datasetTypesDAO.obtainLocked(user, type, session);
    }

    public Sector updateSector(Sector sector, Session session) throws EmfException {
        return sectorsDao.update(sector, session);
    }

    public DatasetType updateDatasetType(DatasetType type, Session session) throws EmfException {
        return datasetTypesDAO.update(type, session);
    }

    public Sector releaseLockedSector(Sector locked, Session session) {
        return sectorsDao.releaseLocked(locked, session);
    }

    public DatasetType releaseLockedDatasetType(DatasetType locked, Session session) {
        return datasetTypesDAO.releaseLocked(locked, session);
    }

    public List getPollutants(Session session) {
        return pollutantsDAO.getAll(session);
    }

    public Pollutant updatePollutant(Pollutant pollutant, Session session) throws EmfException {
        return pollutantsDAO.update(pollutant, session);
    }

    public Pollutant obtainLockedPollutant(User user, Pollutant pollutant, Session session) {
        return pollutantsDAO.obtainLocked(user, pollutant, session);
    }

    public Pollutant releaseLockedPollutant(Pollutant locked, Session session) {
        return pollutantsDAO.releaseLocked(locked, session);
    }

    public List getSourceGroups(Session session) {
        return sourceGroupsDAO.getAll(session);
    }

    public SourceGroup updateSourceGroup(SourceGroup sourcegrp, Session session) throws EmfException {
        return sourceGroupsDAO.update(sourcegrp, session);
    }

    public SourceGroup obtainLockedSourceGroup(User user, SourceGroup sourcegrp, Session session) {
        return sourceGroupsDAO.obtainLocked(user, sourcegrp, session);
    }

    public SourceGroup releaseLockedSourceGroup(SourceGroup locked, Session session) {
        return sourceGroupsDAO.releaseLocked(locked, session);
    }

    public List getControlMeasureImportStatuses(String username, Session session) {
        Criterion criterion1 = Restrictions.eq("username", username);
        Criterion criterion2 = Restrictions.eq("type", "CMImportDetailMsg");
        return getStatus(username, new Criterion[] { criterion1, criterion2 }, session);
    }

    public List getStatuses(String username, Session session) {
        Criterion criterion1 = Restrictions.eq("username", username);
        Criterion criterion2 = Restrictions.ne("type", "CMImportDetailMsg");
        return getStatus(username, new Criterion[] { criterion1, criterion2 }, session);
    }

    private List getStatus(String username, Criterion[] criterions, Session session) {
        removeReadStatus(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Criteria crit = session.createCriteria(Status.class).add();
            Criteria crit = session.createCriteria(Status.class);

            for (int i = 0; i < criterions.length; i++) {// add restrictions
                crit = crit.add(criterions[i]);
            }
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
        datasetTypesDAO.add(datasetType, session);
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

    public void add(Pollutant pollutant, Session session) {
        addObject(pollutant, session);
    }

    public void add(SourceGroup sourcegrp, Session session) {
        addObject(sourcegrp, session);
    }

    public List getRevisions(int datasetId, Session session) {
        return session.createCriteria(Revision.class).add(Restrictions.eq("datasetId", new Integer(datasetId))).list();
    }

    public List getNotes(int datasetId, Session session) {
        return session.createCriteria(Note.class).add(Restrictions.eq("datasetId", new Integer(datasetId))).list();
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public Object current(int id, Class clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean canUpdate(Sector sector, Session session) {
        return sectorsDao.canUpdate(sector, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        return datasetTypesDAO.canUpdate(datasetType, session);
    }

}
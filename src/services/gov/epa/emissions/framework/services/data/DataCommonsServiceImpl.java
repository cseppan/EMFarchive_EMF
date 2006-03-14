package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class DataCommonsServiceImpl implements DataCommonsService {

    private static Log LOG = LogFactory.getLog(DataCommonsServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public DataCommonsServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public DataCommonsServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public Keyword[] getKeywords() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List keywords = dao.getKeywords(session);
            session.close();

            return (Keyword[]) keywords.toArray(new Keyword[keywords.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Keywords", e);
            throw new EmfException("Could not get all Keywords");
        }
    }

    public Country[] getCountries() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List countries = dao.getCountries(session);
            session.close();

            return (Country[]) countries.toArray(new Country[countries.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Countries", e);
            throw new EmfException("Could not get all Countries");
        }
    }

    public Sector[] getSectors() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List sectors = dao.getSectors(session);
            session.close();

            return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Sectors", e);
            throw new EmfException("Could not get all Sectors");
        }
    }

    public Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Sector lockedSector = dao.obtainLockedSector(owner, sector, session);
            session.close();

            return lockedSector;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for sector: " + sector.getName() + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for sector: " + sector.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public Sector updateSector(Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (!dao.canUpdate(sector, session))
                throw new EmfException("Sector name already in use");

            Sector released = dao.updateSector(sector, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update sector: " + sector.getName(), e);
            throw new EmfException("Sector name already in use");
        }
    }

    public Sector releaseLockedSector(Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Sector released = dao.releaseLockedSector(sector, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for sector: " + sector.getName() + " by owner: " + sector.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for sector: " + sector.getName() + " by owner: "
                    + sector.getLockOwner());
        }
    }

    public DatasetType[] getDatasetTypes() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List list = dao.getDatasetTypes(session);
            session.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType locked = dao.obtainLockedDatasetType(user, type, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for DatasetType: " + type.getName(), e);
            throw new EmfException("Could not obtain lock for DatasetType: " + type.getName());
        }
    }

    public DatasetType updateDatasetType(DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (!dao.canUpdate(type, session))
                throw new EmfException("DatasetType name already in use");

            DatasetType locked = dao.updateDatasetType(type, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not update DatasetType. Name is already in use: " + type.getName(), e);
            throw new EmfException("DatasetType name already in use");
        }
    }

    public DatasetType releaseLockedDatasetType(User user, DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            DatasetType locked = dao.releaseLockedDatasetType(type, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock on DatasetType: " + type.getName(), e);
            throw new EmfException("Could not release lock on DatasetType: " + type.getName());
        }
    }

    public Status[] getStatuses(String username) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List statuses = dao.getStatuses(username, session);
            session.close();

            return (Status[]) statuses.toArray(new Status[statuses.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Status messages", e);
            throw new EmfException("Could not get all Status messages");
        }
    }

    public Project[] getProjects() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List projects = dao.getProjects(session);
            session.close();

            return (Project[]) projects.toArray(new Project[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Projects", e);
            throw new EmfException("Could not get all Projects");
        }
    }

    public void addProject(Project project) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(project.getName(), Project.class, session))
                throw new EmfException("Project name already in use");

            dao.add(project, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new Project", e);
            throw new EmfException("Project name already in use");
        }
    }

    public Region[] getRegions() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List regions = dao.getRegions(session);
            session.close();

            return (Region[]) regions.toArray(new Region[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Regions", e);
            throw new EmfException("Could not get all Regions");
        }
    }

    public void addRegion(Region region) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(region.getName(), Region.class, session))
                throw new EmfException("Region name already in use");

            dao.add(region, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new Region", e);
            throw new EmfException("Region name already in use");
        }
    }

    public IntendedUse[] getIntendedUses() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List regions = dao.getIntendedUses(session);
            session.close();

            return (IntendedUse[]) regions.toArray(new IntendedUse[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Intended Use", e);
            throw new EmfException("Could not get all Intended Use");
        }
    }

    public void addIntendedUse(IntendedUse intendedUse) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(intendedUse.getName(), IntendedUse.class, session))
                throw new EmfException("Intended use name already in use");

            dao.add(intendedUse, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new intended use", e);
            throw new EmfException("Intended use name already in use");
        }
    }

    public void addCountry(Country country) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(country.getName(), Country.class, session))
                throw new EmfException("Country name already in use");

            dao.add(country, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new country", e);
            throw new EmfException("Country name already in use");
        }
    }

    public void addDatasetType(DatasetType type) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(type.getName(), DatasetType.class, session))
                throw new EmfException("DatasetType name already in use");

            dao.add(type, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new DatasetType", e);
            throw new EmfException("DatasetType name already in use");
        }
    }

    public void addSector(Sector sector) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(sector.getName(), Sector.class, session))
                throw new EmfException("Sector name already in use");

            dao.add(sector, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new Sector.", e);
            throw new EmfException("Sector name already in use");
        }
    }

    public Note[] getNotes(int datasetId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List notes = dao.getNotes(datasetId, session);
            session.close();

            return (Note[]) notes.toArray(new Note[notes.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Notes", e);
            throw new EmfException("Could not get all Notes");
        }
    }

    public void addNote(Note note) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(note.getName(), Note.class, session))
                throw new EmfException("Note name already in use");

            dao.add(note, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new note", e);
            throw new EmfException("Note name already in use");
        }
    }

    public void addNotesB(Note[] notes) throws EmfException {
        for (int i = 0; i < notes.length; i++) {
            this.addNote(notes[i]);
        }
    }

    public void addNotes(Note[] notes) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            for (int i = 0; i < notes.length; i++) {
                Note note = notes[i];

                if (dao.nameUsed(note.getName(), Note.class, session))
                    throw new EmfException("Note name already in use");

                dao.add(note, session);
            }
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new note", e);
            throw new EmfException("Note name already in use");
        }

    }

    public NoteType[] getNoteTypes() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List notetypes = dao.getNoteTypes(session);
            session.close();

            return (NoteType[]) notetypes.toArray(new NoteType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Note Types", e);
            throw new EmfException("Could not get all Note Types");
        }
    }

    public Revision[] getRevisions(int datasetId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List revisions = dao.getRevisions(datasetId, session);
            session.close();

            return (Revision[]) revisions.toArray(new Revision[revisions.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Revisions", e);
            throw new EmfException("Could not get all Revisions");
        }
    }

    public void addRevision(Revision revision) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            dao.add(revision, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add revision", e);
            throw new EmfException("Could not add revision");
        }
    }

}

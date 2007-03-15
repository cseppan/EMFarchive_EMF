package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSerializer;
import gov.epa.emissions.framework.services.basic.EmfServerFileSystemView;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
                throw new EmfException("The Sector name is already in use");

            Sector released = dao.updateSector(sector, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update sector: " + sector.getName(), e);
            throw new EmfException("The Sector name is already in use");
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
                throw new EmfException("The Country name is already in use");

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
                throw new EmfException("The DatasetType name is already in use");

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

    public Pollutant[] getPollutants() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List pollutants = dao.getPollutants(session);
            session.close();

            return (Pollutant[]) pollutants.toArray(new Pollutant[pollutants.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all pollutants", e);
            throw new EmfException("Could not get all pollutants");
        }
    }

    public void addPollutant(Pollutant pollutant) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(pollutant.getName(), Pollutant.class, session))
                throw new EmfException("Pollutant name already in use");

            dao.add(pollutant, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new pollutant.", e);
            throw new EmfException("Pollutant name already in use");
        }
    }

    public SourceGroup[] getSourceGroups() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List sourcegrp = dao.getSourceGroups(session);
            session.close();

            return (SourceGroup[]) sourcegrp.toArray(new SourceGroup[sourcegrp.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all source groups", e);
            throw new EmfException("Could not get all source groups");
        }
    }

    public void addSourceGroup(SourceGroup sourcegrp) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (dao.nameUsed(sourcegrp.getName(), SourceGroup.class, session))
                throw new EmfException("Source group name already in use");

            dao.add(sourcegrp, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new source group.", e);
            throw new EmfException("Source group name already in use");
        }
    }

    public String[] getFiles(String dir) throws EmfException {
        try {
            List<String> files2Return = new ArrayList<String>();
            
            if (dir == null || dir.trim().length() == 0)
                dir = System.getProperty("user.dir");
            
            File dirFile = new File(dir);
            
            if (!dirFile.isDirectory())
                return new String[] {dir};
            
            File[] files = dirFile.listFiles();
            
            for (int i = 0; i < files.length; i++)
                files2Return.add(files[i].getAbsolutePath());
            
            return files2Return.toArray(new String[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }

    public EmfFileInfo createNewFolder(String folder) throws EmfException {
        try {
            if (folder == null || folder.trim().isEmpty())
                return null;
            
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File newfolder = fsv.createNewFolder(new File(folder));
            
            return EmfFileSerializer.convert(newfolder);
        } catch (Exception e) {
            LOG.error("Could not create new folder " + folder + ". ", e);
            throw new EmfException("Could not create new folder " + folder + " " + e.getMessage());
        }
    }

    public EmfFileInfo[] getEmfFileInfos(EmfFileInfo dir) throws EmfException {
        try {
            if (dir == null) {
                EmfServerFileSystemView fsv = new EmfServerFileSystemView();
                return getFileInfosList(fsv.getHomeDirectory());
            }
            
            File dirFile = EmfFileSerializer.convert(dir);
            
            if (!dirFile.isDirectory())
                return new EmfFileInfo[] {dir};
            
            return getFileInfosList(dirFile);
        } catch (Exception e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }

    private EmfFileInfo[] getFileInfosList(File dirFile) throws IOException {
        List<EmfFileInfo> files2Return = new ArrayList<EmfFileInfo>();
        File[] files = dirFile.listFiles();
        
        for (int i = 0; i < files.length; i++)
            files2Return.add(EmfFileSerializer.convert(files[i]));
        
        return files2Return.toArray(new EmfFileInfo[0]);
    }

    public EmfFileInfo getDefaultDir() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getDefaultDirectory());
        } catch (IOException e) {
            LOG.error("Could not get default directory.", e);
            throw new EmfException("Could not get get default directory.");
        }
    }

    public EmfFileInfo getHomeDir() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getHomeDirectory());
        } catch (IOException e) {
            LOG.error("Could not get home directory.", e);
            throw new EmfException("Could not get home directory.");
        }
    }

}

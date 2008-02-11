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
import gov.epa.emissions.framework.services.basic.EmfFilePatternMatcher;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataCommonsServiceImpl implements DataCommonsService {

    private static Log LOG = LogFactory.getLog(DataCommonsServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    private EmfFileInfo[] files;

    private EmfFileInfo[] subdirs;

    private EmfFileInfo currentDirectory;

    public DataCommonsServiceImpl() {
        this(HibernateSessionFactory.get());
    }

    public DataCommonsServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

    public synchronized Keyword[] getKeywords() throws EmfException {
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

    public synchronized Country[] getCountries() throws EmfException {
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

    public synchronized Sector[] getSectors() throws EmfException {
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

    public synchronized Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
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

    public synchronized Sector updateSector(Sector sector) throws EmfException {
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

    public synchronized Sector releaseLockedSector(Sector sector) throws EmfException {
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

    public synchronized DatasetType[] getDatasetTypes() throws EmfException {
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

    public synchronized DatasetType getDatasetType(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getDatasetType(name, session);
        } catch (HibernateException e) {
            LOG.error("Could not get DatasetType", e);
            throw new EmfException("Could not get DatasetType");
        } finally {
            session.close();
        }
    }

    public synchronized DatasetType obtainLockedDatasetType(User user, DatasetType type) throws EmfException {
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

    public synchronized DatasetType updateDatasetType(DatasetType type) throws EmfException {
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

    public synchronized DatasetType releaseLockedDatasetType(User user, DatasetType type) throws EmfException {
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

    public synchronized Status[] getStatuses(String username) throws EmfException {
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

    public synchronized Project[] getProjects() throws EmfException {
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

    public synchronized Project addProject(Project project) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (dao.nameUsed(project.getName(), Project.class, session))
                throw new EmfException("Project name already in use");

            dao.add(project, session);
            return (Project) dao.load(Project.class, project.getName(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not add new Project", e);
            throw new EmfException("Project name already in use");
        } finally {
            session.close();
        }
    }

    public synchronized Region[] getRegions() throws EmfException {
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

    public synchronized void addRegion(Region region) throws EmfException {
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

    public synchronized IntendedUse[] getIntendedUses() throws EmfException {
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

    public synchronized void addIntendedUse(IntendedUse intendedUse) throws EmfException {
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

    public synchronized void addCountry(Country country) throws EmfException {
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

    public synchronized void addDatasetType(DatasetType type) throws EmfException {
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

    public synchronized void addSector(Sector sector) throws EmfException {
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

    public synchronized Note[] getNotes(int datasetId) throws EmfException {
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

    public synchronized void addNote(Note note) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            //check has been done on the client side
//            if (dao.nameUsed(note.getName(), Note.class, session))
//                throw new EmfException("Note name already in use");

            dao.add(note, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new note", e);
            throw new EmfException("Note name already in use");
        }
    }

    public synchronized void addNotesB(Note[] notes) throws EmfException {
        for (int i = 0; i < notes.length; i++) {
            this.addNote(notes[i]);
        }
    }

    public synchronized void addNotes(Note[] notes) throws EmfException {
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

    public synchronized NoteType[] getNoteTypes() throws EmfException {
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

    public synchronized Revision[] getRevisions(int datasetId) throws EmfException {
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

    public synchronized void addRevision(Revision revision) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            dao.add(revision, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add revision", e);
            throw new EmfException("Could not add revision");
        }
    }

    public synchronized Pollutant[] getPollutants() throws EmfException {
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

    public synchronized void addPollutant(Pollutant pollutant) throws EmfException {
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

    public synchronized SourceGroup[] getSourceGroups() throws EmfException {
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

    public synchronized void addSourceGroup(SourceGroup sourcegrp) throws EmfException {
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

    public synchronized File[] getFiles(File[] dir) throws EmfException {
        try {
            if (dir[0] == null) {
                EmfServerFileSystemView fsv = new EmfServerFileSystemView();
                return fsv.getDefaultDirectory().listFiles();
            }

            return dir[0].listFiles();
        } catch (RuntimeException e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }

    public synchronized EmfFileInfo createNewFolder(String folder, String subfolder) throws EmfException {
        try {
            if (folder == null || folder.trim().isEmpty())
                return null;

            if (subfolder == null || subfolder.trim().isEmpty())
                return null;

            File subdir = new File(folder, subfolder);
            
            if (subdir.exists())
                throw new EmfException("Subfolder " + subfolder + " already exists.");
            
            if (subdir.mkdirs()) {
                subdir.setWritable(true, false);
                return EmfFileSerializer.convert(subdir);
            }

            return null;
        } catch (Exception e) {
            LOG.error("Could not create new folder " + folder + File.separator + subfolder + ". ", e);
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized EmfFileInfo getDefaultDir() throws EmfException {
        try {
            //NOTE: FileSystemView doesn't work well on Linux platform
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getDefaultDirectory());
        } catch (IOException e) {
            LOG.error("Could not get default directory.", e);
            throw new EmfException("Could not get get default directory.");
        }
    }

    public synchronized EmfFileInfo getHomeDir() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getHomeDirectory());
        } catch (IOException e) {
            LOG.error("Could not get home directory.", e);
            throw new EmfException("Could not get home directory.");
        }
    }

    public synchronized EmfFileInfo[] getRoots() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File[] roots = fsv.getRoots();
            return getFileInfos(roots);
        } catch (IOException e) {
            LOG.error("Could not get file system roots.", e);
            throw new EmfException("Could not file system roots.");
        }
    }

    private synchronized EmfFileInfo[] getFileInfos(File[] roots) throws IOException {
        EmfFileInfo[] infos = new EmfFileInfo[roots.length];

        for (int i = 0; i < infos.length; i++)
            infos[i] = EmfFileSerializer.convert(roots[i]);

        return infos;
    }

    public synchronized boolean isRoot(EmfFileInfo fileInfo) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return fsv.isRoot(EmfFileSerializer.convert(fileInfo));
        } catch (Exception e) {
            throw new EmfException("Could not determine roots.");
        }
    }

    public synchronized boolean isFileSystemRoot(EmfFileInfo fileInfo) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return fsv.isFileSystemRoot(EmfFileSerializer.convert(fileInfo));
        } catch (Exception e) {
            throw new EmfException("Could not determine roots.");
        }
    }

    public synchronized EmfFileInfo getChild(EmfFileInfo file, String child) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File childFile = fsv.getChild(EmfFileSerializer.convert(file), child);
            return EmfFileSerializer.convert(childFile);
        } catch (Exception e) {
            throw new EmfException("Could not get a child.");
        }
    }

    public synchronized EmfFileInfo getParentDirectory(EmfFileInfo file) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File parentFile = fsv.getParentDirectory(EmfFileSerializer.convert(file));
            return EmfFileSerializer.convert(parentFile);
        } catch (Exception e) {
            throw new EmfException("Could not get a parent directory.");
        }
    }

    public synchronized EmfFileInfo[] getSubdirs(EmfFileInfo dir) throws EmfException {
        try {
            EmfFileInfo gooddir = correctEmptyDir(dir);
            if (currentDirectory != null && gooddir.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
                return this.subdirs != null ? this.subdirs : new EmfFileInfo[0];
            }
            
            currentDirectory = gooddir;
            File currentdirFile = new File(currentDirectory.getAbsolutePath());
            listDirsAndFiles(currentdirFile.listFiles(), currentdirFile, "*");

            return this.subdirs != null ? this.subdirs : new EmfFileInfo[0];
        } catch (Exception e) {
            LOG.warn("Could not get subdirectories of  " + dir.getAbsolutePath() + ": " + e.getMessage());
            throw new EmfException("Could not get subdirectories of  " + dir.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private synchronized EmfFileInfo correctEmptyDir(EmfFileInfo dir) throws IOException {
        boolean resetPath = false;

        if (dir == null || dir.getAbsolutePath() == null || dir.getAbsolutePath().trim().equals(""))
            resetPath = true;
        else {
            File f = new File(dir.getAbsolutePath());

            if (f.isFile())
                return EmfFileSerializer.convert(f.getParentFile());
            
            if (!f.exists())
                resetPath = true;
        }

        if (resetPath) {
            if (File.separatorChar == '/') {
                dir.setAbsolutePath("/");
                dir.setName("/");
            } else {
                dir.setAbsolutePath("C:\\");
                dir.setName("C:\\");
            }
        }

        return dir;
    }

    private synchronized void listDirsAndFiles(File[] files, File cur, String filter) throws IOException {
        List<EmfFileInfo> subdirsOfCurDir = new ArrayList<EmfFileInfo>();
        List<EmfFileInfo> filesOfCurDir = new ArrayList<EmfFileInfo>();
        EmfFileInfo curInfo = EmfFileSerializer.convert(cur);
        curInfo.setName(".");
        subdirsOfCurDir.add(0, curInfo);

        EmfFileInfo parentdir = EmfFileSerializer.convert(cur);
        parentdir.setName("..");
        boolean isRoot = false;

        // if it's the root, you don't have ..
        if (File.separatorChar == '/') {
            if (cur.getAbsolutePath().equals("/"))
                isRoot = true;
        } else { // Windows
            if (cur.getAbsolutePath().length() == 3)
                isRoot = true;
        }

        if (!isRoot) {
            parentdir = EmfFileSerializer.convert(cur.getParentFile());
            parentdir.setName("..");
            subdirsOfCurDir.add(1, parentdir);
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                subdirsOfCurDir.add(EmfFileSerializer.convert(files[i]));
            } else {
                filesOfCurDir.add(EmfFileSerializer.convert(files[i]));
            }
        }

        this.files = getFileinfosFromPattern(filesOfCurDir.toArray(new EmfFileInfo[0]), filter);
        this.subdirs = subdirsOfCurDir.toArray(new EmfFileInfo[0]);
    }

    public synchronized EmfFileInfo[] getEmfFileInfos(EmfFileInfo dir, String filter) throws EmfException {
        try {
            EmfFileInfo gooddir = correctEmptyDir(dir);
            if (currentDirectory != null && gooddir.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
                return this.files == null ? new EmfFileInfo[0] : this.files;
            }

            currentDirectory = gooddir;
            File currentdirFile = new File(currentDirectory.getAbsolutePath());
            listDirsAndFiles(currentdirFile.listFiles(), currentdirFile, filter);

            return this.files != null ? this.files : new EmfFileInfo[0];
        } catch (Exception e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }
    
    private synchronized EmfFileInfo[] getFileinfosFromPattern(EmfFileInfo[] fileInfos, String pattern) throws EmfException {
        try {
            if (pattern == null || fileInfos.length == 0)
                return fileInfos;
                
            String pat = pattern.trim();
            
            if (pat.length() == 0)
                return fileInfos;
            
            File directory = new File(fileInfos[0].getParent());
            EmfFilePatternMatcher fpm = new EmfFilePatternMatcher(directory, pat);
            EmfFileInfo[] matchedNames = fpm.getMatched(fileInfos);
            if (matchedNames.length == 0)
                throw new EmfException("No files found for pattern '" + pattern + "'");

            return matchedNames;
        } catch (Exception e) {
            throw new EmfException("Cannot apply pattern.");
        }
    }

}

package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.impl.DataServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

import java.util.Date;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataCommonsServiceTest extends ServicesTestCase {

    private DataCommonsService service;
    private DataService dataService;
    private UserService userService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        service = new DataCommonsServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        dataService = new DataServiceImpl(sessionFactory);
    }

    public void testShouldReturnCompleteListOfSectors() throws EmfException {
        Sector[] sectors = service.getSectors();
        assertTrue(sectors.length >= 14);
    }

    private Sector currentSector(Sector target) throws EmfException {
        Sector[] sectors = service.getSectors();
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].equals(target))
                return sectors[i];
        }

        return null;
    }

    private DatasetType currentDatasetType(DatasetType target) throws EmfException {
        DatasetType[] list = service.getDatasetTypes();
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(target))
                return list[i];
        }

        return null;
    }

    public void testShouldGetSectorLock() throws EmfException {
        User owner = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector locked = service.obtainLockedSector(owner, sector);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        // Sector object returned directly from the sector table
        Sector loadedFromDb = currentSector(sector);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldGetLockOnDatasetType() throws EmfException {
        User user = userService.getUser("emf");
        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];

        DatasetType locked = service.obtainLockedDatasetType(user, type);
        assertEquals(locked.getLockOwner(), user.getUsername());

        // Sector object returned directly from the sector table
        DatasetType loadedFromDb = currentDatasetType(type);
        assertEquals(loadedFromDb.getLockOwner(), user.getUsername());
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User owner = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector locked = service.obtainLockedSector(owner, sector);
        Sector released = service.releaseLockedSector(locked);
        assertFalse("Should have released lock", released.isLocked());

        Sector loadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldReleaseDatasetTypeLock() throws EmfException {
        User owner = userService.getUser("emf");
        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];

        DatasetType locked = service.obtainLockedDatasetType(owner, type);
        DatasetType released = service.releaseLockedDatasetType(owner, locked);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldAddDatasetType() throws EmfException {
        String newname = "MyDatasetType" + Math.abs(new Random().nextInt());
        DatasetType newtype = new DatasetType(newname);
        newtype.setDescription("MyDatasetType, newly added type.");
        newtype.setKeywords(new Keyword[0]);
        int existingTypes = service.getDatasetTypes().length;

        service.addDatasetType(newtype);

        try {
            assertEquals(existingTypes + 1, service.getDatasetTypes().length);
            assertTrue(currentDatasetType(newtype).getName().equalsIgnoreCase(newname));
        } finally {
            remove(newtype);
        }
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        User owner = userService.getUser("emf");

        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];
        String name = type.getName();

        DatasetType modified1 = service.obtainLockedDatasetType(owner, type);
        assertEquals(modified1.getLockOwner(), owner.getUsername());
        modified1.setName("TEST");

        DatasetType modified2 = service.updateDatasetType(modified1);
        assertEquals("TEST", modified2.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        DatasetType modified = service.obtainLockedDatasetType(owner, type);
        modified.setName(name);

        DatasetType modified3 = service.updateDatasetType(modified);
        assertEquals(type.getName(), modified3.getName());
    }

    public void testShouldFailOnAttemptToAddDatasetTypeWithDuplicateName() throws EmfException {
        String newname = "MyDatasetType" + Math.abs(new Random().nextInt());
        DatasetType type1 = new DatasetType(newname);
        type1.setDescription("MyDatasetType, newly added type.");
        type1.setKeywords(new Keyword[0]);
        service.addDatasetType(type1);

        try {
            DatasetType type2 = new DatasetType(newname);
            type2.setDescription("duplicate type");
            type2.setKeywords(new Keyword[0]);

            service.addDatasetType(type2);
        } catch (EmfException e) {
            assertEquals("DatasetType name already in use", e.getMessage());
            return;
        } finally {
            remove(type1);
        }
    }

    public void testShouldFailOnAttemptToUpdateDatasetTypeWithDuplicateName() throws EmfException {
        String newname = "MyDatasetType" + Math.abs(new Random().nextInt());
        DatasetType type1 = new DatasetType(newname);
        type1.setDescription("MyDatasetType, newly added type.");
        type1.setKeywords(new Keyword[0]);
        service.addDatasetType(type1);

        DatasetType type2 = new DatasetType(newname + "foobar");
        type2.setDescription("duplicate type");
        type2.setKeywords(new Keyword[0]);
        service.addDatasetType(type2);

        try {
            type2.setName(newname);
            service.updateDatasetType(type2);            
        } catch (EmfException e) {
            assertEquals("DatasetType name already in use", e.getMessage());
            return;
        } finally {
            remove(type1);
        }
    }

    public void testShouldAddSector() throws EmfException {
        String newname = "MySector" + Math.abs(new Random().nextInt());
        Sector newSector = new Sector(newname, newname);
        boolean newSectorAdded = false;
        int existingSectors = service.getSectors().length;

        service.addSector(newSector);
        try {
            Sector[] sectors = service.getSectors();
            for (int i = 0; i < sectors.length; i++)
                if (sectors[i].getName().equalsIgnoreCase(newname))
                    newSectorAdded = true;

            assertEquals(existingSectors + 1, service.getSectors().length);
            assertTrue(newSectorAdded);
        } finally {
            remove(newSector);
        }
    }

    public void testShouldUpdateSector() throws EmfException {
        User owner = userService.getUser("emf");

        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];
        String name = sector.getName();

        Sector modified1 = service.obtainLockedSector(owner, sector);
        assertEquals(modified1.getLockOwner(), owner.getUsername());
        modified1.setName("TEST");

        Sector modified2 = service.updateSector(modified1);
        assertEquals("TEST", modified2.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        Sector modified = service.obtainLockedSector(owner, sector);
        modified.setName(name);
        Sector modified3 = service.updateSector(modified);
        assertEquals(sector.getName(), modified3.getName());
    }

    public void testShouldFailOnAttemptToAddSectorWithDuplicateName() throws EmfException {
        String name = "MySector" + Math.abs(new Random().nextInt());
        Sector sector1 = new Sector("Desc", name);
        service.addSector(sector1);

        try {
            Sector sector2 = new Sector("Desc", name);
            service.addSector(sector2);
        } catch (EmfException e) {
            assertEquals("Sector name already in use", e.getMessage());
            return;
        } finally {
            remove(sector1);
        }

        fail("Should have failed to add Sector w/ duplicate name");
    }

    public void testShouldFailOnAttemptToUpdateSectorWithDuplicateName() throws EmfException {
        String name = "MySector" + Math.abs(new Random().nextInt());
        Sector sector1 = new Sector("Desc", name);
        service.addSector(sector1);

        Sector sector2 = new Sector("Desc", name+"foobar");
        service.addSector(sector2);

        try {
            sector2.setName(name);
            service.addSector(sector2);
            
        } catch (EmfException e) {
            assertEquals("Sector name already in use", e.getMessage());
            return;
        } finally {
            remove(sector1);
        }

        fail("Should have failed to add Sector w/ duplicate name");
    }

    public void testShouldAddProject() throws EmfException {
        String newname = "MyProject" + Math.abs(new Random().nextInt());
        Project newProject = new Project(newname);
        boolean newProjectAdded = false;
        int existingProjects = service.getProjects().length;

        service.addProject(newProject);
        try {
            Project[] projects = service.getProjects();
            for (int i = 0; i < projects.length; i++)
                if (projects[i].getName().equalsIgnoreCase(newname))
                    newProjectAdded = true;

            assertEquals(existingProjects + 1, service.getProjects().length);
            assertTrue(newProjectAdded);
        } finally {
            remove(newProject);
        }
    }

    public void testShouldFailOnAttemptToAddProjectWithDuplicateName() throws EmfException {
        String name = "MyProject" + Math.abs(new Random().nextInt());
        Project project1 = new Project(name);
        service.addProject(project1);

        try {
            Project project2 = new Project(name);
            service.addProject(project2);
        } catch (EmfException e) {
            assertEquals("Project name already in use", e.getMessage());
            return;
        } finally {
            remove(project1);
        }

        fail("Should have failed to add Sector w/ duplicate name");
    }

    public void testShouldFailOnAttemptToUpdateProjectWithDuplicateName() throws EmfException {
        String name = "Myproject" + Math.abs(new Random().nextInt());
        Project project1 = new Project(name);
        service.addProject(project1);

        Project project2 = new Project(name+"foobar");
        service.addProject(project2);

        try {
            project2.setName(name);
            service.addProject(project2);
            
        } catch (EmfException e) {
            assertEquals("Project name already in use", e.getMessage());
            return;
        } finally {
            remove(project1);
        }

        fail("Should have failed to add Sector w/ duplicate name");
    }

    public void testShouldGetAllStatusesAndRemovePreviouslyRead() throws Exception {
        User owner = userService.getUser("emf");
        newStatus(owner.getUsername());

        Status[] firstRead = service.getStatuses(owner.getUsername());
        assertEquals(1, firstRead.length);

        Status[] secondRead = service.getStatuses(owner.getUsername());
        assertEquals(0, secondRead.length);
    }

    private void newStatus(String username) {
        Status status = new Status();
        status.setMessage("test message");
        status.setType("type");
        status.setUsername(username);
        status.setTimestamp(new Date());

        save(status);
    }

    private void save(Status status) {
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

    public void testShouldGetAllNoteTypes() throws EmfException {
            NoteType[] notetypes = service.getNoteTypes();
            assertEquals("5 note types should return", notetypes.length,5);
    }

    public void testShouldAddNote() throws EmfException {
        long id = Math.abs(new Random().nextInt());
        User user = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        dataService.addDataset(dataset);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note = new Note(user,datasetFromDB.getId(),new Date(),"NOTE DETAILS","NOTE NAME"+id, loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        service.addNote(note);
        boolean newNoteAdded = false;
        try {
            Note[] notes = service.getNotes(datasetFromDB.getId());
            for (int i = 0; i < notes.length; i++)
                if (notes[i].getName().equalsIgnoreCase("NOTE NAME"+id))
                    newNoteAdded = true;

            assertTrue(newNoteAdded);
        } finally {
            remove(note);
            remove(dataset);
        }
    }

    public void testShouldGetAllNotes() throws EmfException {
        long id = Math.abs(new Random().nextInt());
        User user = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        dataService.addDataset(dataset);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());

        Note note1 = new Note(user,datasetFromDB.getId(),new Date(),"NOTE DETAILS","NOTE NAME1"+id, loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        service.addNote(note1);
        Note note2 = new Note(user,datasetFromDB.getId(),new Date(),"NOTE DETAILS","NOTE NAME2"+id, loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        service.addNote(note2);

        try {
            Note[] notes = service.getNotes(datasetFromDB.getId());
            assertEquals("Two notes should return", notes.length,2);
        } finally {
            remove(note1);
            remove(note2);
            remove(dataset);
        }
    }

    private EmfDataset newDataset() {
        Random rando = new Random();
        long id = Math.abs(rando.nextInt());
        EmfDataset dataset = new EmfDataset();

        dataset.setName("FOO_" + id);
        dataset.setAccessedDateTime(new Date());
        dataset.setCreatedDateTime(new Date());
        dataset.setDescription("DESCRIPTION");
        dataset.setModifiedDateTime(new Date());
        dataset.setStartDateTime(new Date());
        dataset.setStatus("imported");
        dataset.setYear(42);
        dataset.setUnits("orl");
        dataset.setTemporalResolution("t1");
        dataset.setStopDateTime(new Date());

        return dataset;
    }

    private NoteType loadNoteType(String type) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(NoteType.class).add(Restrictions.eq("type", type));
            tx.commit();

            return (NoteType) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private EmfDataset loadDataset(String name) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", name));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    protected void doTearDown() throws Exception {// no op
    }

    
}

package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.IntendedUse;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;
import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.Status;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataCommonsDAOTest extends ServiceTestCase {

    private DataCommonsDAO dao;

    private DatasetDao datasetDAO;

    private UserDAO userDao;

    protected void doSetUp() throws Exception {
        dao = new DataCommonsDAO();
        userDao = new UserDAO();
        datasetDAO = new DatasetDao();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllKeywords() {
        int totalBeforeAdd = dao.getKeywords(session).size();
        Keyword keyword = new Keyword("test" + Math.random());
        add(keyword);

        try {
            List keywords = dao.getKeywords(session);
            assertEquals(totalBeforeAdd + 1, keywords.size());
            assertEquals(keyword, keywords.get(0));
        } finally {
            remove(keyword);
        }
    }

    public void testShouldGetAllCountries() {
        int totalBeforeAdd = dao.getCountries(session).size();
        Country country = new Country("test" + Math.random());
        add(country);

        try {
            List countries = dao.getCountries(session);
            assertEquals(totalBeforeAdd + 1, countries.size());
            assertTrue(countries.contains(country));
        } finally {
            remove(country);
        }
    }

    public void testShouldGetAllProjects() {
        int totalBeforeAdd = dao.getProjects(session).size();
        Project project = new Project("test" + Math.random());
        add(project);

        try {
            List list = dao.getProjects(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(project));
        } finally {
            remove(project);
        }
    }

    public void testShouldGetAllRegions() {
        int totalBeforeAdd = dao.getRegions(session).size();
        Region region = new Region("test" + Math.random());
        add(region);

        try {
            List list = dao.getRegions(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(region));
        } finally {
            remove(region);
        }
    }

    public void testShouldGetAllIntendedUses() {
        int totalBeforeAdd = dao.getIntendedUses(session).size();
        IntendedUse newElement = new IntendedUse("test" + Math.random());
        add(newElement);

        try {
            List list = dao.getIntendedUses(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(newElement));
        } finally {
            remove(newElement);
        }
    }

    public void testShouldGetAllSectors() {
        List sectors = dao.getSectors(session);
        assertTrue(sectors.size() >= 14);
    }

    public void testShouldGetAllDatasetTypes() {
        List types = dao.getDatasetTypes(session);
        assertTrue(types.size() >= 10);
    }

    private Sector currentSector(Sector target) {
        List sectors = dao.getSectors(session);
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector element = (Sector) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    private DatasetType currentDatasetType(DatasetType target) {
        List list = dao.getDatasetTypes(session);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            DatasetType element = (DatasetType) iter.next();
            if (element.equals(target))
                return element;
        }

        return null;
    }

    public void testShouldGetSectorLock() {
        User user = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLockedSector(user, sector, session);
        assertEquals(lockedSector.getLockOwner(), user.getUsername());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertEquals(sectorLoadedFromDb.getLockOwner(), user.getUsername());
    }

    public void testShouldGetDatasetTypeLock() {
        User owner = userDao.get("emf", session);
        List types = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) types.get(0);

        DatasetType locked = dao.obtainLockedDatasetType(owner, type, session);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldGetLockOnDatasetTypeContainingKeyVals() {
        User owner = userDao.get("emf", session);
        
        DatasetType newType = new DatasetType("name" + Math.random());
        newType.setDescription("MyDatasetType, newly added type.");
        Keyword keyword = new Keyword("test");
        KeyVal keyVal = new KeyVal(keyword, "val");
        newType.setKeyVals(new KeyVal[] { keyVal });

        try {
            dao.add(newType, session);

            DatasetType locked = dao.obtainLockedDatasetType(owner, newType, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            DatasetType loadedFromDb = currentDatasetType(newType);
            assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
        } finally {
            remove(newType);
            remove(keyword);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        User owner = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        dao.obtainLockedSector(owner, sector, session);

        User user = userDao.get("admin", session);
        Sector result = dao.obtainLockedSector(user, sector, session);

        assertTrue(result.isLocked(owner));// locked by owner
        assertFalse(result.isLocked(user));// failed to obtain lock for another user
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User owner = userDao.get("emf", session);
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        Sector lockedSector = dao.obtainLockedSector(owner, sector, session);
        Sector releasedSector = dao.releaseLockedSector(lockedSector, session);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldReleaseDatasetTypeLock() throws EmfException {
        User owner = userDao.get("emf", session);
        List list = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) list.get(0);

        DatasetType locked = dao.obtainLockedDatasetType(owner, type, session);
        DatasetType released = dao.releaseLockedDatasetType(locked, session);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldFailToReleaseSectorLockIfNotObtained() {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);

        try {
            dao.releaseLockedSector(sector, session);
        } catch (EmfException e) {
            assertEquals("Cannot release without owning lock", e.getMessage());
            return;
        }

        fail("Should have failed to release lock that was not obtained");
    }

    public void testShouldUpdateSector() throws EmfException {
        List sectors = dao.getSectors(session);
        Sector sector = (Sector) sectors.get(0);
        String name = sector.getName();

        User owner = userDao.get("emf", session);

        Sector modifiedSector1 = dao.obtainLockedSector(owner, sector, session);
        assertEquals(modifiedSector1.getLockOwner(), owner.getUsername());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = dao.updateSector(modifiedSector1, session);
        assertEquals("TEST", modifiedSector1.getName());
        assertEquals(modifiedSector2.getLockOwner(), null);

        // restore
        Sector modifiedSector = dao.obtainLockedSector(owner, sector, session);
        modifiedSector.setName(name);

        Sector modifiedSector3 = dao.updateSector(modifiedSector, session);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        List types = dao.getDatasetTypes(session);
        DatasetType type = (DatasetType) types.get(0);
        String name = type.getName();

        User owner = userDao.get("emf", session);

        DatasetType modified1 = dao.obtainLockedDatasetType(owner, type, session);
        assertEquals(modified1.getLockOwner(), owner.getUsername());
        modified1.setName("TEST");

        DatasetType modified2 = dao.updateDatasetType(modified1, session);
        assertEquals("TEST", modified1.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        DatasetType modified = dao.obtainLockedDatasetType(owner, type, session);
        modified.setName(name);

        DatasetType modified3 = dao.updateDatasetType(modified, session);
        assertEquals(type.getName(), modified3.getName());
    }

    public void testShouldGetStatuses() {
        clearStatuses();

        User emf = userDao.get("emf", session);
        Status status = newStatus(emf);

        try {
            List afterInsert = dao.getStatuses(emf.getUsername(), session);
            assertEquals(1, afterInsert.size());
        } finally {
            remove(status);
        }
    }

    private void clearStatuses() {
        Transaction tx = session.beginTransaction();
        List all = session.createCriteria(Status.class).list();
        for (Iterator iter = all.iterator(); iter.hasNext();) {
            Status element = (Status) iter.next();
            session.delete(element);
        }
        tx.commit();
    }

    public void testShouldMarkCurrentMessagesAsReadAndRemoveThemOnSubsequentFetchOfStatuses() {
        User emf = userDao.get("emf", session);
        newStatus(emf);

        List firstRead = dao.getStatuses(emf.getUsername(), session);
        assertEquals(1, firstRead.size());

        List secondRead = dao.getStatuses(emf.getUsername(), session);
        assertEquals(0, secondRead.size());
    }

    private Status newStatus(User emf) {
        Status status = unreadStatus(emf);

        add(status);

        return status(status.getMessage());
    }

    private Status unreadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());
        return status;
    }

    public void testShouldClearPreviousReadMessagesAnMarkCurrentMessagesAsReadOnGetAll() {
        User emf = userDao.get("emf", session);
        newReadStatus(emf);

        List messages = dao.getStatuses(emf.getUsername(), session);
        assertEquals(0, messages.size());
    }

    public void testShouldPersistStatusOnAdd() {
        User emf = userDao.get("emf", session);
        Status status = unreadStatus(emf);

        dao.add(status, session);

        try {
            List messages = dao.getStatuses(emf.getUsername(), session);
            assertEquals(1, messages.size());
        } finally {
            remove(status);
        }
    }

    public void testShouldPersistRevisionOnAdd() {
        Revision rev = null;

        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        rev = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE", "WHY ONE",
                "NOTE ONE");

        dao.add(rev, session);

        try {
            List allRevisions = dao.getRevisions(datasetFromDB.getId(), session);
            assertEquals(1, allRevisions.size());
        } finally {
            remove(rev);
            remove(dataset);
        }
    }

    public void testShouldAddANDGetThreeRevisions() {
        Revision rev1 = null, rev2 = null, rev3 = null;

        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());

        rev1 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT ONE",
                "WHY ONE", "NOTE ONE");
        rev2 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT TWO",
                "WHY TWO", "NOTE TWO");
        rev3 = new Revision(user, datasetFromDB.getId(), new Date(), dataset.getDefaultVersion(), "WHAT THREE",
                "WHY THREE", "NOTE THREE");
        dao.add(rev1, session);
        dao.add(rev2, session);
        dao.add(rev3, session);
        try {
            List allRevisions = dao.getRevisions(datasetFromDB.getId(), session);
            assertEquals(3, allRevisions.size());
        } finally {
            remove(rev1);
            remove(rev2);
            remove(rev3);
            remove(dataset);
        }
    }

    public void testShouldPersistNoteOnAdd() {
        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());

        dao.add(note, session);

        try {
            List allNotes = dao.getNotes(datasetFromDB.getId(), session);
            assertEquals(1, allNotes.size());
        } finally {
            remove(note);
            remove(dataset);
        }
    }

    public void testShouldGetAllNotes() {
        User user = userDao.get("emf", session);
        EmfDataset dataset = newDataset();
        dataset.setCreator(user.getUsername());
        datasetDAO.add(dataset, session);
        EmfDataset datasetFromDB = loadDataset(dataset.getName());
        Note note1 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS", "NOTE NAME",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());
        Note note2 = new Note(user, datasetFromDB.getId(), new Date(), "NOTE DETAILS2", "NOTE NAME 2",
                loadNoteType("Observation"), "abcd", dataset.getDefaultVersion());

        dao.add(note1, session);

        dao.add(note2, session);
        try {
            List allNotes = dao.getNotes(datasetFromDB.getId(), session);
            assertEquals(2, allNotes.size());
        } finally {
            remove(note1);
            remove(note2);
            remove(dataset);
        }
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

    public void testShouldGetNoteTypes() {
        List allNoteTypes = dao.getNoteTypes(session);
        assertEquals("7 note types", allNoteTypes.size(), 7);
    }

    private Status newReadStatus(User emf) {
        Status status = new Status();
        status.setUsername(emf.getUsername());
        status.setType("type");
        status.setMessage("message");
        status.setTimestamp(new Date());

        status.markRead();

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

}

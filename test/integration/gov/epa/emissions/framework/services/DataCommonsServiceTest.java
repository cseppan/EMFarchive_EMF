package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

import java.util.Date;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;

public class DataCommonsServiceTest extends ServicesTestCase {

    private DataCommonsService service;

    private UserService userService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        service = new DataCommonsServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
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
    
    public void testShouldAddDatasetType() throws EmfException {
        String newname = "MyDatasetType" + Math.abs(new Random().nextInt());
        DatasetType newtype = new DatasetType(newname);
        newtype.setDescription("MyDatasetType, newly added type.");
        newtype.setKeywords(new Keyword[0]);
        newtype.setMinFiles(1);
        newtype.setMaxFiles(1);
        newtype.setImporterClassName("importer");
        newtype.setExporterClassName("exporter");
        newtype.setExternal(false);
        int existingTypes = service.getDatasetTypes().length;
        service.addDatasetType(newtype);
        
        assertEquals(existingTypes + 1, service.getDatasetTypes().length);
        assertTrue(currentDatasetType(newtype).getName().equalsIgnoreCase(newname));
    }
    
    public void testShouldAddSector() throws EmfException {
        String newname = "MySector" + Math.abs(new Random().nextInt());
        Sector newSector = new Sector(newname, newname);
        boolean newSectorAdded = false;
        int existingSectors = service.getSectors().length;
        
        service.addSector(newSector);
        Sector[] sectors = service.getSectors();
        for(int i = 0; i < sectors.length; i++)
            if(sectors[i].getName().equalsIgnoreCase(newname))
                newSectorAdded = true;
        
        assertEquals(existingSectors + 1, service.getSectors().length);
        assertTrue(newSectorAdded);
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

    protected void doTearDown() throws Exception {// no op
    }

}

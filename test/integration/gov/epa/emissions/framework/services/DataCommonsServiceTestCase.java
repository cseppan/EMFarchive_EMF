package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public abstract class DataCommonsServiceTestCase extends ServicesTestCase {

    private DataCommonsService service;

    private UserService userService;

    protected void setUpService(DataCommonsService service, UserService userService) throws Exception {
        this.service = service;
        this.userService = userService;
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
        User user = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector lockedSector = service.obtainLockedSector(user, sector);
        assertEquals(lockedSector.getLockOwner(), user.getUsername());

        // Sector object returned directly from the sector table
        Sector sectorLoadedFromDb = currentSector(sector);
        assertEquals(sectorLoadedFromDb.getLockOwner(), user.getUsername());
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
        User user = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector lockedSector = service.obtainLockedSector(user, sector);
        Sector releasedSector = service.releaseLockedSector(lockedSector);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldReleaseDatasetTypeLock() throws EmfException {
        User user = userService.getUser("emf");
        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];

        DatasetType locked = service.obtainLockedDatasetType(user, type);
        DatasetType released = service.releaseLockedDatasetType(user, locked);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldUpdateSector() throws EmfException {
        User user = userService.getUser("emf");

        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];
        String name = sector.getName();

        Sector modifiedSector1 = service.obtainLockedSector(user, sector);
        assertEquals(modifiedSector1.getLockOwner(), user.getUsername());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = service.updateSector(modifiedSector1);
        assertEquals("TEST", modifiedSector2.getName());
        assertEquals(modifiedSector2.getLockOwner(), null);

        // restore
        Sector modifiedSector = service.obtainLockedSector(user, sector);
        modifiedSector.setName(name);
        Sector modifiedSector3 = service.updateSector(modifiedSector);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        User user = userService.getUser("emf");

        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];
        String name = type.getName();

        DatasetType modified1 = service.obtainLockedDatasetType(user, type);
        assertEquals(modified1.getLockOwner(), user.getUsername());
        modified1.setName("TEST");

        DatasetType modified2 = service.updateDatasetType(user, modified1);
        assertEquals("TEST", modified2.getName());
        assertEquals(modified2.getLockOwner(), null);

        // restore
        DatasetType modified = service.obtainLockedDatasetType(user, type);
        modified.setName(name);

        DatasetType modified3 = service.updateDatasetType(user, modified);
        assertEquals(type.getName(), modified3.getName());
    }

    protected void doTearDown() throws Exception {// no op
    }

}

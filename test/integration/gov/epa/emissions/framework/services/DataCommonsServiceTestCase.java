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

        Sector lockedSector = service.getSectorLock(user, sector);
        assertEquals(lockedSector.getUsername(), user.getFullName());

        // Sector object returned directly from the sector table
        Sector sectorLoadedFromDb = currentSector(sector);
        assertEquals(sectorLoadedFromDb.getUsername(), user.getFullName());
    }

    public void testShouldGetLockOnDatasetType() throws EmfException {
        User user = userService.getUser("emf");
        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];

        DatasetType locked = service.getDatasetTypeLock(user, type);
        assertEquals(locked.getUsername(), user.getFullName());

        // Sector object returned directly from the sector table
        DatasetType loadedFromDb = currentDatasetType(type);
        assertEquals(loadedFromDb.getUsername(), user.getFullName());
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User user = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector lockedSector = service.getSectorLock(user, sector);
        Sector releasedSector = service.releaseSectorLock(user, lockedSector);
        assertFalse("Should have released lock", releasedSector.isLocked());

        Sector sectorLoadedFromDb = currentSector(sector);
        assertFalse("Should have released lock", sectorLoadedFromDb.isLocked());
    }

    public void testShouldReleaseDatasetTypeLock() throws EmfException {
        User user = userService.getUser("emf");
        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];

        DatasetType locked = service.getDatasetTypeLock(user, type);
        DatasetType released = service.releaseDatasetTypeLock(user, locked);
        assertFalse("Should have released lock", released.isLocked());

        DatasetType loadedFromDb = currentDatasetType(type);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldUpdateSector() throws EmfException {
        User user = userService.getUser("emf");

        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];
        String name = sector.getName();

        Sector modifiedSector1 = service.getSectorLock(user, sector);
        assertEquals(modifiedSector1.getUsername(), user.getFullName());
        modifiedSector1.setName("TEST");

        Sector modifiedSector2 = service.updateSector(user, modifiedSector1);
        assertEquals("TEST", modifiedSector1.getName());
        assertEquals(modifiedSector2.getUsername(), null);

        // restore
        Sector modifiedSector = service.getSectorLock(user, sector);
        modifiedSector.setName(name);
        Sector modifiedSector3 = service.updateSector(user, modifiedSector);
        assertEquals(sector.getName(), modifiedSector3.getName());
    }

    public void testShouldUpdateDatasetType() throws EmfException {
        User user = userService.getUser("emf");

        DatasetType[] list = service.getDatasetTypes();
        DatasetType type = list[0];
        String name = type.getName();

        DatasetType modified1 = service.getDatasetTypeLock(user, type);
        assertEquals(modified1.getUsername(), user.getFullName());
        modified1.setName("TEST");

        DatasetType modified2 = service.updateDatasetType(user, modified1);
        assertEquals("TEST", modified1.getName());
        assertEquals(modified2.getUsername(), null);

        // restore
        DatasetType modified = service.getDatasetTypeLock(user, type);
        modified.setName(name);

        DatasetType modified3 = service.updateDatasetType(user, modified);
        assertEquals(type.getName(), modified3.getName());
    }

    protected void doTearDown() throws Exception {// no op
    }

}

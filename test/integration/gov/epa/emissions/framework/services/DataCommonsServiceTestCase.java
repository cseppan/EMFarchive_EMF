package gov.epa.emissions.framework.services;

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

    private Sector sectors(long id) throws EmfException {
        Sector[] sectors = service.getSectors();
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].getId() == id)
                return sectors[i];
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
        Sector sectorLoadedFromDb = sectors(sector.getId());
        assertEquals(sectorLoadedFromDb.getUsername(), user.getFullName());
    }

    public void testShouldReleaseSectorLock() throws EmfException {
        User user = userService.getUser("emf");
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];

        Sector modifiedSector = service.releaseSectorLock(user, sector);

        // Sector object returned from the lock call
        assertEquals(modifiedSector.getUsername(), null);

        // Sector object returned directly from the sector table
        Sector modifiedSector2 = sectors(sector.getId());
        assertEquals(modifiedSector2.getUsername(), null);
    }

    public void testShouldUpdateSectorWithLocking() throws EmfException {
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

    protected void doTearDown() throws Exception {// no op
    }

}

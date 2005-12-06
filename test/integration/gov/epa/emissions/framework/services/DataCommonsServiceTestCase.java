package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.PostgresDbUpdate;
import gov.epa.emissions.framework.services.impl.ServicesTestCase;

public abstract class DataCommonsServiceTestCase extends ServicesTestCase {

    private DataCommonsService service;

    protected void setUpService(DataCommonsService service) {
        this.service = service;
    }

    public void testShouldReturnCompleteListOfSectors() throws EmfException {
        Sector[] sectors = service.getSectors();
        assertTrue(sectors.length >= 14);
    }

    public void testShouldAddSector() throws Exception {
        Sector sector = new Sector();
        sector.setName("TEST");

        try {
            service.addSector(sector);
            assertEquals(15, service.getSectors().length);
            sector = find(service.getSectors(), sector.getName());
        } finally {
            PostgresDbUpdate update = new PostgresDbUpdate();
            update.delete("emf.sectors", "id", sector.getId() + "");
        }
    }

    private Sector find(Sector[] sectors, String name) {
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].getName().equals(name))
                return sectors[i];
        }

        return null;
    }

    public void testShouldUpdateSector() throws EmfException {
        Sector[] sectors = service.getSectors();
        Sector sector = sectors[0];
        String name = sector.getName();
        sector.setName("TEST");
        service.updateSector(sector);

        Sector modifiedSector = sectors(sector.getId());
        assertEquals("TEST", modifiedSector.getName());

        // restore
        modifiedSector.setName(name);
        service.updateSector(modifiedSector);
    }

    private Sector sectors(long id) throws EmfException {
        Sector[] sectors = service.getSectors();
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].getId() == id)
                return sectors[i];
        }

        return null;
    }

}

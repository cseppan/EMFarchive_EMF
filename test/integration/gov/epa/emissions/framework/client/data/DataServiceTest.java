package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.db.PostgresDbUpdate;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.WebServicesIntegrationTestCase;

public class DataServiceTest extends WebServicesIntegrationTestCase {

    private DataService services;

    protected void setUp() {
        services = serviceLocator.getDataService();
    }

    public void testShouldReturnCompleteListOfSectors() throws EmfException {
        Sector[] sectors = services.getSectors();
        assertTrue(sectors.length >= 14);
    }

    public void testShouldAddSector() throws Exception {
        Sector sector = new Sector();
        sector.setName("TEST");

        try {
            services.addSector(sector);
            assertEquals(15, services.getSectors().length);
            sector = find(services.getSectors(), sector.getName());
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
        Sector[] sectors = services.getSectors();
        Sector sector = sectors[0];
        String name = sector.getName();
        sector.setName("TEST");
        services.updateSector(sector);

        Sector modifiedSector = sectors(sector.getId());
        assertEquals("TEST", modifiedSector.getName());

        // restore
        modifiedSector.setName(name);
        services.updateSector(modifiedSector);
    }

    private Sector sectors(long id) throws EmfException {
        Sector[] sectors = services.getSectors();
        for (int i = 0; i < sectors.length; i++) {
            if (sectors[i].getId() == id)
                return sectors[i];
        }

        return null;
    }

}

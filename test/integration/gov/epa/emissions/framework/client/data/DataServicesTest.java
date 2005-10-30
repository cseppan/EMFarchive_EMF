package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.db.Config;
import gov.epa.emissions.framework.db.DbUpdate;
import gov.epa.emissions.framework.services.DataServices;
import junit.framework.TestCase;

public class DataServicesTest extends TestCase {

    private DataServices services;

    protected void setUp() {
        String baseUrl = "http://localhost:8080/emf/services";
        ServiceLocator serviceLocator = new RemoteServiceLocator(baseUrl);
        services = serviceLocator.getDataServices();
    }

    public void testShouldReturnCompleteListOfSectors() throws EmfException {
        Sector[] sectors = services.getSectors();
        assertEquals(14, sectors.length);
    }

    public void testShouldAddSector() throws Exception {
        Sector sector = new Sector();
        sector.setName("TEST");

        assertEquals(14, services.getSectors().length);

        try {
            services.addSector(sector);
            assertEquals(15, services.getSectors().length);
            sector = find(services.getSectors(), sector.getName());
        } finally {
            DbUpdate update = new DbUpdate(new Config("test/integration/integration.conf"));
            update.delete("emf.sectors", "id", sector.getId()+"");
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

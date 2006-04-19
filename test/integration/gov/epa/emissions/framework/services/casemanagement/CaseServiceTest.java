package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Arrays;
import java.util.List;

public class CaseServiceTest extends ServiceTestCase {

    private CaseService service;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
        service = new CaseServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAbbreviations() throws Exception {
        int totalBeforeAdd = service.getAbbreviations().length;
        Abbreviation element = new Abbreviation("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getAbbreviations());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAirQualityModels() throws Exception {
        int totalBeforeAdd = service.getAirQualityModels().length;
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getAirQualityModels());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetCaseCategories() throws Exception {
        int totalBeforeAdd = service.getCaseCategories().length;
        CaseCategory element = new CaseCategory("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getCaseCategories());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetEmissionsYears() throws Exception {
        int totalBeforeAdd = service.getEmissionsYears().length;
        EmissionsYear element = new EmissionsYear("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getEmissionsYears());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetGrids() throws Exception {
        int totalBeforeAdd = service.getGrids().length;
        Grid element = new Grid("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getGrids());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetMeteorlogicalYears() throws Exception {
        int totalBeforeAdd = service.getMeteorlogicalYears().length;
        MeteorlogicalYear element = new MeteorlogicalYear("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getMeteorlogicalYears());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetSpeciations() throws Exception {
        int totalBeforeAdd = service.getSpeciations().length;
        Speciation element = new Speciation("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getSpeciations());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetCases() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getCases());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldAddCase() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());
        service.addCase(element);

        try {
            List list = Arrays.asList(service.getCases());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldRemoveCase() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());
        service.addCase(element);
        
        service.removeCase(element);

        List list = Arrays.asList(service.getCases());
        assertEquals(totalBeforeAdd, list.size());
        assertFalse(list.contains(element));
    }

}

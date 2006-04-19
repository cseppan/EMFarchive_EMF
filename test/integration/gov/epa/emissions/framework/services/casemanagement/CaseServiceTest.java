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

    public void testShouldGetAirQualiyModels() throws Exception {
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

}

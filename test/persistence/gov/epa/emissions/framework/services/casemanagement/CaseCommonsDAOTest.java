package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.ServiceTestCase;

import java.util.List;

public class CaseCommonsDAOTest extends ServiceTestCase {

    private CaseCommonsDAO dao;

    protected void doSetUp() throws Exception {
        dao = new CaseCommonsDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldGetAllAbbreviations() {
        int totalBeforeAdd = dao.getAbbreviations(session).size();
        Abbreviation element = new Abbreviation("test" + Math.random());
        add(element);

        try {
            List list = dao.getAbbreviations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistAbbreviationOnAdd() {
        int totalBeforeAdd = dao.getAbbreviations(session).size();
        Abbreviation element = new Abbreviation("test" + Math.random());
        dao.add(element, session);

        try {
            List list = dao.getAbbreviations(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllAirQualityModels() {
        int totalBeforeAdd = dao.getAirQualityModels(session).size();
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        add(element);

        try {
            List list = dao.getAirQualityModels(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistAirQualityModelOnAdd() {
        int totalBeforeAdd = dao.getAirQualityModels(session).size();
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        dao.add(element, session);

        try {
            List list = dao.getAirQualityModels(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllCaseCategories() {
        int totalBeforeAdd = dao.getCaseCategories(session).size();
        CaseCategory element = new CaseCategory("test" + Math.random());
        add(element);

        try {
            List list = dao.getCaseCategories(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseCategoryOnAdd() {
        int totalBeforeAdd = dao.getCaseCategories(session).size();
        CaseCategory element = new CaseCategory("test" + Math.random());
        dao.add(element, session);

        try {
            List list = dao.getCaseCategories(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

}

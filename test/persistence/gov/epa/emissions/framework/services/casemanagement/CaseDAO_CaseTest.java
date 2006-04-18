package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.ServiceTestCase;

import java.util.List;

public class CaseDAO_CaseTest extends ServiceTestCase {

    private CaseDAO dao;

    protected void doSetUp() throws Exception {
        dao = new CaseDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldPersistEmptyCaseOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = new Case("test" + Math.random());
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAllCases() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = new Case("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAbbreviationOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        Abbreviation abbreviation = new Abbreviation("test" + Math.random());
        add(abbreviation);
        element.setAbbreviation(abbreviation);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(abbreviation, ((Case) list.get(0)).getAbbreviation());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAirQualityModelOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        AirQualityModel aqm = new AirQualityModel("test" + Math.random());
        add(aqm);
        element.setAirQualityModel(aqm);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(aqm, ((Case) list.get(0)).getAirQualityModel());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithCaseCategoryOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        CaseCategory attrib = new CaseCategory("test" + Math.random());
        add(attrib);
        element.setCaseCategory(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(attrib, ((Case) list.get(0)).getCaseCategory());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithEmissionsYearOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        EmissionsYear attrib = new EmissionsYear("test" + Math.random());
        add(attrib);
        element.setEmissionsYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(attrib, ((Case) list.get(0)).getEmissionsYear());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithGridOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        Grid attrib = new Grid("test" + Math.random());
        add(attrib);
        element.setGrid(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(attrib, ((Case) list.get(0)).getGrid());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithMeteorlogicalYearOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        MeteorlogicalYear attrib = new MeteorlogicalYear("test" + Math.random());
        add(attrib);
        element.setMeteorlogicalYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(attrib, ((Case) list.get(0)).getMeteorlogicalYear());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithSpeciationOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        Speciation attrib = new Speciation("test" + Math.random());
        add(attrib);
        element.setSpeciation(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertEquals(attrib, ((Case) list.get(0)).getSpeciation());
        } finally {
            remove(element);
        }
    }
}

package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

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

    public void testShouldRemoveCaseOnRemove() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = new Case("test" + Math.random());
        dao.add(element, session);

        session.clear();
        dao.remove(element, session);
        List list = dao.getCases(session);
        assertEquals(totalBeforeAdd, list.size());
    }

    public void testShouldGetAllCases() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = newCase();

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithPrimitiveAttributesOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        element.setDescription("desc");
        element.setRunStatus("started");
        element.setLastModifiedDate(new Date());
        element.setCopiedFrom("another dataset");
        
        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);

            Case added = (Case) list.get(totalBeforeAdd);
            assertEquals(element.getDescription(), added.getDescription());
            assertEquals(element.getRunStatus(), added.getRunStatus());
            assertEquals(element.getLastModifiedDate(), added.getLastModifiedDate());
            assertEquals(element.getCopiedFrom(), added.getCopiedFrom());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAbbreviationOnAdd() {
        Case element = new Case("test" + Math.random());
        Abbreviation abbreviation = new Abbreviation("test" + Math.random());
        add(abbreviation);
        element.setAbbreviation(abbreviation);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(abbreviation, ((Case) list.get(0)).getAbbreviation());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAirQualityModelOnAdd() {
        Case element = new Case("test" + Math.random());
        AirQualityModel aqm = new AirQualityModel("test" + Math.random());
        add(aqm);
        element.setAirQualityModel(aqm);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(aqm, ((Case) list.get(0)).getAirQualityModel());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithCaseCategoryOnAdd() {
        Case element = new Case("test" + Math.random());
        CaseCategory attrib = new CaseCategory("test" + Math.random());
        add(attrib);
        element.setCaseCategory(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getCaseCategory());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithEmissionsYearOnAdd() {
        Case element = new Case("test" + Math.random());
        EmissionsYear attrib = new EmissionsYear("test" + Math.random());
        add(attrib);
        element.setEmissionsYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getEmissionsYear());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithGridOnAdd() {
        Case element = new Case("test" + Math.random());
        Grid attrib = new Grid("test" + Math.random());
        add(attrib);
        element.setGrid(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getGrid());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithMeteorlogicalYearOnAdd() {
        Case element = new Case("test" + Math.random());
        MeteorlogicalYear attrib = new MeteorlogicalYear("test" + Math.random());
        add(attrib);
        element.setMeteorlogicalYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getMeteorlogicalYear());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithSpeciationOnAdd() {
        Case element = new Case("test" + Math.random());
        Speciation attrib = new Speciation("test" + Math.random());
        add(attrib);
        element.setSpeciation(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getSpeciation());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithCreatorOnAdd() {
        Case element = new Case("test" + Math.random());
        UserDAO userDAO = new UserDAO();
        User creator = userDAO.get("emf", session);
        element.setCreator(creator);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(creator, ((Case) list.get(0)).getCreator());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithProjectOnAdd() {
        Case element = new Case("test" + Math.random());
        Project attrib = new Project("test" + Math.random());
        add(attrib);
        element.setProject(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getProject());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithRegionOnAdd() {
        Case element = new Case("test" + Math.random());
        Region attrib = new Region("test" + Math.random());
        add(attrib);
        element.setRegion(attrib);
        
        dao.add(element, session);
        
        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getRegion());
        } finally {
            remove(element);
        }
    }

    public void testShouldObtainLockedCaseForUpdate() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            Case locked = dao.obtainLocked(owner, element, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            Case loadedFromDb = load(element);
            assertEquals(owner.getUsername(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldReleaseLock() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            Case locked = dao.obtainLocked(owner, element, session);
            Case released = dao.releaseLocked(locked, session);
            assertFalse("Should have released lock", released.isLocked());

            Case loadedFromDb = load(element);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            dao.obtainLocked(owner, element, session);

            User user = userDAO.get("admin", session);
            Case result = dao.obtainLocked(user, element, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateCaseOnUpdate() throws EmfException {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = newCase();

        session.clear();
        try {
            Case locked = dao.obtainLocked(owner, element, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            Case modified = dao.update(locked, session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(element);
        }
    }

    private Case newCase() {
        Case element = new Case("test" + Math.random());
        add(element);

        return element;
    }

    private Case load(Case dataset) {
        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Case.class).add(Restrictions.eq("name", dataset.getName()));
            tx.commit();

            return (Case) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}

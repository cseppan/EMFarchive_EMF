package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseServiceTest extends ServiceTestCase {

    private CaseService service;

    private UserServiceImpl userService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        service = new CaseServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
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

    public void testShouldGetGridResolutions() throws Exception {
        int totalBeforeAdd = service.getGridResolutions().length;
        GridResolution element = new GridResolution("test" + Math.random());
        add(element);
        
        try {
            List list = Arrays.asList(service.getGridResolutions());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetCaseInputs() throws Exception {
        int totalBeforeAdd = service.getCaseInputs().length;
        CaseInput caseinput = new CaseInput("test one");
        Case element = new Case("test" + Math.random());
        element.setCaseInputs(new CaseInput[]{caseinput});
        add(element);
        
        try {
            List list = Arrays.asList(service.getCaseInputs());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(caseinput));
        } finally {
            remove(element);
            remove(caseinput);
        }
    }
    
    public void testShouldGetInputNames() throws Exception {
        int totalBeforeAdd = service.getInputNames().length;
        
        CaseInput caseinput = new CaseInput("input one");
        InputName inputname = new InputName("input name one");
        Case element = new Case("test" + Math.random());
        
        caseinput.setInputName(inputname);
        element.setCaseInputs(new CaseInput[]{caseinput});
        add(element);
        
        try {
            List list = Arrays.asList(service.getInputNames());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(inputname));
        } finally {
            remove(element);
            remove(caseinput);
            remove(inputname);
        }
    }
    
    public void testShouldGetInputEnvtVars() throws Exception {
        int totalBeforeAdd = service.getInputEnvtVars().length;
        
        CaseInput caseinput = new CaseInput("input one");
        InputEnvtVar envtVar = new InputEnvtVar("envt var one");
        Case element = new Case("test" + Math.random());
        
        caseinput.setEnvtVars(envtVar);
        element.setCaseInputs(new CaseInput[]{caseinput});
        add(element);
        
        try {
            List list = Arrays.asList(service.getInputEnvtVars());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(envtVar));
        } finally {
            remove(element);
            remove(caseinput);
            remove(envtVar);
        }
    }
 
    public void testShouldGetPrograms() throws Exception {
        int totalBeforeAdd = service.getPrograms().length;
        
        CaseInput caseinput = new CaseInput("input one");
        Program program = new Program("input name one");
        Case element = new Case("test" + Math.random());
        
        caseinput.setProgram(program);
        element.setCaseInputs(new CaseInput[]{caseinput});
        add(element);
        
        try {
            List list = Arrays.asList(service.getPrograms());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(program));
        } finally {
            remove(element);
            remove(caseinput);
            remove(program);
        }
    }

    public void testShouldGetCases() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = newCase();
        
        try {
            List list = Arrays.asList(service.getCases());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    private Case newCase() {
        Case element = new Case("test" + Math.random());
        add(element);
        return element;
    }

    public void testShouldAddCase() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());
        
        CaseInput input1 = new CaseInput("input one");
        CaseInput input2 = new CaseInput("input two");
        CaseInput input3 = new CaseInput("input three");
        element.setCaseInputs(new CaseInput[]{input1, input2, input3}); 
        service.addCase(element);

        try {
            List list = Arrays.asList(service.getCases());
            Case retrieved = (Case)list.get(totalBeforeAdd);
            
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
            assertEquals("input one", retrieved.getCaseInputs()[0].getName());
            assertEquals("input two", retrieved.getCaseInputs()[1].getName());
            assertEquals("input three", retrieved.getCaseInputs()[2].getName());
        } finally {
            remove(element);
            remove(input1);
            remove(input2);
            remove(input3);
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

    public void testShouldObtainLockedCase() throws EmfException {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            Case loadedFromDb = load(element);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldReleaseLockedCase() throws EmfException {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            Case released = service.releaseLocked(locked);
            assertFalse("Should have released lock", released.isLocked());

            Case loadedFromDb = load(element);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateCase() throws Exception {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            locked.setName("TEST");
            locked.setDescription("TEST case");

            Case released = service.updateCase(locked);
            assertEquals("TEST",released.getName());
            assertEquals("TEST case", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(element);
        }
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

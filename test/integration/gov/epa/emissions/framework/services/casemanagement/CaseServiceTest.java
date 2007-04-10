package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
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
    
    private HibernateSessionFactory sessionFactory;
    
    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new CaseServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {
       service = null;
       userService = null;
       sessionFactory = null;
       System.gc();
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

    public void testShouldGetModelToRuns() throws Exception {
        int totalBeforeAdd = service.getModelToRuns().length;
        ModelToRun element = new ModelToRun("test" + Math.random());
        add(element);
        
        try {
            List list = Arrays.asList(service.getModelToRuns());
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

    public void testShouldGetInputNames() throws Exception {
        int totalBeforeAdd = service.getInputNames().length;
        
        InputName element = new InputName("input name one" + Math.random());
        add(element);
        
        try {
            List list = Arrays.asList(service.getInputNames());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }
    
    public void testShouldGetInputEnvtVars() throws Exception {
        int totalBeforeAdd = service.getInputEnvtVars().length;
        InputEnvtVar envtVar = new InputEnvtVar("envt var one" + Math.random());
        add(envtVar);
        
        try {
            List list = Arrays.asList(service.getInputEnvtVars());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(envtVar));
        } finally {
            remove(envtVar);
        }
    }
 
    public void testShouldGetPrograms() throws Exception {
        int totalBeforeAdd = service.getPrograms().length;
        CaseProgram program = new CaseProgram("input name one" + Math.random());
        add(program);
        
        try {
            List list = Arrays.asList(service.getPrograms());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(program));
        } finally {
            remove(program);
        }
    }

    public void testShouldGetSubdirs() throws Exception {
        int totalBeforeAdd = service.getSubDirs().length;
        SubDir subdir = new SubDir("subdir name one" + Math.random());
        add(subdir);
        
        try {
            List list = Arrays.asList(service.getSubDirs());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(subdir));
        } finally {
            remove(subdir);
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
    
    public void testShouldCopyACaseWithCaseInputs() throws Exception {
        Case element = newCase();
        Case copied = null;
        CaseInput cpdInput1 = null;
        CaseInput cpdInput2 = null;
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program" );
        Sector sector1 = new Sector("" , "test sector one");
        Sector sector2 = new Sector("", "test sector two");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();
        
        add(inputname);
        add(program);
        add(sector1);
        add(sector2);
        inputOne.setInputName(inputname);
        inputOne.setProgram(program);
        inputOne.setSector(sector1);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(program);
        inputTwo.setSector(sector2);
        
        try {
            int caseId = load(element).getId();
            inputOne.setCaseID(caseId);
            inputTwo.setCaseID(caseId);
            add(inputOne);
            add(inputTwo);
            
            copied = service.copyCaseObject(new int[] {caseId})[0];
            CaseInput[] copiedInputs = service.getCaseInputs(copied.getId());
            cpdInput1 = copiedInputs[0];
            cpdInput2 = copiedInputs[1];
            assertTrue("Copied case name should begin with [Copy of]", copied.getName().startsWith("Copy of"));
            assertEquals(2, copiedInputs.length);
            assertEquals("test input name", cpdInput1.getName());
            assertEquals("test input name", cpdInput2.getName());
            assertEquals("test case program", cpdInput1.getProgram().getName());
            assertEquals("test case program", cpdInput2.getProgram().getName());
            assertEquals("test sector one", cpdInput1.getSector().getName());
            assertEquals("test sector two", cpdInput2.getSector().getName());
        } finally {
            remove(inputOne);
            remove(inputTwo);
            remove(cpdInput1);
            remove(cpdInput2);
            remove(inputname);
            remove(program);
            remove(sector1);
            remove(sector2);
            remove(element);
            remove(copied);
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

    private Case load(Case caseObj) {
        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Case.class).add(Restrictions.eq("name", caseObj.getName()));
            tx.commit();

            return (Case) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}

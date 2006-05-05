package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataServiceTest extends ServiceTestCase {

    private DataServiceImpl service;

    private UserService userService;

    private DataCommonsServiceImpl dataCommonsService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        service = new DataServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        dataCommonsService = new DataCommonsServiceImpl(sessionFactory);
        deleteAllDatasets();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAllDatasets() throws EmfException {
        EmfDataset[] datasets = service.getDatasets();
        assertEquals(0, datasets.length);

        EmfDataset dataset = newDataset();

        try {
            EmfDataset[] postInsert = service.getDatasets();
            assertEquals(1, postInsert.length);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            EmfDataset loadedFromDb = load(dataset);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldReleaseLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            EmfDataset released = service.releaseLockedDataset(locked);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = load(dataset);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldUpdateDataset() throws Exception {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setName("TEST");
            locked.setDescription("TEST dataset");

            EmfDataset released = service.updateDataset(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST dataset", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(dataset);
        }
    }

    private EmfDataset newDataset() throws EmfException {
        EmfDataset dataset = new EmfDataset();

        User owner = userService.getUser("emf");

        dataset.setName("dataset-dao-test" + Math.abs(new Random().nextInt()));
        dataset.setCreator(owner.getUsername());

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return load(dataset);
    }

    private EmfDataset load(EmfDataset dataset) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", dataset.getName()));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void testShouldAddMultipleSectorsFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        Sector[] allSectors = dataCommonsService.getSectors();

        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.addSector(allSectors[0]);
            locked.addSector(allSectors[1]);
            EmfDataset released = service.updateDataset(locked);
            Sector[] sectorsFromDataset = released.getSectors();

            assertEquals(2, sectorsFromDataset.length);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldAddCountryFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        Country country = new Country("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            dataCommonsService.addCountry(country);
            locked.setCountry(country);

            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getCountry().getName());
        } finally {
            remove(dataset);
            remove(country);
        }
    }

    public void testShouldAddProjectFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        Project project = new Project("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            dataCommonsService.addProject(project);
            locked.setProject(project);

            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getProject().getName());
        } finally {
            remove(dataset);
            remove(project);
        }
    }

    public void testShouldAddRegionFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        Region region = new Region("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setRegion(region);
            dataCommonsService.addRegion(region);
            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getRegion().getName());
        } finally {
            remove(dataset);
            remove(region);
        }
    }

    public void testShouldUpdateDatsetName() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setName("TEST dataset");

            EmfDataset released = service.updateDataset(locked);
            assertEquals("TEST dataset", released.getName());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldFailOnAttemptToUpdateDatasetWithDuplicateName() throws EmfException {
        EmfDataset dataset1 = newDataset();

        EmfDataset dataset2 = newDataset();
        try {
            dataset2.setName(dataset1.getName());
            service.updateDataset(dataset2);
        } catch (EmfException e) {
            assertEquals("Dataset name already in use", e.getMessage());
            return;
        } finally {
            remove(dataset1);
        }
    }

}

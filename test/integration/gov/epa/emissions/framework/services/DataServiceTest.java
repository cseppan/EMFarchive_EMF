package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.impl.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.impl.DataServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.UserServiceImpl;

import java.util.Date;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataServiceTest extends ServicesTestCase {

    private DataService service;

    private UserService userService;

    private DataCommonsServiceImpl dataCommonsService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory();
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

    public void xtestShouldUpdateDataset() throws Exception {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setDescription("TEST dataset");

            EmfDataset released = service.updateDataset(locked);
            assertEquals("TEST dataset", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        finally {
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

    public void xtestShouldAddMultipleSectorsFromWithinDataset() throws EmfException {
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

    public void xtestShouldAddCountryFromWithinDataset() throws EmfException {
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

    public void xtestShouldAddProjectFromWithinDataset() throws EmfException {
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

    public void xtestShouldAddRegionFromWithinDataset() throws EmfException {
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

  public void testShouldFailOnAttemptToAddDatasetWithDuplicateName() throws EmfException {
        String newname = "MyDataset" + Math.abs(new Random().nextInt());
        EmfDataset dataset1 = new EmfDataset();
        EmfDataset dataset2 = new EmfDataset();

        User creator = userService.getUser("emf");

        dataset1 = new EmfDataset();
        dataset1.setName(newname);
        dataset1.setAccessedDateTime(new Date());
        dataset1.setCreatedDateTime(new Date());
        dataset1.setCreator(creator.getUsername());
        dataset1.setDescription("DESCRIPTION");
        dataset1.setModifiedDateTime(new Date());
        dataset1.setStartDateTime(new Date());
        dataset1.setStatus("imported");
        dataset1.setYear(42);
        dataset1.setUnits("orl");
        dataset1.setTemporalResolution("t1");
        dataset1.setStopDateTime(new Date());

        service.addDataset(dataset1);

        dataset2 = new EmfDataset();
        dataset2.setName(newname);
        dataset2.setAccessedDateTime(new Date());
        dataset2.setCreatedDateTime(new Date());
        dataset2.setCreator(creator.getUsername());
        dataset2.setDescription("DESCRIPTION");
        dataset2.setModifiedDateTime(new Date());
        dataset2.setStartDateTime(new Date());
        dataset2.setStatus("imported");
        dataset2.setYear(42);
        dataset2.setUnits("orl");
        dataset2.setTemporalResolution("t1");
        dataset2.setStopDateTime(new Date());

        try {
            service.addDataset(dataset2);
        } catch (EmfException e) {
            assertEquals("Dataset name already in use", e.getMessage());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            remove(dataset1);
        }
    }

  public void testShouldFailOnAttemptToUpdateDatasetWithDuplicateName() throws EmfException {
      String newname = "MyDataset" + Math.abs(new Random().nextInt());
      EmfDataset dataset1 = new EmfDataset();
      EmfDataset dataset2 = new EmfDataset();

      User creator = userService.getUser("emf");

      dataset1 = new EmfDataset();
      dataset1.setName(newname);
      dataset1.setAccessedDateTime(new Date());
      dataset1.setCreatedDateTime(new Date());
      dataset1.setCreator(creator.getUsername());
      dataset1.setDescription("DESCRIPTION");
      dataset1.setModifiedDateTime(new Date());
      dataset1.setStartDateTime(new Date());
      dataset1.setStatus("imported");
      dataset1.setYear(42);
      dataset1.setUnits("orl");
      dataset1.setTemporalResolution("t1");
      dataset1.setStopDateTime(new Date());

      service.addDataset(dataset1);

      dataset2 = new EmfDataset();
      dataset2.setName(newname+"foobar");
      dataset2.setAccessedDateTime(new Date());
      dataset2.setCreatedDateTime(new Date());
      dataset2.setCreator(creator.getUsername());
      dataset2.setDescription("DESCRIPTION");
      dataset2.setModifiedDateTime(new Date());
      dataset2.setStartDateTime(new Date());
      dataset2.setStatus("imported");
      dataset2.setYear(42);
      dataset2.setUnits("orl");
      dataset2.setTemporalResolution("t1");
      dataset2.setStopDateTime(new Date());
      service.addDataset(dataset2);

      try {
          dataset2.setName(newname);
          service.updateDataset(dataset2);
      } catch (EmfException e) {
          assertEquals("Dataset name already in use", e.getMessage());
          return;
      } catch (Exception e) {
          e.printStackTrace();
      } finally {

          remove(dataset1);
      }
  }

}

package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseServiceTest extends ServiceTestCase {

    private CaseServiceImpl service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new CaseServiceImpl(sessionFactory, dbServerFactory);
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
        // create a new case and set the name and export top dir
        Case element = new Case("test" + Math.random());
        element.setInputFileDir("/home/azubrow/smoke_emf_training/2002/smoke");

        // adds the element to the db and then reloads it from the db
        // ensures that it has an id
        add(element);
        return (Case) load(Case.class, element.getName());
    }

    // private CaseJob newCaseJob(Case caseObj) {
    // // adds the element to the db and then reloads it from the db
    // // ensures that it has an id
    // CaseJob element = new CaseJob("test" + Math.random());
    // element.setCaseId(caseObj.getId());
    // add(element);
    // return load(element);
    // }

    private CaseInput loadCaseInput(CaseInput input) {
        /**
         * Gets case input data from the db, requires 4 cols to uniquely identify: caseID, inputname, sector, and
         * program
         */
        CaseDAO DAO = new CaseDAO();
        session.clear();
        return (CaseInput) DAO.loadCaseInput(input, session);
    }

    private CaseJob loadNewCaseJob(CaseJob job, Case caseObj) {
        /**
         * Takes a new job that and adds case ID and adds to db
         */
        // adds the element to the db and then reloads it from the db
        // ensures that it has an id
        job.setCaseId(caseObj.getId());
        add(job);
        return (CaseJob) load(CaseJob.class, job.getName());
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

            Case loadedFromDb = (Case) load(Case.class, element.getName());// object returned directly from the table
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

            Case loadedFromDb = (Case) load(Case.class, element.getName());
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
        CaseProgram program = new CaseProgram("test case program");
        Sector sector1 = new Sector("", "test sector one");
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
            int caseId = ((Case) load(Case.class, element.getName())).getId();
            inputOne.setCaseID(caseId);
            inputTwo.setCaseID(caseId);
            add(inputOne);
            add(inputTwo);

            copied = service.copyCaseObject(new int[] { caseId })[0];
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

    public void testCaseRunJob() throws Exception {
        /**
         * Tests the Case run job on the server side
         */
        
        Case caseObj = newCase();
        CaseJob job = new CaseJob();
        EmfDataset[] datasets = null;
        CaseInput[] inputs = null;
        InputName[] inNames = null;
//        Sector sector = null;
        CaseProgram program = null;
        Executable execVal = null;
        SubDir subDirObj = null;
        
        try {
            // Create a new user, case, job, and executable
            User user = userService.getUser("emf");
            
            // Create a new executable and synch w/ db
            execVal = new Executable();
            execVal.setName("smk_onroad_test.csh");
            add(execVal);
            execVal = (Executable) load(Executable.class, execVal.getName());
            
            // Set job name, executable names and path and add to job
            job.setName("job_test1");
            job.setPath("/home/azubrow/tmp");
//            job.setExecutable(new Executable[] { execVal });
            job.setExecutable(execVal);

            // Create EMF datasets -- metadata only
            datasets = loadMetaDatasets();

            // create case inputs and input names for each dataset
            inputs = new CaseInput[datasets.length];
            inNames = new InputName[datasets.length];

            // clears stale tables from db
            session.clear();
           

            // new sub dir and synch w/ db
            subDirObj = new SubDir();
            subDirObj.setName("ge_dat/v3");
            add(subDirObj);
            subDirObj = (SubDir) load(SubDir.class, subDirObj.getName());
           
            // Need new sector and program for input
//            sector = new Sector("", "test sector one");
//            add(sector);
//            sector = (Sector) load(Sector.class, sector.getName());
            program = new CaseProgram("test case program2");
            add(program);
            program = (CaseProgram) load(CaseProgram.class, program.getName());

            // loop over datasets and assign each to an input
            for (int i = 0; i < datasets.length; i++) {
                inputs[i] = new CaseInput();
                inNames[i] = new InputName();
                // Need to setup input name before setting associating
                // case inputs w/ this name
                // alittle messy, but need input names already in db
                // making input names derived from dataset type
                // should check for duplication
                inNames[i].setName(datasets[i].getDatasetTypeName() + "test");
                add(inNames[i]);
                inNames[i] = (InputName) load(InputName.class, inNames[i].getName());

                // populate case input
                inputs[i].setInputName(inNames[i]);
                inputs[i].setDataset(datasets[i]);
                inputs[i].setVersion(newVersion(datasets[i]));
                inputs[i].setDatasetType(datasets[i].getDatasetType());
                inputs[i].setProgram(program);
                inputs[i].setSector(datasets[i].getSectors()[0]);
                inputs[i].setSubdirObj(subDirObj);

                // associate the input to a particular case
                inputs[i].setCaseID(caseObj.getId());

                // clears stale tables from db --
                // need to make sure inputname already in table ??
                // session.clear();

                // add and reload the input so we're synched w/ db
                add(inputs[i]);
                inputs[i] = loadCaseInput(inputs[i]);

            }
            // update everything to db
            job = loadNewCaseJob(job, caseObj);

            service.submitJob(job, user,caseObj);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());

        } finally {
            /*
             * Clean up db-
             * Order matters, clean up tables that reference
             * other tables first.
             */
            for (CaseInput input : inputs) {
                Version tmpVersion = input.getVersion();
                remove(input);
                remove(tmpVersion);
            }
            remove(program);
            remove(subDirObj);
            for (InputName inName : inNames) {
                remove(inName);
            }

            for (EmfDataset dataset : datasets) {
                remove(dataset);
            }
//            remove(sector);
            remove(job);
            remove(execVal);
            remove(caseObj);

        }
    }

    private EmfDataset[] loadMetaDatasets() throws EmfException {
        /**
         * This loads some test datasets into the EMF-test db
         * 
         * Provides only metadata for the run job testing
         * 
         */

        // Dataset array for returns
        EmfDataset[] datasets = new EmfDataset[2];

        // sectors
        Sector sectorOR = (Sector) load(Sector.class, "On Road");

        // Create metadata for new dataset of specific type
        datasets[0] = newDataset("mbinv_onroad", "ORL Onroad Inventory (MBINV)" );
        datasets[0].setSectors(new Sector[] {sectorOR});

        // Create metadata for new dataset of specific type
        datasets[1] = newDataset("mcodes", "Mobile Source Codes (Line-based)");
        datasets[1].setSectors(new Sector[] {sectorOR});

        return datasets;
    }

    private EmfDataset newDataset(String name, String type) throws EmfException {
        /**
         * Creating and loading a dataset to the EMF-test
         */
        EmfDataset dataset = new EmfDataset();

        User owner = userService.getUser("emf");

        // setup dataset object
        dataset.setName(name);
        dataset.setCreator(owner.getUsername());
        DatasetType dataType = getDatasetType(type);
        dataset.setDatasetType(dataType);

        // Add keyvals and keywords
        Keyword keyword1 = (Keyword) load(Keyword.class, "EXPORT_SUFFIX");
        Keyword keyword2 = (Keyword) load(Keyword.class, "EXPORT_PREFIX");
        KeyVal keyval1 = new KeyVal();
        keyval1.setKeyword(keyword1);
        keyval1.setValue(".txt");
        KeyVal keyval2 = new KeyVal();
        keyval2.setKeyword(keyword2);
        keyval2.setValue("");
        dataset.addKeyVal(keyval1);
        dataset.addKeyVal(keyval2);

        // commit object to db
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

    private Version newVersion(EmfDataset dataset) {
        /**
         * Associates dataset id w/ a new version adds version to db and returns new version
         */
        // Create version db
        Version version = new Version();
        version.setDatasetId(dataset.getId());
        version.setVersion(0);
        version.setPath("");
        version.setFinalVersion(true);
        Date lastModifiedDate = new Date(); // initialized to now
        version.setLastModifiedDate(lastModifiedDate);

        version.setName("test_version" + Math.random());
        add(version);
        return (version);
        // return (Version) load(Version.class, version.getName());

    }

    private DatasetType getDatasetType(String type) {
        /**
         * Compares string type to all datasetTypes in db returns the appropriate dataset type
         * 
         */
        Transaction tx = null;
        try {
            // Start hibernate session, get DatasetType names from db
            tx = session.beginTransaction();
            List<DatasetType> list = session.createCriteria(DatasetType.class).list();
            tx.commit();

            // Search list for specific type
            for (DatasetType elem : list) {
                if (elem.getName().equals(type)) {
                    return elem;
                }
            }

        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        // if we've gotten here, no type found
        return null;
    }

    // private Dataset[] loadDataExamples() throws Exception {
    // /**
    // * Loads the data into datasets for job and case examples
    // */
    // // setting up server info
    // DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
    // SqlDataTypes sqlDataTypes = localDbServer.getSqlDataTypes();
    //
    // // Create a list for future dataset
    // List datasetLst = new ArrayList();
    //        
    // // create a particular dataset ORL
    // Dataset dataset = new SimpleDataset();
    // dataset.setName("testOnroadOrl");
    // dataset.setId(Math.abs(new Random().nextInt()));
    //        
    // // Setup version
    // Version version = new Version();
    // version.setVersion(0);
    //
    // // loads the data
    // File file = new File("test/data/orl/nc", "small-onroad.txt");
    // Importer orlImporter = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
    // localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
    // // VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer,
    // lastModifiedDate(file.getParentFile(),file.getName()));
    // VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, new Date());
    // importer.run();
    //
    // // add new dataset to the list
    // datasetLst.add(dataset);
    //
    // }

    public void testShouldUpdateCase() throws Exception {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            locked.setName("TEST");
            locked.setDescription("TEST case");

            Case released = service.updateCase(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST case", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(element);
        }
    }

    public Object load(Class clazz, String className) {
        /**
         * loads the abstract class through hibernate from db need to pass class name (type) and recast the return
         */

        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", className));
            tx.commit();

            return crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // private CaseJob load(CaseJob caseJobObj) {
    // Transaction tx = null;
    //
    // session.clear();
    // try {
    // tx = session.beginTransaction();
    // Criteria crit = session.createCriteria(CaseJob.class).add(Restrictions.eq("name", caseJobObj.getName()));
    // tx.commit();
    //
    // return (CaseJob) crit.uniqueResult();
    // } catch (HibernateException e) {
    // tx.rollback();
    // throw e;
    // }
    // }

}

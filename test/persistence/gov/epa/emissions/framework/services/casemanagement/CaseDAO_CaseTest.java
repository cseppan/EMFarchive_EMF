package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.IntendedUse;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseDAO_CaseTest extends ServiceTestCase {

    private CaseDAO dao;

    private UserServiceImpl userService;

    private CaseService service;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        dao = new CaseDAO();
        service = new CaseServiceImpl(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
    }

    protected void doTearDown() {// no op
    }

    public void testShouldPersistEmptyCaseOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        add(element);

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
        add(element);

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
        element.setTemplateUsed("another dataset");

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);

            Case added = (Case) list.get(totalBeforeAdd);
            assertEquals(element.getDescription(), added.getDescription());
            assertEquals(element.getRunStatus(), added.getRunStatus());
            assertEquals(element.getLastModifiedDate(), added.getLastModifiedDate());
            assertEquals(element.getTemplateUsed(), added.getTemplateUsed());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAbbreviationOnAdd() {
        Case element = new Case("test" + Math.random());
        Abbreviation abbreviation = new Abbreviation("test" + Math.random());
        add(abbreviation);
        List abbrs = dao.getAbbreviations(session);
        element.setAbbreviation((Abbreviation) abbrs.get(abbrs.size() - 1));

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(abbreviation, ((Case) list.get(list.size()-1)).getAbbreviation());
        } finally {
            remove(element);
            remove(abbreviation);
        }
    }

    public void testShouldPersistCaseWithAnAirQualityModelOnAdd() {
        Case element = new Case("test" + Math.random());
        AirQualityModel aqm = new AirQualityModel("test" + Math.random());
        add(aqm);
        element.setAirQualityModel(aqm);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(aqm, ((Case) list.get(0)).getAirQualityModel());
        } finally {
            remove(element);
            remove(aqm);
        }
    }

    public void testShouldPersistCaseWithAnCaseInputAndASubdirOnAdd() {
        Case element = new Case("test" + Math.random());
        SubDir subdir = new SubDir("sub/dir" + Math.random());
        add(subdir);
        CaseInput input = new CaseInput();
        input.setSubdirObj(subdir);
        add(element);
        session.clear();

        try {
            List list = dao.getCases(session);
            Case loadedCase = (Case) list.get(list.size() - 1);
            input.setCaseID(loadedCase.getId());
            add(input);
            session.clear();

            List inputs = dao.getCaseInputs(loadedCase.getId(), session);
            assertEquals(input, inputs.get(inputs.size() - 1));
            assertEquals(subdir, ((CaseInput) inputs.get(inputs.size() - 1)).getSubdirObj());
            assertEquals(1, inputs.size());
        } finally {
            remove(input);
            remove(subdir);
            remove(element);
        }
    }

    public void testShouldThrowExceptionWhenAnCaseInputHasSameSectorProgramInputname() {
        Case element = new Case("test" + Math.random());
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program");
        Sector sector = new Sector("", "test sector");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();

        add(inputname);
        add(program);
        add(sector);
        add(element);
        inputOne.setInputName(inputname);
        inputOne.setProgram(program);
        inputOne.setSector(sector);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(program);
        inputTwo.setSector(sector);

        session.clear();

        try {
            List list = dao.getCases(session);
            Case loadedCase = (Case) list.get(0);
            inputOne.setCaseID(loadedCase.getId());
            inputTwo.setCaseID(loadedCase.getId());
            add(inputOne);
            add(inputTwo);
        } catch (Exception e) {
            String exceptionMsg = "could not insert:";
            assertTrue("Should throw exception", e.getMessage().startsWith(exceptionMsg));
        } finally {
            remove(inputOne);
            remove(inputname);
            remove(program);
            remove(sector);
            remove(element);
        }
    }

    public void testShouldPersistCaseWithCaseCategoryOnAdd() {
        Case element = new Case("test" + Math.random());
        CaseCategory attrib = new CaseCategory("test" + Math.random());
        add(attrib);
        element.setCaseCategory(attrib);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getCaseCategory());
        } finally {
            remove(element);
            remove(attrib);
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
            remove(attrib);
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
            remove(attrib);
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
            remove(attrib);
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
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithCreatorOnAdd() {
        Case element = new Case("test" + Math.random());
        UserDAO userDAO = new UserDAO();
        User creator = userDAO.get("emf", session);
        element.setLastModifiedBy(creator);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(creator, ((Case) list.get(0)).getLastModifiedBy());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithProjectOnAdd() {
        Case element = new Case("test" + Math.random());
        Project attrib = new Project("test" + Math.random());
        add(attrib);
        element.setProject(attrib);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getProject());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithRegionOnAdd() {
        Case element = new Case("test" + Math.random());
        Region attrib = new Region("test" + Math.random());
        add(attrib);
        element.setControlRegion(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getControlRegion());
        } finally {
            remove(element);
            remove(attrib);
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

    public void testShouldDeepCopyACaseWithAllAttributesFilled() throws Exception {
        String caseToCopyName = "test" + Math.random();
        Case toCopy = new Case(caseToCopyName);

        Abbreviation abbr = new Abbreviation();
        abbr.setName("test" + Math.random());
        add(abbr);

        ModelToRun model2Run = new ModelToRun();
        model2Run.setName("test" + Math.random());
        add(model2Run);

        AirQualityModel airModel = new AirQualityModel();
        airModel.setName("test" + Math.random());
        add(airModel);

        CaseCategory cat = new CaseCategory();
        cat.setName("test" + Math.random());
        add(cat);

        EmissionsYear emisYear = new EmissionsYear();
        emisYear.setName("test" + Math.random());
        add(emisYear);

        Grid grid = new Grid();
        grid.setName("test" + Math.random());
        add(grid);

        MeteorlogicalYear metYear = new MeteorlogicalYear();
        metYear.setName("test" + Math.random());
        add(metYear);

        Speciation spec = new Speciation();
        spec.setName("test" + Math.random());
        add(spec);

        Project proj = new Project();
        proj.setName("test" + Math.random());
        add(proj);

        Mutex lock = new Mutex();
        lock.setLockDate(new Date());
        lock.setLockOwner("emf");

        Region modRegion = new Region();
        modRegion.setName("test" + Math.random());
        add(modRegion);

        Region contrlRegion = new Region();
        contrlRegion.setName("test" + Math.random());
        add(contrlRegion);

        User owner = userService.getUser("emf");

        GridResolution gridResltn = new GridResolution();
        gridResltn.setName("test" + Math.random());
        add(gridResltn);

        SectorCriteria crit = new SectorCriteria();
        crit.setCriteria("new rule");
        crit.setType("new type");

        List<SectorCriteria> criteriaList = new ArrayList<SectorCriteria>();
        criteriaList.add(crit);

        SectorCriteria[] criteriaArray = new SectorCriteria[] { crit };

        Sector dssector = new Sector();
        dssector.setDescription("description");
        dssector.setLockDate(new Date());
        dssector.setLockOwner("emf");
        dssector.setName("test" + Math.random());
        dssector.setSectorCriteria(criteriaList);
        dssector.setSectorCriteria(criteriaArray);
        add(dssector);

        Sector casesector = new Sector();
        casesector.setDescription("description");
        casesector.setLockDate(new Date());
        casesector.setLockOwner("emf");
        casesector.setName("test" + Math.random());
        casesector.setSectorCriteria(criteriaList);
        casesector.setSectorCriteria(criteriaArray);
        add(casesector);

        IntendedUse use = new IntendedUse();
        use.setName("test" + Math.random());
        add(use);

        Country country = new Country();
        country.setName("test" + Math.random());
        add(country);

        Keyword keyword = new Keyword();
        keyword.setName("test" + Math.random());
        add(keyword);

        KeyVal keyval = new KeyVal();
        keyval.setKeyword(keyword);
        keyval.setListindex(0);
        keyval.setValue("test string");

        QAProgram qaprog = new QAProgram();
        qaprog.setName("test" + Math.random());
        qaprog.setRunClassName("run class name");
        add(qaprog);

        QAStepTemplate qatemplate = new QAStepTemplate();
        qatemplate.setDescription("description");
        qatemplate.setListIndex(0);
        qatemplate.setName("test" + Math.random());
        qatemplate.setOrder(0);
        qatemplate.setProgram(qaprog);
        qatemplate.setProgramArguments("args");
        qatemplate.setRequired(true);

        DatasetType dstype = new DatasetType();
        dstype.setDefaultSortOrder("new order");
        dstype.setDescription("description");
        dstype.setExporterClassName("sdf.sadf.name");
        dstype.setExternal(false);
        dstype.setImporterClassName("importer.class.name");
        dstype.setKeyVals(new KeyVal[] { keyval });
        dstype.setLockDate(new Date());
        dstype.setLockOwner("emf");
        dstype.setMaxFiles(1);
        dstype.setMinFiles(1);
        dstype.setName("test type");
        dstype.setQaStepTemplates(new QAStepTemplate[] { qatemplate });
        add(dstype);

        InternalSource internalSrc = new InternalSource();
        internalSrc.setCols(new String[] { "col one" });
        internalSrc.setColsList("col list");
        internalSrc.setListindex(0);
        internalSrc.setSource("source");
        internalSrc.setSourceSize(123);
        internalSrc.setTable("table");
        internalSrc.setType("new type");

        ExternalSource externalSrc = new ExternalSource();
        externalSrc.setDatasource("data source");
        externalSrc.setListindex(0);

        EmfDataset dataset = new EmfDataset();
        dataset.setAccessedDateTime(new Date());
        dataset.setCountry(country);
        dataset.setCreatedDateTime(new Date());
        dataset.setCreator("emf");
        dataset.setDatasetType(dstype);
        dataset.setDefaultVersion(0);
        dataset.setDescription("description");
        dataset.setExternalSources(new ExternalSource[] { externalSrc });
        dataset.setIntendedUse(use);
        dataset.setInternalSources(new InternalSource[] { internalSrc });
        dataset.setKeyVals(new KeyVal[] { keyval });
        dataset.setLockDate(new Date());
        dataset.setLockOwner("emf");
        dataset.setModifiedDateTime(new Date());
        dataset.setName("test" + Math.random());
        dataset.setProject(proj);
        dataset.setRegion(modRegion);
        dataset.setSectors(new Sector[] { dssector });
        dataset.setStartDateTime(new Date());
        dataset.setStatus("copied");
        dataset.setStopDateTime(new Date());
        dataset.setSummarySource(internalSrc);
        dataset.setTemporalResolution("resolutation");
        dataset.setUnits("unit ton");
        dataset.setYear(1999);
        add(dataset);

        InputEnvtVar envtVar = new InputEnvtVar();
        envtVar.setName("test" + Math.random());
        add(envtVar);

        Version version = new Version();
        version.setCreator(owner);
        version.setDatasetId(0);
        version.setFinalVersion(true);
        version.setLastModifiedDate(new Date());
        version.setLockDate(new Date());
        version.setLockOwner("emf");
        version.setName("test" + Math.random());
        version.setPath("path|path|path");
        version.setVersion(0);
        add(version);

        InputName inputName = new InputName();
        inputName.setName("test" + Math.random());
        add(inputName);

        CaseProgram caseProg = new CaseProgram();
        caseProg.setName("test" + Math.random());
        add(caseProg);

        toCopy.setAbbreviation(abbr);
        toCopy.setAirQualityModel(airModel);
        toCopy.setBaseYear(1999);
        toCopy.setCaseCategory(cat);
        toCopy.setCaseTemplate(true);
        toCopy.setControlRegion(contrlRegion);
        toCopy.setCreator(owner);
        toCopy.setDescription("Description");
        toCopy.setEmissionsYear(emisYear);
        toCopy.setEndDate(new Date());
        toCopy.setFutureYear(2005);
        toCopy.setGrid(grid);
        toCopy.setGridDescription("grid description");
        toCopy.setGridResolution(gridResltn);
        toCopy.setInputFileDir("input/file/dir");
        toCopy.setIsFinal(true);
        toCopy.setLastModifiedBy(owner);
        toCopy.setLastModifiedDate(new Date());
        toCopy.setLock(lock);
        toCopy.setLockDate(new Date());
        toCopy.setLockOwner("emf");
        toCopy.setMeteorlogicalYear(metYear);
        toCopy.setModel(model2Run);
        toCopy.setModelingRegion(modRegion);
        toCopy.setNumEmissionsLayers(1);
        toCopy.setNumMetLayers(1);
        toCopy.setOutputFileDir("output/file/dir");
        toCopy.setProject(proj);
        toCopy.setRunStatus("copy");
        toCopy.setSectors(new Sector[] { casesector });
        toCopy.setSpeciation(spec);
        toCopy.setStartDate(new Date());
        toCopy.setTemplateUsed("orig");
        add(toCopy);

        CaseInput input = new CaseInput();
        input.setDataset(dataset);
        input.setDatasetType(dstype);
        input.setEnvtVars(envtVar);
        input.setInputName(inputName);
        input.setProgram(caseProg);
        input.setRequired(true);
        input.setSector(casesector);
        input.setShow(true);
        input.setVersion(version);
        input.setCaseID(toCopy.getId());
        add(input);

        Case loaded = load(toCopy);

        Case coppied = null;

        try {
            coppied = service.copyCaseObject(new int[] { loaded.getId() })[0];
            assertTrue(coppied.getName().startsWith("Copy of " + caseToCopyName));
            assertTrue(coppied.getIsFinal());
        } finally {
            CaseInput[] cpInputs = service.getCaseInputs(coppied.getId());
            remove(input);
            remove(cpInputs[cpInputs.length - 1]);
            remove(toCopy);
            if (coppied != null)
                remove(coppied);
            remove(casesector);
            remove(abbr);
            remove(model2Run);
            remove(airModel);
            remove(cat);
            remove(emisYear);
            remove(grid);
            remove(metYear);
            remove(spec);
            remove(gridResltn);
            remove(internalSrc);
            remove(externalSrc);
            remove(keyval);
            remove(qatemplate);
            remove(dataset);
            remove(country);
            remove(dssector);
            remove(modRegion);
            remove(contrlRegion);
            remove(use);
            remove(proj);
            remove(dstype);
            remove(qaprog);
            remove(keyword);
            remove(envtVar);
            remove(version);
            remove(inputName);
            remove(caseProg);
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

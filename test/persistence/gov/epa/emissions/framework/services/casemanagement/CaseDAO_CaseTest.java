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
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.IntendedUse;

import java.util.ArrayList;
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
        element.setTemplateUsed("another dataset");

        dao.add(element, session);

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
        session.clear();
        element.setAbbreviation(abbreviation);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(abbreviation, ((Case) list.get(0)).getAbbreviation());
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

        dao.add(element, session);

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
        element.setCaseInputs(new CaseInput[]{input});
        
        dao.add(element, session);
        
        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(input, ((Case) list.get(0)).getCaseInputs()[0]);
            assertEquals(1, ((Case) list.get(0)).getCaseInputs().length);
        } finally {
            remove(element);
            remove(subdir);
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

        dao.add(element, session);

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
        Case toCopy = new Case("test" + Math.random());
        Abbreviation abbr = new Abbreviation();
        abbr.setId(0);
        abbr.setName("test" + Math.random());

        ModelToRun model2Run = new ModelToRun();
        model2Run.setId(0);
        model2Run.setName("test" + Math.random());

        AirQualityModel airModel = new AirQualityModel();
        airModel.setId(0);
        airModel.setName("test" + Math.random());

        CaseCategory cat = new CaseCategory();
        cat.setId(0);
        cat.setName("test" + Math.random());

        EmissionsYear emisYear = new EmissionsYear();
        emisYear.setId(0);
        emisYear.setName("test" + Math.random());

        Grid grid = new Grid();
        grid.setId(0);
        grid.setName("test" + Math.random());

        MeteorlogicalYear metYear = new MeteorlogicalYear();
        metYear.setId(0);
        metYear.setName("test" + Math.random());

        Speciation spec = new Speciation();
        spec.setId(0);
        spec.setName("test" + Math.random());

        Project proj = new Project();
        proj.setId(0);
        proj.setName("test" + Math.random());

        Mutex lock = new Mutex();
        lock.setLockDate(new Date());
        lock.setLockOwner("emf");

        Region modRegion = new Region();
        modRegion.setId(0);
        modRegion.setName("test" + Math.random());

        Region contrlRegion = new Region();
        contrlRegion.setId(0);
        contrlRegion.setName("test" + Math.random());

        User owner = new User();
        owner.setAccountDisabled(false);
        owner.setAdmin(true);
        owner.setAffiliation("test");
        owner.setEmail("abc@test.com");
        owner.setEncryptedPassword("ja;sdfklj12");
        owner.setId(0);
        owner.setLockDate(new Date());
        owner.setLockOwner("emf");
        owner.setName("emf");
        owner.setPassword("sadfasdf12");
        owner.setPhone("999-123-4567");
        owner.setUsername("emf");

        User modBy = new User();
        modBy.setAccountDisabled(false);
        modBy.setAdmin(true);
        modBy.setAffiliation("test");
        modBy.setEmail("abc@test.com");
        modBy.setEncryptedPassword("ja;sdfklj12");
        modBy.setId(0);
        modBy.setLockDate(new Date());
        modBy.setLockOwner("emf");
        modBy.setName("emf");
        modBy.setPassword("sadfasdf12");
        modBy.setPhone("999-123-4567");
        modBy.setUsername("emf");

        GridResolution gridResltn = new GridResolution();
        gridResltn.setId(0);
        gridResltn.setName("test" + Math.random());

        SectorCriteria crit = new SectorCriteria();
        crit.setCriteria("new rule");
        crit.setId(0);
        crit.setType("new type");

        List criteriaList = new ArrayList();
        criteriaList.add(crit);

        SectorCriteria[] criteriaArray = new SectorCriteria[] { crit };

        Sector sector = new Sector();
        sector.setDescription("description");
        sector.setId(0);
        sector.setLockDate(new Date());
        sector.setLockOwner("emf");
        sector.setName("test" + Math.random());
        sector.setSectorCriteria(criteriaList);
        sector.setSectorCriteria(criteriaArray);

        IntendedUse use = new IntendedUse();
        use.setId(0);
        use.setName("test" + Math.random());

        Country country = new Country();
        country.setId(0);
        country.setName("test" + Math.random());

        Keyword keyword = new Keyword();
        keyword.setId(0);
        keyword.setName("test" + Math.random());

        KeyVal keyval = new KeyVal();
        keyval.setId(1);
        keyval.setKeyword(keyword);
        keyval.setListindex(0);
        keyval.setValue("test string");

        QAProgram qaprog = new QAProgram();
        qaprog.setId(0);
        qaprog.setName("test" + Math.random());
        qaprog.setRunClassName("run class name");

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
        dstype.setId(0);
        dstype.setImporterClassName("importer.class.name");
        dstype.setKeyVals(new KeyVal[] { keyval });
        dstype.setLockDate(new Date());
        dstype.setLockOwner("emf");
        dstype.setMaxFiles(1);
        dstype.setMinFiles(1);
        dstype.setName("test type");
        dstype.setQaStepTemplates(new QAStepTemplate[] { qatemplate });

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
        dataset.setCreator("emf123");
        dataset.setDatasetType(dstype);
        dataset.setDefaultVersion(0);
        dataset.setDescription("description");
        dataset.setExternalSources(new ExternalSource[] { externalSrc });
        dataset.setId(0);
        dataset.setIntendedUse(use);
        dataset.setInternalSources(new InternalSource[] { internalSrc });
        dataset.setKeyVals(new KeyVal[] { keyval });
        dataset.setLockDate(new Date());
        dataset.setLockOwner("emf");
        dataset.setModifiedDateTime(new Date());
        dataset.setName("test" + Math.random());
        dataset.setProject(proj);
        dataset.setRegion(modRegion);
        dataset.setSectors(new Sector[] { sector });
        dataset.setStartDateTime(new Date());
        dataset.setStatus("copied");
        dataset.setStopDateTime(new Date());
        dataset.setSummarySource(internalSrc);
        dataset.setTemporalResolution("resolutation");
        dataset.setUnits("unit ton");
        dataset.setYear(1999);

        InputEnvtVar envtVar = new InputEnvtVar();
        envtVar.setId(0);
        envtVar.setName("test" + Math.random());

        Version version = new Version();
        version.setCreator(owner);
        version.setDatasetId(0);
        version.setFinalVersion(true);
        version.setId(0);
        version.setLastModifiedDate(new Date());
        version.setLockDate(new Date());
        version.setLockOwner("emf");
        version.setName("test" + Math.random());
        version.setPath("path|path|path");
        version.setVersion(0);

        InputName inputName = new InputName();
        inputName.setId(0);
        inputName.setName("test" + Math.random());

        CaseProgram caseProg = new CaseProgram();
        caseProg.setId(0);
        caseProg.setName("test" + Math.random());

        CaseInput input = new CaseInput();
        input.setDataset(dataset);
        input.setDatasetType(dstype);
        input.setEnvtVars(envtVar);
        input.setInputName(inputName);
        input.setProgram(caseProg);
        input.setRecordID(0);
        input.setRequired(true);
        input.setSector(sector);
        input.setShow(true);
        input.setSubdir("sub dir");
        input.setVersion(version);

        toCopy.setAbbreviation(abbr);
        toCopy.setAirQualityModel(airModel);
        toCopy.setBaseYear(1999);
        toCopy.setCaseCategory(cat);
        toCopy.setCaseInputs(new CaseInput[] { input });
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
        toCopy.setId(0);
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
        toCopy.setName("test to be copied");
        toCopy.setNumEmissionsLayers(1);
        toCopy.setNumMetLayers(1);
        toCopy.setOutputFileDir("output/file/dir");
        toCopy.setProject(proj);
        toCopy.setRunStatus("copy");
        toCopy.setSectors(new Sector[] { sector });
        toCopy.setSpeciation(spec);
        toCopy.setStartDate(new Date());
        toCopy.setTemplateUsed("orig");

        Case coppied = (Case) DeepCopy.copy(toCopy);
        assertEquals("test to be copied", coppied.getName());
        assertTrue(coppied.getIsFinal());
        assertEquals("emf123", coppied.getCaseInputs()[0].getDataset().getCreator());
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

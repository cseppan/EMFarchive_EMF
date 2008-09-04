package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CaseAssistanceService {
    private static Log log = LogFactory.getLog(ManagedCaseService.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private CaseDAO caseDao;

    private DataCommonsDAO dataDao;

    private HibernateSessionFactory sessionFactory;

    public CaseAssistanceService(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.caseDao = new CaseDAO();
        this.dataDao = new DataCommonsDAO();

        if (DebugLevels.DEBUG_9)
            System.out.println("In CaseAssistanceService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        myTag();

        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + myTag());

        log.info("CaseAssistanceService");
        log.info("Session factory null? " + (sessionFactory == null));
    }

    public synchronized void importCase(String folder, String[] files, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        CaseDaoHelper helper = new CaseDaoHelper(sessionFactory, caseDao, dataDao);
        String[][] cases = sortCaseFiles(getFiles(folder, files));

        Case newCase = null;

        try {
            for (int i = 0; i < cases.length; i++) {
                CaseFileParser caseParser = new CaseFileParser(cases[i][0], cases[i][1], cases[i][2]);
                newCase = caseParser.getCase();

                if (newCase == null)
                    throw new EmfException("Case not properly parsed.");

                if (newCase.getName() == null || newCase.getName().trim().isEmpty())
                    throw new EmfException("Case name not specified.");

                if (newCase.getModel() == null || newCase.getModel().getName().isEmpty())
                    throw new EmfException("Case run model not specified.");

                Case existedCase = caseDao.getCaseFromName(newCase.getName(), session);

                if (existedCase != null)
                    throw new EmfException("Case (" + newCase.getName()
                            + ") to import has a duplicate name in cases table.");

                session.clear();
                resetCaseValues(user, newCase, session);
                session.clear(); // NOTE: to clear up the old object images
                session.flush();
                addNewCaseObject(newCase);

                session.clear(); // NOTE: to clear up the old object images
                Case loadedCase = caseDao.getCaseFromName(newCase.getName(), session);
                insertParameters(loadedCase.getId(), loadedCase.getModel().getId(), caseParser.getParameters(), helper);
                insertInputs(loadedCase.getId(), loadedCase.getModel().getId(), caseParser.getInputs(), helper);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not inmport case", e);

            if (e instanceof EmfException)
                throw (EmfException) e;

            Throwable ex = e.getCause();

            if (ex != null)
                throw new EmfException(ex.getMessage());

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    private void addNewCaseObject(Case newCase) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            caseDao.add(newCase, session);
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
    }

    private String[] getFiles(String folder, String[] files) {
        String[] fullPathFiles = new String[files.length];

        for (int i = 0; i < files.length; i++)
            fullPathFiles[i] = folder + File.separator + files[i];

        return fullPathFiles;
    }

    private String[][] sortCaseFiles(String[] files) throws EmfException {
        int len = files.length;
        int numCases = len / 3;

        if (len % 3 != 0)
            throw new EmfException("Incomplete files for importing cases.");

        String[][] caseFiles = new String[numCases][3];

        // NOTE: needs to expand to support multiple cases import
        for (int i = 0; i < numCases; i++) {
            caseFiles[i][0] = getCorrectFile(files, "Summary");
            caseFiles[i][1] = getCorrectFile(files, "Input");
            caseFiles[i][2] = getCorrectFile(files, "Job");
        }

        return caseFiles;
    }

    private String getCorrectFile(String[] files, String fileType) {
        for (String file : files) {
            if (file.endsWith("_Summary_Parameters.csv") && fileType.equals("Summary"))
                return file;

            if (file.endsWith("_Inputs.csv") && fileType.equals("Input"))
                return file;

            if (file.endsWith("_Jobs.csv") && fileType.equals("Job"))
                return file;
        }

        return null;
    }

    private void resetCaseValues(User user, Case newCase, Session session) {
        Abbreviation abbr = newCase.getAbbreviation();

        if (abbr == null)
            abbr = new Abbreviation(newCase.getName());

        loadNSetObject(newCase, abbr, Abbreviation.class, abbr.getName(), session);

        CaseCategory cat = newCase.getCaseCategory();
        loadNSetObject(newCase, cat, CaseCategory.class, cat == null ? "" : cat.getName(), session);

        Project proj = newCase.getProject();
        loadNSetObject(newCase, proj, Project.class, proj == null ? "" : proj.getName(), session);

        ModelToRun model = newCase.getModel();
        loadNSetObject(newCase, model, ModelToRun.class, model == null ? "" : model.getName(), session);

        Region modRegion = newCase.getModelingRegion();
        loadNSetObject(newCase, modRegion, Region.class, modRegion == null ? "" : modRegion.getName(), session);

        Grid grid = newCase.getGrid();
        loadNSetObject(newCase, grid, Grid.class, grid == null ? "" : grid.getName(), session);

        GridResolution gResltn = newCase.getGridResolution();
        loadNSetObject(newCase, gResltn, GridResolution.class, gResltn == null ? "" : gResltn.getName(), session);

        AirQualityModel airMod = newCase.getAirQualityModel();
        loadNSetObject(newCase, airMod, AirQualityModel.class, airMod == null ? "" : airMod.getName(), session);

        Speciation spec = newCase.getSpeciation();
        loadNSetObject(newCase, spec, Speciation.class, spec == null ? "" : spec.getName(), session);

        MeteorlogicalYear metYear = newCase.getMeteorlogicalYear();
        loadNSetObject(newCase, metYear, MeteorlogicalYear.class, metYear == null ? "" : metYear.getName(), session);

        newCase.setLastModifiedBy(user);
        newCase.setLastModifiedDate(new Date());
        newCase.setCreator(user);
    }

    private synchronized void loadNSetObject(Case newCase, Object obj, Class<?> clazz, String name, Session session) {
        Object temp = null;

        if (obj != null && name != null && !name.isEmpty())
            temp = checkDB(obj, clazz, name, session);

        if (obj instanceof Abbreviation) {
            newCase.setAbbreviation((Abbreviation) temp);
            return;
        }

        if (obj instanceof CaseCategory) {
            newCase.setCaseCategory((CaseCategory) temp);
            return;
        }

        if (obj instanceof Project) {
            newCase.setProject((Project) temp);
            return;
        }

        if (obj instanceof ModelToRun) {
            newCase.setModel((ModelToRun) temp);
            return;
        }

        if (obj instanceof Region) {
            newCase.setModelingRegion((Region) temp);
            return;
        }

        if (obj instanceof Grid) {
            newCase.setGrid((Grid) temp);
            return;
        }

        if (obj instanceof GridResolution) {
            newCase.setGridResolution((GridResolution) temp);
            return;
        }

        if (obj instanceof AirQualityModel) {
            newCase.setAirQualityModel((AirQualityModel) temp);
            return;
        }

        if (obj instanceof Speciation) {
            newCase.setSpeciation((Speciation) temp);
            return;
        }

        if (obj instanceof MeteorlogicalYear) {
            newCase.setMeteorlogicalYear((MeteorlogicalYear) temp);
            return;
        }
    }

    private Object checkDB(Object obj, Class<?> clazz, String name, Session session) {
        session.clear();
        session.flush();
        Object temp = caseDao.load(clazz, name, session);

        if (temp != null && temp instanceof Abbreviation) {
            String random = Math.random() + "";
            String uniqueName = name + "_" + random.substring(2);
            ((Abbreviation) obj).setName(uniqueName);
            session.clear();
            session.flush();
            caseDao.addObject(obj, session);
            session.clear();
            session.flush();
            temp = caseDao.load(clazz, uniqueName, session);
        }

        if (temp == null) {
            session.clear();
            session.flush();
            caseDao.addObject(obj, session);
            session.clear();
            session.flush();
            temp = caseDao.load(clazz, name, session);
        }

        return temp;
    }

    private void insertParameters(int caseId, int model2RunId, List<CaseParameter> parameters, CaseDaoHelper helper)
            throws Exception {
        for (Iterator<CaseParameter> iter = parameters.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            param.setCaseID(caseId);

            ParameterName name = param.getParameterName();
            name.setModelToRunId(model2RunId);
            name = helper.getParameterName(name);
            param.setParameterName(name);

            ParameterEnvVar envVar = param.getEnvVar();
            envVar.setModelToRunId(model2RunId);
            envVar = helper.getParameterEnvVar(envVar);
            param.setEnvVar(envVar);

            ValueType type = param.getType();
            type = helper.getValueType(type);
            param.setType(type);

            Sector sector = param.getSector();
            sector = helper.getSector(sector);
            param.setSector(sector);

            CaseProgram prog = param.getProgram();
            prog.setModelToRunId(model2RunId);
            prog = helper.getCaseProgram(prog);
            param.setProgram(prog);

            helper.insertCaseParameter(param);
        }

    }

    private void insertInputs(int caseId, int model2RunId, List<CaseInput> inputs, CaseDaoHelper helper)
            throws Exception {
        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            input.setCaseID(caseId);

            InputName name = input.getInputName();
            name.setModelToRunId(model2RunId);
            name = helper.getInputName(name);
            input.setInputName(name);

            InputEnvtVar envVar = input.getEnvtVars();
            envVar.setModelToRunId(model2RunId);
            envVar = helper.getInputEnvVar(envVar);
            input.setEnvtVars(envVar);

            Sector sector = input.getSector();
            sector = helper.getSector(sector);
            input.setSector(sector);

            CaseProgram prog = input.getProgram();
            prog.setModelToRunId(model2RunId);
            prog = helper.getCaseProgram(prog);
            input.setProgram(prog);

            DatasetType type = input.getDatasetType();
            type = helper.getDatasetType(type);
            input.setDatasetType(type);

            SubDir subDir = input.getSubdirObj();
            subDir.setModelToRunId(model2RunId);
            subDir = helper.getSubDir(subDir);
            input.setSubdirObj(subDir);

            // NOTE: temporarily set to null to make input insertion to work
            input.setDataset(null);
            input.setVersion(null);

            helper.insertCaseInput(input);
        }

    }
}

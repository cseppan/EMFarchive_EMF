package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.generic.GenericExporterToString;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.exim.ManagedExportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.CaseJobSumitter;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ManagedCaseService {
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

    private CaseDAO dao;

    private HibernateSessionFactory sessionFactory = null;

    private DbServer dbServer = null;

    private User user;

    private String eolString = System.getProperty("line.separator");

    // these run fields are used to create the run script
    private String runShell = "#!/bin/csh"; // shell to run under

    private String runSet = "setenv"; // how to set a variable

    private String runEq = " "; // equate a variable (could be a space)

    private String runTerminator = ""; // line terminator

    private String runComment = "##"; // line comment

    private String runSuffix = ".csh"; // job run file suffix

    // all sectors and all jobs id in the case inputs tb
    private final Sector ALL_SECTORS = null;

    private final int ALL_JOB_ID = 0;

    protected Session session = null;

//    private Session getSession() {
//        if (session == null) {
//            session = sessionFactory.getSession();
//        }
//        return session;
//    }

    public ManagedCaseService(DbServer dbServer, HibernateSessionFactory sessionFactory) {
        this.dbServer = dbServer;
        this.sessionFactory = sessionFactory;
        this.dao = new CaseDAO(sessionFactory);

        myTag();
        if (DebugLevels.DEBUG_9)
            System.out.println("In ManagedCaseService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        if (DebugLevels.DEBUG_1)
            System.out.println(">>>> " + myTag());

        log.info("ManagedCaseService");
        // log.info("exportTaskSubmitter: " + caseJobSubmitter);
        log.info("Session factory null? " + (sessionFactory == null));
        log.info("dBServer null? " + (this.dbServer == null));
        log.info("User null? : " + (user == null));

    }

    /**
     * Generate the unique job key
     * 
     */
    private synchronized String createJobKey(int jobId) {
        return jobId + "_" + new Date().getTime();
    }

    private synchronized ManagedExportService getExportService() {
        log.info("ManagedCaseService::getExportService");
        ManagedExportService exportService = null;

        if (exportService == null) {
            try {
                exportService = new ManagedExportService(dbServer, sessionFactory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return exportService;
    }

    // ***************************************************************************

    public Case[] getCases() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cases = dao.getCases(session);

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            session.close();
        }
    }

    public Case getCase(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case caseObj = dao.getCase(caseId, session);
            return caseObj;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            session.close();
        }
    }

    public CaseJob getCaseJob(int jobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job for job id " + jobId + ".\n" + e.getMessage());
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
        } finally {
            session.close();
        }
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List abbreviations = dao.getAbbreviations(session);

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        } finally {
            session.close();
        }
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List airQualityModels = dao.getAirQualityModels(session);
            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        } finally {
            session.close();
        }
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getCaseCategories(session);
            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        } finally {
            session.close();
        }
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getEmissionsYears(session);
            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        } finally {
            session.close();
        }
    }

    public Grid[] getGrids() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getGrids(session);
            return (Grid[]) results.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        } finally {
            session.close();
        }
    }

    public GridResolution[] getGridResolutions() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getGridResolutions(session);
            return (GridResolution[]) results.toArray(new GridResolution[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Grid Resolutions", e);
            throw new EmfException("Could not get all Grid Resolutions");
        } finally {
            session.close();
        }
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getMeteorlogicalYears(session);
            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Meteorological Years", e);
            throw new EmfException("Could not get all Meteorological Years");
        } finally {
            session.close();
        }
    }

    public Speciation[] getSpeciations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSpeciations(session);
            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        } finally {
            session.close();
        }
    }

    public synchronized void addCase(User user, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            dao.update(locked, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized void removeCase(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            setStatus(caseObj.getLastModifiedBy(), "Started removing case " + caseObj.getName() + ".", "Remove Case");
            List<CaseInput> inputs = dao.getCaseInputs(caseObj.getId(), session);
            dao.removeCaseInputs(inputs.toArray(new CaseInput[0]), session);

            List<CaseJob> jobs = dao.getCaseJobs(caseObj.getId(), session);
            dao.removeCaseJobs(jobs.toArray(new CaseJob[0]), session);

            List<CaseParameter> parameters = dao.getCaseParameters(caseObj.getId(), session);
            dao.removeCaseParameters(parameters.toArray(new CaseParameter[0]), session);

            dao.remove(caseObj, session);
            setStatus(caseObj.getLastModifiedBy(), "Finished removing case " + caseObj.getName() + ".", "Remove Case");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not remove Case: " + caseObj, e);
            throw new EmfException("Could not remove Case: " + caseObj);
        } finally {
            session.close();
        }
    }

    private synchronized void setStatus(User user, String message, String type) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType(type);
        status.setMessage(message);
        status.setTimestamp(new Date());
        StatusDAO statusDao = new StatusDAO(sessionFactory);
        statusDao.add(status);
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case locked = dao.obtainLocked(owner, element, session);
            return locked;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public Case releaseLocked(Case locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case released = dao.releaseLocked(locked, session);
            return released;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not release lock by " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock by " + locked.getLockOwner() + " for Case: " + locked);
        } finally {
            session.close();
        }
    }

    public synchronized Case updateCase(Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            Case released = dao.update(element, session);
            return released;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update Case", e);
            throw new EmfException("Could not update Case: " + element + "; " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public CaseInput getCaseInput(int inputId) throws EmfException {

        Session session = sessionFactory.getSession();
        try {

            CaseInput input = dao.getCaseInput(inputId, session);
            return input;
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            session.close();
        }

    }

    public InputName[] getInputNames() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputNames(session);
            return (InputName[]) results.toArray(new InputName[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            session.close();
        }
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputEnvtVars(session);
            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Input Environment Variables", e);
            throw new EmfException("Could not get all Input Environment Variables");
        } finally {
            session.close();
        }
    }

    public CaseProgram[] getPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getPrograms(session);
            return (CaseProgram[]) results.toArray(new CaseProgram[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        } finally {
            session.close();
        }
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getModelToRuns(session);
            return (ModelToRun[]) results.toArray(new ModelToRun[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all Models To Run", e);
            throw new EmfException("Could not get all Models To Run");
        } finally {
            session.close();
        }
    }

    private Version[] getInputDatasetVersions(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        List<Version> list = new ArrayList<Version>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());

        return list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getDataset());

        return list.toArray(new EmfDataset[0]);
    }

    private boolean checkExternalDSType(DatasetType type) {
        String name = type.getName();

        return name.indexOf("External") >= 0;
    }

    private SubDir[] getSubdirs(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        SubDir[] subdirs = new SubDir[inputs.length];

        for (int i = 0; i < inputs.length; i++)
            subdirs[i] = inputs[i].getSubdirObj();

        return subdirs;
    }

    public synchronized InputName addCaseInputName(InputName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(name, session);
            return (InputName) dao.load(InputName.class, name.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case input name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(program, session);
            return (CaseProgram) dao.load(CaseProgram.class, program.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new program '" + program.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new program '" + program.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(inputEnvtVar, session);
            return (InputEnvtVar) dao.load(InputEnvtVar.class, inputEnvtVar.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new input environment variable '" + inputEnvtVar.getName() + "'\n"
                    + e.getMessage());
            throw new EmfException("Could not add new input environment variable '" + inputEnvtVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(model, session);
            return (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new model to run '" + model.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(gridResolution, session);
            return (GridResolution) dao.load(GridResolution.class, gridResolution.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new Grid Resolution '" + gridResolution.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new Grid Resolution '" + gridResolution.getName() + "'");
        } finally {
            session.close();
        }
    }

    public SubDir[] getSubDirs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSubDirs(session);
            return (SubDir[]) results.toArray(new SubDir[0]);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not get all subdirectories", e);
            throw new EmfException("Could not get all subdirectories");
        } finally {
            session.close();
        }
    }

    public synchronized SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(subdir, session);
            return (SubDir) dao.load(SubDir.class, subdir.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new subdirectory '" + subdir.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new subdirectory '" + subdir.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized CaseInput addCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        if (dao.caseInputExists(input, session))
            throw new EmfException("The combination of 'Input Name', 'Sector', 'Program', and 'Job' should be unique.");

        try {
            dao.add(input, session);
            return (CaseInput) dao.loadCaseInput(input, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case input '" + input.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input '" + input.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseInput loaded = (CaseInput) dao.loadCaseInput(input, session);

            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed (" + loaded.getId() + "," + input.getId()
                        + ")");

            // Clear the cached information. To update a case
            // FIXME: Verify the session.clear()
            session.clear();
            dao.updateCaseInput(input, session);
            // setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case input: " + input.getName() + ".\n" + e);
            throw new EmfException("Could not update case input: " + input.getName() + ".");
        } finally {
            session.close();
        }
    }

    public void removeCaseInputs(CaseInput[] inputs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseInputs(inputs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case input " + inputs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case input " + inputs[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    /**
     * Gets all the inputs for this job, selects based on: case ID, job ID, and sector
     */
    private List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector) throws EmfException {
        List<CaseInput> outInputs = new ArrayList<CaseInput>();

        Session session = sessionFactory.getSession();
        EmfDataset cipDataset = null;
        String badCipName = null;

        // select the inputs based on 3 criteria
        try {
            List<CaseInput> inputs = dao.getJobInputs(caseId, jobId, sector, session);
            if (DebugLevels.DEBUG_9)
                System.out.println("Are inputs null?" + (inputs == null));
            Iterator<CaseInput> iter = inputs.iterator();

            while (iter.hasNext()) {

                CaseInput cip = iter.next();
                badCipName = cip.getName();
//                if (DebugLevels.DEBUG_9)
//                    System.out.println(cip.getCaseID());
                cipDataset = cip.getDataset();

                if (cipDataset == null) {

                    if (cip.isRequired()) {
                        if (DebugLevels.DEBUG_9)
                            System.out.println("CIP DATASET IS NULL AND IS REQD FOR CIP INPUT " + cip.getName());
                        badCipName = cip.getName();
                        // emf exception
                        throw new EmfException("Required dataset not set for CaseInput= " + badCipName);
                    }
                } else {
                    outInputs.add(cip);
                }
            }

            return outInputs;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            // throw new EmfException("Required dataset not set for Case Input name = " + badCipName);
            throw new EmfException(e.getMessage());

        } finally {
            session.close();
        }
    }

    private List<CaseInput> getAllJobInputs(CaseJob job) throws EmfException {
        /**
         * Gets all the inputs for a specific job
         */
        int caseId = job.getCaseId();
        int jobId = job.getId();

        /*
         * Need to get the inputs for 4 different scenarios: All sectors, all jobs Specific sector, all jobs All
         * sectors, specific job specific sector, specific job
         */
        List<CaseInput> inputsAA = null; // inputs for all sectors and all jobs
        List<CaseInput> inputsSA = null; // inputs for specific sector and all jobs
        List<CaseInput> inputsAJ = null; // inputs for all sectors specific jobs
        List<CaseInput> inputsSJ = null; // inputs for specific sectors specific jobs
        List<CaseInput> inputsAll = new ArrayList(); // all inputs
        try {

            // Get case inputs (the datasets associated w/ the case)
            // All sectors, all jobs
            inputsAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS);

            // Sector specific, all jobs
            Sector sector = job.getSector();
            if (sector != this.ALL_SECTORS) {
                inputsSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector);
            }

            // All sectors, job specific
            inputsAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS);

            // Specific sector and specific job
            if (sector != this.ALL_SECTORS) {
                inputsSJ = this.getJobInputs(caseId, jobId, sector);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            // throw new EmfException("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId +
            // ").\n");
            throw new EmfException(e.getMessage());
        }

        // append all the job inputs to the inputsAll list
        if ((inputsAA != null) && (inputsAA.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of AA inputs = " + inputsAA.size());
            inputsAll.addAll(inputsAA);
        }
        if ((inputsSA != null) && (inputsSA.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of SA inputs = " + inputsSA.size());
            inputsAll.addAll(inputsSA);
        }
        if ((inputsAJ != null) && (inputsAJ.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of AJ inputs = " + inputsAJ.size());
            inputsAll.addAll(inputsAJ);
        }
        if ((inputsSJ != null) && (inputsSJ.size() > 0)) {
            if (DebugLevels.DEBUG_0)
                System.out.println("Number of SJ inputs = " + inputsSJ.size());
            inputsAll.addAll(inputsSJ);
        }
        if (DebugLevels.DEBUG_0)
            System.out.println("Total number of inputs = " + inputsAll.size());

        return (inputsAll);
    }

    public synchronized Case[] copyCaseObject(int[] toCopy, User user) throws EmfException {
        List<Case> copiedList = new ArrayList<Case>();

        for (int i = 0; i < toCopy.length; i++) {
            Case caseToCopy = getCase(toCopy[i]);
            try {
                copiedList.add(copySingleCaseObj(caseToCopy, user));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not copy case " + caseToCopy.getName() + ".", e);
                throw new EmfException("Could not copy case " + caseToCopy.getName() + ". " + e.getMessage());
            }
        }

        return copiedList.toArray(new Case[0]);
    }

    private synchronized void copyCaseInputs(int origCaseId, int copiedCaseId) throws Exception {
        CaseInput[] tocopy = getCaseInputs(origCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleInput(tocopy[i], copiedCaseId);
    }

    private synchronized CaseInput copySingleInput(CaseInput input, int copiedCaseId) throws Exception {
        CaseInput copied = (CaseInput) DeepCopy.copy(input);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(input.getCaseJobID(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setCaseJobID(copiedJob.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        } finally {

            session.close();
        }

        return addCaseInput(copied);
    }

    private synchronized Case addCopiedCase(Case element, User user) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            return dao.update(locked, session);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not add case " + element, e);
            throw new EmfException("Could not add case " + element);
        } finally {
            session.close();
        }
    }

    private synchronized void copyCaseJobs(int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseJob[] tocopy = getCaseJobs(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleJob(tocopy[i], copiedCaseId);
    }

    private synchronized CaseJob copySingleJob(CaseJob job, int copiedCaseId) throws Exception {
        job.setRunCompletionDate(new Date());
        job.setRunStartDate(new Date());
        CaseJob copied = (CaseJob) DeepCopy.copy(job);
        copied.setCaseId(copiedCaseId);
        copied.setJobkey(null); // jobkey supposedly generated when it is run
        copied.setRunstatus(null);
        copied.setRunLog(null);
        copied.setRunStartDate(null);
        copied.setRunCompletionDate(null);

        return addCaseJob(copied);
    }

    public ParameterName[] getParameterNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterName> type = dao.getParameterNames(session);

            return type.toArray(new ParameterName[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameter names.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter names.\n");
        } finally {
            session.close();
        }
    }

    public synchronized CaseParameter addCaseParameter(CaseParameter param) throws EmfException {
        Session session = sessionFactory.getSession();

        if (dao.caseParameterExists(param, session))
            throw new EmfException(
                    "The combination of 'Parameter Name', 'Sector', 'Program', and 'Job' should be unique.");

        try {
            dao.addParameter(param, session);
            return (CaseParameter) dao.loadCaseParameter(param, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case parameter '" + param.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case parameter '" + param.getName() + "'");
        } finally {
            session.close();
        }
    }

    public void removeCaseParameters(CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseParameters(params, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case parameter " + params[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case parameter " + params[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(caseId, session);

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized Executable addExecutable(Executable exe) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.exeutableExists(session, exe))
                dao.add(exe, session);

            return (Executable) dao.load(Executable.class, exe.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new executable '" + exe.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new executable '" + exe.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addParameterName(name, session);
            return (ParameterName) dao.load(ParameterName.class, name.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new parameter name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ValueType[] getValueTypes() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ValueType> type = dao.getValueTypes(session);

            return type.toArray(new ValueType[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all value types.\n" + e.getMessage());
            throw new EmfException("Could not get all value types.\n");
        } finally {
            session.close();
        }
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addValueType(type, session);
            return (ValueType) dao.load(ValueType.class, type.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new value type '" + type.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new value type '" + type.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterEnvVar> envvar = dao.getParameterEnvVars(session);

            return envvar.toArray(new ParameterEnvVar[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameter env variables.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter env variables.\n");
        } finally {
            session.close();
        }
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(envVar, session);
            return (ParameterEnvVar) dao.load(ParameterEnvVar.class, envVar.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new parameter env variable '" + envVar.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter env variable '" + envVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    private synchronized void copyCaseParameters(int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseParameter[] tocopy = getCaseParameters(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleParameter(tocopy[i], copiedCaseId);
    }

    private synchronized CaseParameter copySingleParameter(CaseParameter parameter, int copiedCaseId) throws Exception {
        CaseParameter copied = (CaseParameter) DeepCopy.copy(parameter);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(parameter.getJobId(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setJobId(copiedJob.getId());
            }
        } finally {
            session.close();
        }

        return addCaseParameter(copied);
    }

    public synchronized CaseJob addCaseJob(CaseJob job) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(job, session);
            return (CaseJob) dao.loadCaseJob(job);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case job '" + job.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case job '" + job.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseJob[] getCaseJobs(int caseId) throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<CaseJob> jobs = dao.getCaseJobs(caseId, session);

            return jobs.toArray(new CaseJob[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all jobs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all jobs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public JobRunStatus[] getJobRunStatuses() throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<JobRunStatus> runstatuses = dao.getJobRunStatuses(session);

            return runstatuses.toArray(new JobRunStatus[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job run statuses.\n" + e.getMessage());
            throw new EmfException("Could not get all job run statuses.\n");
        } finally {
            session.close();
        }
    }

    public Executable[] getExecutables(int casejobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Executable> runstatuses = dao.getExecutables(session);

            return runstatuses.toArray(new Executable[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all executables.\n" + e.getMessage());
            throw new EmfException("Could not get all executables.\n");
        } finally {
            session.close();
        }
    }

    private void resetRelatedJobsField(CaseJob[] jobs) throws EmfException {
        int jobslen = jobs.length;

        if (jobslen == 0)
            return;

        Session session = sessionFactory.getSession();

        try {
            int caseId = jobs[0].getCaseId();
            CaseInput[] inputs = (CaseInput[]) dao.getCaseInputs(caseId, session).toArray(new CaseInput[0]);
            CaseParameter[] params = dao.getCaseParameters(caseId, session).toArray(new CaseParameter[0]);

            for (int i = 0; i < jobslen; i++) {
                for (int j = 0; j < inputs.length; j++)
                    if (inputs[j].getCaseJobID() == jobs[i].getId())
                        inputs[j].setCaseJobID(0);

                for (int k = 0; k < params.length; k++)
                    if (params[k].getJobId() == jobs[i].getId())
                        params[k].setJobId(0);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not reset case job.\n" + e.getMessage());
            throw new EmfException("Could not reset case job ");
        } finally {
            session.close();
        }

    }

    public void removeCaseJobs(CaseJob[] jobs) throws EmfException {

        Session session = sessionFactory.getSession();

        resetRelatedJobsField(jobs);

        try {
            dao.removeCaseJobs(jobs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case job " + jobs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case job " + jobs[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    private synchronized Case copySingleCaseObj(Case toCopy, User user) throws Exception {
        Case copied = (Case) DeepCopy.copy(toCopy);
        copied.setName(getUniqueNewName("Copy of " + toCopy.getName()));
        copied.setTemplateUsed(toCopy.getName());
        copied.setAbbreviation(null);
        Case loaded = addCopiedCase(copied, user);
        copyCaseJobs(toCopy.getId(), loaded.getId()); // copy job first for references in input and parameter
        copyCaseInputs(toCopy.getId(), loaded.getId());
        copyCaseParameters(toCopy.getId(), loaded.getId());

        Session session = sessionFactory.getSession();

        try {

            // FIXME: Verfiy why locked?
            // NOTE: it could be being edited by other user, but you still want to copy it
            if (loaded.isLocked())
                dao.releaseLocked(loaded, session);
        } finally {
            session.close();
        }

        return loaded;
    }

    private String getUniqueNewName(String name) {
        Session session = sessionFactory.getSession();
        List<String> names = new ArrayList<String>();

        try {
            List<Case> allCases = dao.getCases(session);

            for (Iterator<Case> iter = allCases.iterator(); iter.hasNext();) {
                Case caseObj = iter.next();
                if (caseObj.getName().startsWith(name)) {
                    names.add(caseObj.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all cases.\n" + e.getMessage());
        } finally {
            session.close();
        }

        if (names.size() == 0)
            return name;

        return name + " " + getSequence(name, names);
    }

    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            // NOTE: Assume one case won't be copied 10000 times.
            // This is farely safe assuming the random number do not duplicate.
            return Math.abs(new Random().nextInt()) % 10000;
        }
    }

    public synchronized void exportInputsForCase(User user, String dirName, String purpose, boolean overWrite,
            int caseId) throws EmfException {
        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();
            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);
            if (!dir.exists())
                dir.mkdirs();

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);
        }
    }

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseParameter loaded = (CaseParameter) dao.loadCaseParameter(parameter, session);

            if (loaded != null && loaded.getId() != parameter.getId())
                throw new EmfException("Case parameter uniqueness check failed (" + loaded.getId() + ","
                        + parameter.getId() + ")");

            // FIXME: why session.clear()?
            session.clear();
            dao.updateCaseParameter(parameter, session);
            // setStatus(user, "Saved parameter " + parameter.getName() + " to database.", "Save Parameter");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case parameter: " + parameter.getName() + ".\n" + e);
            throw new EmfException("Could not update case parameter: " + parameter.getName() + ".");
        } finally {
            session.close();
        }
    }

    /**
     * SubmitJobs(...) is called form the CaseServiceImpl.
     * 
     * 
     */
    public synchronized String submitJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        if (DebugLevels.DEBUG_0)
            System.out.println("ManagedCaseService::submitJobs size: " + jobIds.length + " for caseId= " + caseId);

        // create a new caseJobSubmitter for each client call in a session
        TaskSubmitter caseJobSubmitter = new CaseJobSumitter(sessionFactory);

        try {
            String caseJobExportSubmitterId = null;
            String caseJobSubmitterId = caseJobSubmitter.getSubmitterId();

            if (DebugLevels.DEBUG_0)
                System.out.println("Is CaseJobSubmitterId null? " + (caseJobSubmitterId == null));
            // FIXME: Does this need to be done in a new DAO method???
            // Get the CaseJobs for each jobId
            for (Integer jobId : jobIds) {
                int jid = jobId.intValue();
                String jobKey = null;

                if (DebugLevels.DEBUG_0)
                    System.out.println("The jobId= " + jid);
                CaseJob caseJob = this.getCaseJob(jid);

                // NOTE: This is where the jobkey is generated and set in the CaseJob
                jobKey = this.createJobKey(jid);

                // set the job key in the case job
                caseJob.setJobkey(jobKey);

                // set the user for the case job
                User jobUser = caseJob.getUser();
                if (jobUser == null || !jobUser.equals(user)) {
                    caseJob.setUser(user);
                }

                if (DebugLevels.DEBUG_0)
                    System.out.println("Is the caseJob for this jobId null? " + (caseJob == null));
                Case jobCase = this.getCase(caseId);

                if (DebugLevels.DEBUG_0)
                    System.out.println("caseId= " + caseId + " Is the Case for this job null? " + (jobCase == null));
                // FIXME: Is this still needed?????
                // caseJob.setRunStartDate(new Date());
                CaseJobTask cjt = new CaseJobTask(jid, caseId, user);
                cjt.setJobkey(jobKey);

                // get or create the reference to the Managed Export Service for this casejobtask
                ManagedExportService expSvc = this.getExportService();

                if (DebugLevels.DEBUG_6)
                    System.out.println("set the casejobsubmitter id = " + caseJobSubmitterId);

                cjt.setSubmitterId(caseJobSubmitterId);

                if (DebugLevels.DEBUG_6)
                    System.out.println("setJobFileContent");

                if (DebugLevels.DEBUG_9)
                    System.out.println("before setJobFileContent");

                cjt.setJobFileContent(this.createJobFileContent(caseJob, user, expSvc));

                if (DebugLevels.DEBUG_9)
                    System.out.println("before getJobFileName");

                String jobFileName = this.getJobFileName(caseJob);

                cjt.setJobFile(jobFileName);
                cjt.setLogFile(this.getLog(jobFileName));
                cjt.setJobName(caseJob.getName());
                if (DebugLevels.DEBUG_6)
                    System.out.println("set Host Name");
                cjt.setHostName(caseJob.getHost().getName());
                if (DebugLevels.DEBUG_6)
                    System.out.println("getQueOptions");
                cjt.setQueueOptions(caseJob.getQueOptions());

                // FIXME: BELOW FOR TESTING ONLY
                cjt.setReadyTrue();
                cjt.setDependenciesSet(true);
                // FIXME: ABOVE FOR TESTING ONLY

                if (DebugLevels.DEBUG_6)
                    System.out.println("Completed setting the CaseJobTask");

                List<CaseInput> inputs = getAllJobInputs(caseJob);

                if (DebugLevels.DEBUG_6)
                    System.out.println("Number of inputs for this job: " + inputs.size());

                // FIXME: Need to flesh this string out
                // purpose = "Used by CaseName and JobName"

                String purpose = "Used by " + caseJob.getName() + " of Case " + jobCase.getName();
                if (DebugLevels.DEBUG_6)
                    System.out.println("Purpose= " + purpose);

                // pass the inputs to the exportService which uses an exportJobSubmitter to work with exportTaskManager
                caseJobExportSubmitterId = expSvc
                        .exportForJob(user, inputs, cjt.getTaskId(), purpose, caseJob, jobCase);

                String runStatusExporting = "Exporting";

                caseJob.setRunstatus(getJobRunStatus(runStatusExporting));
                caseJob.setRunStartDate(new Date());

                // Now update the casejob in the database
                updateJob(caseJob);

                if (DebugLevels.DEBUG_6)
                    System.out.println("Added caseJobTask to collection");

                if (DebugLevels.DEBUG_0)
                    System.out.println("Case Job Export Submitter Id for case job:" + caseJobExportSubmitterId);

                TaskManagerFactory.getCaseJobTaskManager(sessionFactory).addTask(cjt);
            }

            if (DebugLevels.DEBUG_0)
                System.out.println("Case Job Submitter Id for case job:" + caseJobSubmitterId);

            return caseJobSubmitterId;
        } catch (EmfException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());

        }
    }

    private JobRunStatus getJobRunStatus(String runStatus) throws EmfException {

        Session session = sessionFactory.getSession();
        JobRunStatus jrStat = null;

        try {
            jrStat = dao.getJobRunStatuse(runStatus);

            return jrStat;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job run status.\n" + e.getMessage());
            throw new EmfException("Could not get job run status.\n");
        } finally {
            session.close();
        }
    }

    private synchronized void updateJob(CaseJob caseJob) throws EmfException {
        try {
            dao.updateCaseJob(caseJob);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } 
    }

    public synchronized void updateCaseJob(User user, CaseJob job) throws EmfException {
        try {
            CaseJob loaded = (CaseJob) dao.loadCaseJob(job);
            if (user == null)
                throw new EmfException("Running Case Job requires a valid user");

            if (loaded != null && loaded.getId() != job.getId())
                throw new EmfException("Case job uniqueness check failed (" + loaded.getId() + "," + job.getId() + ")");

            dao.updateCaseJob(job);
            // this should go to case message panel instead
            // setStatus(user, "Saved job " + job.getName() + " to database.", "Save Job");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public Host[] getHosts() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Host> hosts = dao.getHosts(session);

            return hosts.toArray(new Host[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all hosts.\n" + e.getMessage());
            throw new EmfException("Could not get all hosts.\n");
        } finally {
            session.close();
        }
    }

    public synchronized Host addHost(Host host) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(host, session);
            return (Host) dao.load(Host.class, host.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new host '" + host.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new host '" + host.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {

        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();

            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String exportDir = caseObj.getInputFileDir() + System.getProperty("file.separator") + subDir;

            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version }, exportDir,
                    purpose, false);

        }

    }

    public synchronized void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();
            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String exportDir = caseObj.getInputFileDir() + System.getProperty("file.separator") + subDir;

            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version }, exportDir,
                    purpose, true);

        }

    }

    public synchronized void export(User user, String dirName, String purpose, boolean overWrite, int caseId)
            throws EmfException {
        System.out.println("ManagedCaseService::export for caseId: " + caseId);

        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();

            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);
            if (!dir.exists())
                dir.mkdirs();

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);

        }
    }

    private CaseParameter[] getJobParameters(int caseId, int jobId, Sector sector) throws EmfException {
        /**
         * Gets all the parameters for this job, selects based on: case ID, job ID, and sector
         */
        Session session = sessionFactory.getSession();

        // select the inputs based on 3 criteria
        try {
            List<CaseParameter> parameters = dao.getJobParameters(caseId, jobId, sector, session);
            // return an array of all type CaseParameter
            return parameters.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId
                    + ").\n");
        } finally {
            session.close();
        }
    }

    private synchronized List<CaseInput> excludeInputsEnv(List<CaseInput> inputs, String envname) {
        /**
         * Excludes input elements from the inputs list based on environmental variable name.
         * 
         * NOTE: the # of elements of inputs is modified in the calling routine also, if an input has no environmental
         * variable, it will be treated as having the name ""
         */
        List<CaseInput> exclInputs = new ArrayList();
        String inputEnvName = "";

        // loop of inputs (using an iterator) and test for this env name
        Iterator<CaseInput> iter = inputs.iterator();
        while (iter.hasNext()) {
            CaseInput input = iter.next();

            inputEnvName = (input.getEnvtVars() == null) ? "" : input.getEnvtVars().getName();
            // input has an environmental variable w/ this name
            if (inputEnvName.equals(envname)) {
                // add the input to exclude
                exclInputs.add(input);
            }
        }

        // Now remove the excluded elements from inputs
        Iterator<CaseInput> iter2 = exclInputs.iterator();
        while (iter2.hasNext()) {
            CaseInput exclInput = iter2.next();
            // remove this element from the input list
            inputs.remove(exclInput);
        }

        // return the exclude list
        return exclInputs;
    }

    private String setenvInput(CaseInput input, Case caseObj, ManagedExportService expSvc) throws EmfException {
        /**
         * Creates a line of the run job file. Sets the env variable to the value input file.
         * 
         * For eg. If the env variable is GRIDDESC, and the shell type is csh, this will return "setenv GRIDDESC
         * /home/azubrow/smoke/ge_dat/griddesc_12Apr2007_vo.txt"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */

        EmfDataset dataset = input.getDataset();
        InputEnvtVar envvar = input.getEnvtVars();
        SubDir subdir = input.getSubdirObj();
        // check if dataset or env variable is null, if so e
        if (dataset == null) {
            throw new EmfException("Input (" + input.getName() + ") must have a dataset");
        }

        // Create a full path to the input file
        String fullPath = expSvc.getCleanDatasetName(input.getDataset(), input.getVersion());
        if ((subdir != null) && !(subdir.toString()).equals("")) {
            fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator") + input.getSubdirObj()
                    + System.getProperty("file.separator") + fullPath;
        } else {
            fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator") + fullPath;
        }

        String setenvLine = null;
        if (envvar == null) {
            // if no environmental variable, just created a commented
            // line w/ input name = fullPath
            setenvLine = this.runComment + " " + input.getName() + " = " + fullPath + eolString;
        } else {
            setenvLine = shellSetenv(envvar.getName(), fullPath);
        }
        return setenvLine;
    }

    private String shellSetenv(String envvariable, String envvalue) {
        /**
         * Simply creates a setenv line from an environmental variable and a value.
         * 
         * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return "setenv GRIDDESC
         * US36_148x112"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */
        String setenvLine = this.runSet + " " + envvariable + this.runEq + envvalue + this.runTerminator;
        return setenvLine + eolString;

    }

    /**
     * Creates a line of the run job file. Sets the env variable to the value of the parameter.
     * 
     * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return
     * 
     * "setenv GRIDDESC US36_148x112"
     * 
     * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl, etc.
     */
    private String setenvParameter(CaseParameter parameter) throws EmfException {

        if (false)
            throw new EmfException();
        String setenvLine = null;
        if (parameter.getEnvVar() == null) {
            // no environmental variable, therefore create commented line
            // parameter name = value
            setenvLine = this.runComment + " " + parameter.getName() + " = " + parameter.getValue() + eolString;
        } else {
            setenvLine = shellSetenv(parameter.getEnvVar().getName(), parameter.getValue());

        }
        return setenvLine;
    }

    public synchronized String createJobFileContent(CaseJob job, User user, ManagedExportService expSvc)
            throws EmfException {
        // String jobContent="";
        String jobFileHeader = "";

        StringBuffer sbuf = new StringBuffer();

        /**
         * Creates the content string for a job run file w/ all necessary inputs and parameters set.
         * 
         * Input: job - the Case Job user - the user
         * 
         * Output: String - the job content
         */

        // Some objects needed for accessing data
        Case caseObj = this.getCase(job.getCaseId());
        int caseId = job.getCaseId();
        int jobId = job.getId();

        CaseInput headerInput = null; // input for the EMF Job header

        List<CaseInput> inputsAA = null; // inputs for all sectors and all jobs
        List<CaseInput> inputsSA = null; // inputs for specific sector and all jobs
        List<CaseInput> inputsAJ = null; // inputs for all sectors specific jobs
        List<CaseInput> inputsSJ = null; // inputs for specific sectors specific jobs

        String jobName = job.getName().replace(" ", "_");

        /*
         * Get the inputs in the following order: all sectors, all jobs sector specific, all jobs all sectors, job
         * specific sector specific, job specific
         * 
         * Need to search for inputs now, b/c want to see if there is an EMF_HEADER
         */

        // Create an export service to get names of the datasets as inputs to Smoke script
        // ExportService exports = new ExportService(dbServerlocal, this.threadPool, this.sessionFactory);
        // Get case inputs (the datasets associated w/ the case)
        // All sectors, all jobs
        inputsAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS);

        // Exclude any inputs w/ environmental variable EMF_JOBHEADER
        List<CaseInput> exclInputs = this.excludeInputsEnv(inputsAA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // Sector specific, all jobs
        Sector sector = job.getSector();
        if (sector != this.ALL_SECTORS) {
            inputsSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector);

            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            exclInputs = this.excludeInputsEnv(inputsSA, "EMF_JOBHEADER");
            if (exclInputs.size() != 0) {
                headerInput = exclInputs.get(0); // get the first element as header
            }
        }

        // All sectors, job specific
        inputsAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS);

        // Exclude any inputs w/ environmental variable EMF_JOBHEADER
        exclInputs = this.excludeInputsEnv(inputsAJ, "EMF_JOBHEADER");
        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // Specific sector and specific job
        if (sector != this.ALL_SECTORS) {
            inputsSJ = this.getJobInputs(caseId, jobId, sector);

            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            exclInputs = this.excludeInputsEnv(inputsSJ, "EMF_JOBHEADER");
            if (exclInputs.size() != 0) {
                headerInput = exclInputs.get(0); // get the first element as header
            }
        }

        // Get header String:
        if (headerInput != null) {
            // Header string starts the job file content string

            try {
                // get the string of the EMF_JOBHEADER from the input
                jobFileHeader = getJobFileHeader(headerInput);
                sbuf.append(jobFileHeader);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Could not write EMF header to job script file, " + e.getMessage());
                throw new EmfException("Could not write EMF header to job script file");
            }

        } else {
            // Start the job file content string and append the end of line characters for this OS
            sbuf.append(this.runShell + this.eolString);
        }

        // print job name to file
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Job run file for job: " + jobName + eolString);

        /*
         * Define some EMF specific variables
         */
        sbuf.append(eolString);

        sbuf.append(this.runComment + " EMF specific variables" + eolString);
        sbuf.append(shellSetenv("EMF_JOBID", String.valueOf(jobId)));
        sbuf.append(shellSetenv("EMF_JOBNAME", jobName));
        sbuf.append(shellSetenv("EMF_USER", user.getUsername()));

        // Generate and get a unique job key, add it to the job,
        // update the db, and write it to the script
        String jobKey = createJobKey(jobId);
        job.setJobkey(jobKey);
        updateCaseJob(user, job);
        sbuf.append(shellSetenv("EMF_JOBKEY", job.getJobkey()));

        // Print the inputs to the file

        /*
         * loop over inputs and write Env variables and input (full name and path) to job run file, print comments
         */
        // All sectors and all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- for all sectors and all jobs" + eolString);
        if (inputsAA != null) {
            for (CaseInput input : inputsAA) {
                sbuf.append(setenvInput(input, caseObj, expSvc));
            }
        }

        // Sector specific and all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- sector (" + sector + ") and all jobs" + eolString);
        if (inputsSA != null) {
            for (CaseInput input : inputsSA) {
                sbuf.append(setenvInput(input, caseObj, expSvc));
            }
        }
        // All sectors and specific job
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- for all sectors and job: " + job + eolString);
        if (inputsAJ != null) {
            for (CaseInput input : inputsAJ) {
                sbuf.append(setenvInput(input, caseObj, expSvc));
            }
        }
        // Sector and Job specific
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- sector (" + sector + ") and job: " + job + eolString);
        if (inputsSJ != null) {
            for (CaseInput input : inputsSJ) {
                sbuf.append(setenvInput(input, caseObj, expSvc));
            }
        }
        /*
         * Get the parameters for this job in following order: from summary tab, all sectors, all jobs sector specific,
         * all jobs all sectors, job specific sector specific, job specific
         */

        // Parameters from the summary tab
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- from Case summary " + eolString);
        sbuf.append(shellSetenv("CASE", caseObj.getAbbreviation().toString()));
        // Need to have quotes around model name b/c could be more than one word
        String modelName = '"' + caseObj.getModel().toString() + '"';
        sbuf.append(shellSetenv("MODEL_LABEL", modelName));
        sbuf.append(shellSetenv("IOAPI_GRIDNAME_1", caseObj.getGrid().toString()));
        sbuf.append(shellSetenv("EMF_GRID", caseObj.getGridResolution().toString()));
        sbuf.append(shellSetenv("EMF_AQM", caseObj.getAirQualityModel().toString()));
        sbuf.append(shellSetenv("EMF_SPC", caseObj.getSpeciation().toString()));
        sbuf.append(shellSetenv("BASE_YEAR", caseObj.getEmissionsYear().toString())); // Should base year == emissions
                                                                                        // year ????
        // sbuf.append(shellSetenv("BASE_YEAR", String.valueOf(caseObj.getBaseYear())));
        sbuf.append(shellSetenv("FUTURE_YEAR", String.valueOf(caseObj.getFutureYear())));
        // sbuf.append(shellSetenv("EPI_STDATE_TIME", caseObj.getStartDate()));
        // sbuf.append(shellSetenv("EPI_ENDATE_TIME", caseObj.getEndDate()));

        // All sectors, all jobs
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- all sectors, all jobs " + eolString);
        CaseParameter[] parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, this.ALL_SECTORS);
        if (parameters != null) {
            for (CaseParameter param : parameters) {
                sbuf.append(setenvParameter(param));
            }

        }

        // Specific sector, all jobs
        if (sector != this.ALL_SECTORS) {
            sbuf.append(eolString);
            sbuf.append(this.runComment + " Parameters -- sectors (" + sector + "), all jobs " + eolString);
            parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, sector);
            if (parameters != null) {
                for (CaseParameter param : parameters) {
                    sbuf.append(setenvParameter(param));
                }
            }
        }
        // All sectors, specific job
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- all sectors, job: " + job + eolString);
        parameters = this.getJobParameters(caseId, jobId, this.ALL_SECTORS);
        if (parameters != null) {
            for (CaseParameter param : parameters) {
                sbuf.append(setenvParameter(param));
            }
        }
        // Specific sector, specific job
        if (sector != this.ALL_SECTORS) {
            sbuf.append(eolString);
            sbuf.append(this.runComment + " Parameters -- sectors (" + sector + "), job: " + job + eolString);
            parameters = this.getJobParameters(caseId, jobId, sector);
            if (parameters != null) {
                for (CaseParameter param : parameters) {
                    sbuf.append(setenvParameter(param));
                }
            }
        }
        // Getting the executable object from the job
        Executable execVal = job.getExecutable();

        // path to executable
        String execPath = job.getPath();

        // executable string
        String execName = execVal.getName();

        // executable full name and arguments
        String execFull = execPath + System.getProperty("file.separator") + execName;
        String execFullArgs = execFull + " " + job.getArgs() + eolString;
        // print executable
        sbuf.append(eolString);
        sbuf.append(this.runComment + " job executable" + eolString);
        sbuf.append("$EMF_CLIENT -k $EMF_JOBKEY -x " + execFull + " -m \"Running top level script\"" + eolString);
        sbuf.append(execFullArgs);

        // add a test of the status and send info through the
        // command client -- should generalize so not csh specific
        sbuf.append("if ( $status != 0 ) then" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Failed' -m \"ERROR running Job: $EMF_JOBNAME\" -t 'e' "
                + eolString);
        sbuf.append("\t exit(1)" + eolString);
        sbuf.append("else" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Completed' -m \"Completed job: $EMF_JOBNAME\"" + eolString);
        sbuf.append("endif" + eolString);

        // Send back the contents of jobContent string for job file
        return sbuf.toString();

    }// /end of createJobFileContent()

    private String getJobFileHeader(CaseInput headerInput) throws EmfException {
        /**
         * gets the EMF JOBHEADER as a string
         */

        // get some info from the header input
        EmfDataset dataset = headerInput.getDataset();
        Version version = headerInput.getVersion();
        System.out.println("Version of EMF JOBHEADER : " + version.getVersion());

        // create an exporter to get the string
        GenericExporterToString exporter = new GenericExporterToString(dataset, this.dbServer, this.dbServer
                .getSqlDataTypes(), new VersionedDataFormatFactory(version, dataset), null);

        // Get the string from the exporter
        try {
            exporter.export(null);
            String fileHeader = exporter.getOutputString();
            return fileHeader;

        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }

    }

    private String getJobFileName(CaseJob job) throws EmfException {
        /*
         * Creates the File name that corresponds to the job script file name.
         * 
         * Format: <jobname>_<case_abbrev>_<datestamp>.csh
         */
        String dateStamp = EmfDateFormat.format_YYYYMMDDHHMMSS(new Date());
        String jobName = job.getName().replace(" ", "_");
        Case caseObj = this.getCase(job.getCaseId());

        // Get case abbreviation, if no case abbreviation construct one from id
        String defaultAbbrev = "case" + job.getCaseId();
        String caseAbbrev = (caseObj.getAbbreviation() == null) ? defaultAbbrev : caseObj.getAbbreviation().getName();

        // Test ouput directory to place job script
        String outputFileDir = caseObj.getOutputFileDir();
        if ((outputFileDir == null) || (outputFileDir.equals(""))) {
            throw new EmfException("Output job script directory must be set to run job: " + job.getName());
        }

        String fileName = jobName + "_" + caseAbbrev + "_" + dateStamp + this.runSuffix;
        fileName = outputFileDir + System.getProperty("file.separator") + fileName;
        return fileName;
    }

    private String getLog(String jobFileName) throws EmfException {
        /*
         * From the job script name, it creates the name of corresponding log file. It also checks that an appropriate
         * log directory exists.
         */

        File file = new File(jobFileName);
        String fileShort = file.getName(); // file w/o path
        // Create a log file name by replacing the suffix of the job
        // of the job file w/ .log
        String logFileName = fileShort.replaceFirst(this.runSuffix, ".log");

        // Check if logs dir (jobpath/logs) exists, if not create
        File logDir = new File(file.getParent() + System.getProperty("file.separator") + "logs");
        if (!(logDir.isDirectory())) {
            // Need to create the directory
            if (!(logDir.mkdir())) {
                throw new EmfException("Error creating job log directory: " + logDir);
            }
        }

        // Make directory writable by everyone
        if (!logDir.setWritable(true, false)) {
            throw new EmfException("Error changing job log directory's write permissions: " + logDir);
        }

        // Create the logFile full name
        logFileName = logDir + System.getProperty("file.separator") + logFileName;

        return logFileName;

    }

    // for command line client
    public int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        try {
            CaseJob job = dao.getCaseJob(jobKey);

            if (job == null)
                throw new EmfException("No jobs found associated with job key: " + jobKey);

            User user = job.getUser();
            message.setCaseId(job.getCaseId());
            message.setJobId(job.getId());
            message.setReceivedTime(new Date());
            String status = message.getStatus();
            String jobStatus = job.getRunstatus().getName();
            String lastMsg = message.getMessage();

            if (lastMsg != null && !lastMsg.trim().isEmpty())
                job.setRunLog(lastMsg);

            if (!status.isEmpty() && !jobStatus.equalsIgnoreCase(status)) {
                job.setRunstatus(dao.getJobRunStatuse(status));

                // If the status from the Command Client is not Completed or not Failed
                // then the job is Running. A Running job gets a Run Start Date
                // all other statuses get a Run Completion Date.
                if (!(status.equalsIgnoreCase("Running"))) {
                    job.setRunCompletionDate(new Date());
                } else {
                    job.setRunStartDate(new Date());
                }

                dao.updateCaseJob(job);
            }

            if (!user.getUsername().equalsIgnoreCase(message.getRemoteUser()))
                throw new EmfException("Remote user doesn't match the user who runs the job.");

            dao.add(message);

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        Session session = sessionFactory.getSession();
        List<JobMessage> msgs = null;

        try {
            if (jobId == 0)
                msgs = dao.getJobMessages(caseId, session);
            else
                msgs = dao.getJobMessages(caseId, jobId, session);

            return msgs.toArray(new JobMessage[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void finalize() throws Throwable {
        this.session = null;
        super.finalize();
    }

}

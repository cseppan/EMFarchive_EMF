package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.generic.LineExporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.exim.ExportService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class CaseServiceImpl implements CaseService {
    private static Log LOG = LogFactory.getLog(CaseServiceImpl.class);

    private CaseDAO dao;

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbFactory;

    private DbServer dbServer;

    private ExportService exportService;
    
    // these run fields are used to create the run script
    private String runShell = "#!/bin/csh";  //shell to run under
    
    private String runSet = "setenv";  // how to set a variable
    
    private String runEq = " ";  // equate a variable (could be a space)
    
    private String runTerminator = ""; // line terminator
    
    private String runComment = "##";  // line comment 
    
    private String runSuffix = ".csh";  // job run file suffix

    // all sectors and all jobs id in the case inputs tb
    private static Sector ALL_SECTORS = null;
    private static int ALL_JOB_ID = 0;

    public CaseServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public CaseServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbFactory) {
        this.sessionFactory = sessionFactory;
        this.dbFactory = dbFactory;
        this.dao = new CaseDAO();
        this.threadPool = createThreadPool();
    }

    protected void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    private void createExportService() throws EmfException {
        try {
            this.dbServer = dbFactory.getDbServer();
            this.exportService = new ExportService(dbServer, threadPool, sessionFactory);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public Case[] getCases() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List cases = dao.getCases(session);
            session.close();

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        }
    }

    private Case getCase(int caseId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case caseObj = dao.getCase(caseId, session);
            session.close();

            return caseObj;
        } catch (RuntimeException e) {
            LOG.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        }
    }

    public Abbreviation[] getAbbreviations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List abbreviations = dao.getAbbreviations(session);
            session.close();

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        }
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List airQualityModels = dao.getAirQualityModels(session);
            session.close();

            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        }
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getCaseCategories(session);
            session.close();

            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        }
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getEmissionsYears(session);
            session.close();

            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        }
    }

    public Grid[] getGrids() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getGrids(session);
            session.close();

            return (Grid[]) results.toArray(new Grid[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        }
    }

    public GridResolution[] getGridResolutions() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getGridResolutions(session);
            session.close();

            return (GridResolution[]) results.toArray(new GridResolution[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grid Resolutions", e);
            throw new EmfException("Could not get all Grid Resolutions");
        }
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getMeteorlogicalYears(session);
            session.close();

            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Meteorological Years", e);
            throw new EmfException("Could not get all Meteorological Years");
        }
    }

    public Speciation[] getSpeciations() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getSpeciations(session);
            session.close();

            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        }
    }

    public void addCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        }
    }

    public void removeCase(Case caseObj) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            setStatus(caseObj.getLastModifiedBy(), "Start removing case " + caseObj.getName() + ".", "Remove Case");
            List<CaseInput> inputs = dao.getCaseInputs(caseObj.getId(), session);
            dao.removeCaseInputs(inputs.toArray(new CaseInput[0]), session);

            List<CaseJob> jobs = dao.getCaseJobs(caseObj.getId(), session);
            dao.removeCaseJobs(jobs.toArray(new CaseJob[0]), session);

            List<CaseParameter> parameters = dao.getCaseParameters(caseObj.getId(), session);
            dao.removeCaseParameters(parameters.toArray(new CaseParameter[0]), session);

            dao.remove(caseObj, session);
            setStatus(caseObj.getLastModifiedBy(), "Finished removing case " + caseObj.getName() + ".", "Remove Case");
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not remove Case: " + caseObj, e);
            throw new EmfException("Could not remove Case: " + caseObj);
        }
    }

    private void setStatus(User user, String message, String type) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType(type);
        status.setMessage(message);
        status.setTimestamp(new Date());
        StatusDAO statusDao = new StatusDAO(sessionFactory);
        statusDao.add(status);
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case locked = dao.obtainLocked(owner, element, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        }
    }

    public Case releaseLocked(Case locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Case released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock by " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock by " + locked.getLockOwner() + " for Case: " + locked);
        }
    }

    public Case updateCase(Case element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Case released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Case", e);
            throw new EmfException("Could not update Case: " + element);
        }
    }

    public InputName[] getInputNames() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getInputNames(session);
            session.close();

            return (InputName[]) results.toArray(new InputName[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        }
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getInputEnvtVars(session);
            session.close();

            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Input Environment Variables", e);
            throw new EmfException("Could not get all Input Environment Variables");
        }
    }

    public CaseProgram[] getPrograms() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getPrograms(session);
            session.close();

            return (CaseProgram[]) results.toArray(new CaseProgram[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        }
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getModelToRuns(session);
            session.close();

            return (ModelToRun[]) results.toArray(new ModelToRun[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Models To Run", e);
            throw new EmfException("Could not get all Models To Run");
        }
    }

    public void export(User user, String dirName, String purpose, boolean overWrite, Case caseToExport)
            throws EmfException {
        createExportService();
        EmfDataset[] datasets = getInputDatasets(caseToExport);
        Version[] versions = getInputDatasetVersions(caseToExport);
        SubDir[] subdirs = getSubdirs(caseToExport);

        if (datasets.length == 0)
            return;

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();
            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);
            if (!dir.exists())
                dir.mkdirs();

            exportService.export(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] }, exportDir,
                    purpose, overWrite);
        }
    }

    private Version[] getInputDatasetVersions(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        List<Version> list = new ArrayList<Version>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());

        return list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
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

    private SubDir[] getSubdirs(Case caseToExport) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseToExport.getId());
        SubDir[] subdirs = new SubDir[inputs.length];

        for (int i = 0; i < inputs.length; i++)
            subdirs[i] = inputs[i].getSubdirObj();

        return subdirs;
    }

    public InputName addCaseInputName(InputName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(name, session);
            return (InputName) dao.load(InputName.class, name.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new case input name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseProgram addProgram(CaseProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(program, session);
            return (CaseProgram) dao.load(CaseProgram.class, program.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new program '" + program.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new program '" + program.getName() + "'");
        } finally {
            session.close();
        }
    }

    public InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(inputEnvtVar, session);
            return (InputEnvtVar) dao.load(InputEnvtVar.class, inputEnvtVar.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new input environment variable '" + inputEnvtVar.getName() + "'\n"
                    + e.getMessage());
            throw new EmfException("Could not add new input environment variable '" + inputEnvtVar.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(model, session);
            return (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new model to run '" + model.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            session.close();
        }
    }

    public GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(gridResolution, session);
            return (GridResolution) dao.load(GridResolution.class, gridResolution.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new Grid Resolution '" + gridResolution.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new Grid Resolution '" + gridResolution.getName() + "'");
        } finally {
            session.close();
        }
    }

    public SubDir[] getSubDirs() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List results = dao.getSubDirs(session);
            session.close();

            return (SubDir[]) results.toArray(new SubDir[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all subdirectories", e);
            throw new EmfException("Could not get all subdirectories");
        }
    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(subdir, session);
            return (SubDir) dao.load(SubDir.class, subdir.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new subdirectory '" + subdir.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new subdirectory '" + subdir.getName() + "'");
        } finally {
            session.close();
        }
    }

    public CaseInput addCaseInput(CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        if (dao.caseInputExists(input, session))
            throw new EmfException("The combination of 'Input Name', 'Sector', 'Program', and 'Job' should be unique.");

        try {
            dao.add(input, session);
            return (CaseInput) dao.loadCaseInput(input, session);
        } catch (Exception e) {
            LOG.error("Could not add new case input '" + input.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case input '" + input.getName() + "'");
        } finally {
            session.close();
        }
    }

    public void updateCaseInput(User user, CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseInput loaded = (CaseInput) dao.loadCaseInput(input, session);

            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed (" + loaded.getId() + "," + input.getId()
                        + ")");

            session.clear();
            dao.updateCaseInput(input, session);
            setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            LOG.error("Could not update case input: " + input.getName() + ".\n" + e);
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
            LOG.error("Could not remove case input " + inputs[0].getName() + " etc.\n" + e.getMessage());
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
            LOG.error("Could not get all inputs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    private List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector) throws EmfException {
        /**
         * Gets all the inputs for this job, selects based on: 
         *      case ID, job ID, and sector
         */
        Session session = sessionFactory.getSession();

        // select the inputs based on 3 criteria
        try {
            List<CaseInput> inputs = dao.getJobInputs(caseId, jobId, sector, session);
            // return an array of all type CaseInput
            //return inputs.toArray(new CaseInput[0]);
            return inputs;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not get all inputs for case (id=" + caseId + 
                    "), job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all inputs for case (id=" + caseId + 
                    "), job (id=" + jobId + ").\n");
        } finally {
            session.close();
        }
    }
    
    private List <CaseInput> excludeInputsEnv(List<CaseInput> inputs, String envname) {
        /**
         * Excludes input elements from the inputs list based on 
         * environmental variable name.
         * 
         * NOTE: the # of elements of inputs is modified in the calling routine
         */
        List<CaseInput> exclInputs = new ArrayList();
        
        // loop of inputs (using an iterator) and test for this env name
        Iterator<CaseInput> iter = inputs.iterator();
        while(iter.hasNext()) {
            CaseInput input = iter.next();
            
            // input has an environmental variable w/ this name
            if (input.getEnvtVars().getName().equals(envname)){
                // add the input to exclude
                exclInputs.add(input);
                
            }
        }
        
        // Now remove the excluded elements from inputs
        Iterator<CaseInput> iter2 = exclInputs.iterator();
        while(iter2.hasNext()){
            CaseInput exclInput = iter2.next();
            // remove this element from the input list
            inputs.remove(exclInput);
        }
        
        // return the exclude list
        return exclInputs;
    }

    private CaseParameter[] getJobParameters(int caseId, int jobId, Sector sector) throws EmfException {
        /**
         * Gets all the parameters for this job, selects based on: 
         *      case ID, job ID, and sector
         */
        Session session = sessionFactory.getSession();

        // select the inputs based on 3 criteria
        try {
            List<CaseParameter> parameters = dao.getJobParameters(caseId, jobId, sector, session);
            // return an array of all type CaseParameter
            return parameters.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not get all parameters for case (id=" + caseId + 
                    "), job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + 
                    "), job (id=" + jobId + ").\n");
        } finally {
            session.close();
        }
    }

    public Case[] copyCaseObject(int[] toCopy) throws EmfException {
        List<Case> copiedList = new ArrayList<Case>();

        for (int i = 0; i < toCopy.length; i++) {
            Case caseToCopy = getCase(toCopy[i]);
            try {
                copiedList.add(copySingleCaseObj(caseToCopy));
            } catch (Exception e) {
                LOG.error("Could not copy case " + caseToCopy.getName() + ".", e);
                throw new EmfException("Could not copy case " + caseToCopy.getName() + ". " + e.getMessage());
            }
        }

        return copiedList.toArray(new Case[0]);
    }

    private Case copySingleCaseObj(Case toCopy) throws Exception {
        Case copied = (Case) DeepCopy.copy(toCopy);
        copied.setName("Copy of " + toCopy.getName() + " " + new Date().getTime());
        copied.setTemplateUsed(toCopy.getName());
        Case loaded = addCopiedCase(copied);
        copyCaseJobs(toCopy.getId(), loaded.getId()); // copy job first for references in input and parameter
        copyCaseInputs(toCopy.getId(), loaded.getId());
        copyCaseParameters(toCopy.getId(), loaded.getId());

        Session session = sessionFactory.getSession();

        try {
            if (loaded.isLocked())
                dao.releaseLocked(loaded, session);
        } finally {
            session.close();
        }

        return loaded;
    }

    private void copyCaseInputs(int origCaseId, int copiedCaseId) throws Exception {
        CaseInput[] tocopy = getCaseInputs(origCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleInput(tocopy[i], copiedCaseId);
    }

    private CaseInput copySingleInput(CaseInput input, int copiedCaseId) throws Exception {
        CaseInput copied = (CaseInput) DeepCopy.copy(input);
        copied.setCaseID(copiedCaseId);

        Session session = sessionFactory.getSession();
        try {
            CaseJob job = dao.getCaseJob(input.getCaseJobID(), session);

            if (job != null) {
                CaseJob copiedJob = dao.getCaseJob(copiedCaseId, job, session);
                copied.setCaseJobID(copiedJob.getId());
            }
        } finally {
            session.close();
        }

        return addCaseInput(copied);
    }

    private Case addCopiedCase(Case element) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(element, session);
            return (Case) dao.load(Case.class, element.getName(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not add case " + element, e);
            throw new EmfException("Could not add case " + element);
        } finally {
            session.close();
        }
    }

    private void copyCaseJobs(int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseJob[] tocopy = getCaseJobs(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleJob(tocopy[i], copiedCaseId);
    }

    private CaseJob copySingleJob(CaseJob job, int copiedCaseId) throws Exception {
        CaseJob copied = (CaseJob) DeepCopy.copy(job);
        copied.setCaseId(copiedCaseId);

        return addCaseJob(copied);
    }

    private void copyCaseParameters(int toCopyCaseId, int copiedCaseId) throws Exception {
        CaseParameter[] tocopy = getCaseParameters(toCopyCaseId);

        for (int i = 0; i < tocopy.length; i++)
            copySingleParameter(tocopy[i], copiedCaseId);
    }

    private CaseParameter copySingleParameter(CaseParameter parameter, int copiedCaseId) throws Exception {
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

    public CaseJob addCaseJob(CaseJob job) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(job, session);
            return (CaseJob) dao.loadCaseJob(job, session);
        } catch (Exception e) {
            LOG.error("Could not add new case job '" + job.getName() + "'\n" + e.getMessage());
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
            LOG.error("Could not get all jobs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all jobs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public CaseJob getCaseJob(int jobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            LOG.error("Could not get job for job id " + jobId + ".\n" + e.getMessage());
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
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
            LOG.error("Could not get all job run statuses.\n" + e.getMessage());
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
            LOG.error("Could not get all executables.\n" + e.getMessage());
            throw new EmfException("Could not get all executables.\n");
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
            LOG.error("Could not remove case job " + jobs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case job " + jobs[0].getName() + " etc.");
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
            LOG.error("Could not reset case job.\n" + e.getMessage());
            throw new EmfException("Could not reset case job ");
        } finally {
            session.close();
        }

    }

    public void updateCaseJob(User user, CaseJob job) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob loaded = (CaseJob) dao.loadCaseJob(job, session);
            if (user == null) 
                throw new EmfException("Running Case Job requires a valid user");
            
            if (loaded != null && loaded.getId() != job.getId())
                throw new EmfException("Case job uniqueness check failed (" + loaded.getId() + "," + job.getId() + ")");

            session.clear();
            dao.updateCaseJob(job, session);
            setStatus(user, "Saved job " + job.getName() + " to database.", "Save Job");
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOG.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        } finally {
            session.close();
        }
    }

    public Host[] getHosts() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Host> hosts = dao.getHosts(session);

            return hosts.toArray(new Host[0]);
        } catch (Exception e) {
            LOG.error("Could not get all hosts.\n" + e.getMessage());
            throw new EmfException("Could not get all hosts.\n");
        } finally {
            session.close();
        }
    }

    public Host addHost(Host host) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(host, session);
            return (Host) dao.load(Host.class, host.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new host '" + host.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new host '" + host.getName() + "'");
        } finally {
            session.close();
        }
    }

    public void runJob(CaseJob job, User user) throws EmfException {
        /**
         * runs the job.
         * 
         * Creates a job run file which sets up the environment including input files and parameters for the run script
         * defines the run script, and then executes it.
         */
        
        String executionStr = null;
        
        // get the case
        Case caseObj = this.getCase(job.getCaseId());

        // Test ouput directory to place job script
        String outputFileDir = caseObj.getOutputFileDir();
        if ((outputFileDir == null) || (outputFileDir.equals(""))){
            throw new EmfException("Output job script directory must be set to run job: " + job.getName() );
        }

        // Job run file name, put it in the top directory of SMOKE output job scripts
        String dateStamp = EmfDateFormat.format_YYYYMMDDHHMMSS(new Date());
        String jobName = job.getName().replace(" ", "_");
        String fileName = jobName + "_" + dateStamp + this.runSuffix;
        File ofile = new File(outputFileDir + System.getProperty("file.separator") + fileName);
        
        
        // Create job script
        writeJobFile(job, user, ofile);
        
        // get log file for job script        
        String logFileName = outputFileDir + System.getProperty("file.separator") + "logs"
        + System.getProperty("file.separator") + jobName + "_" 
        + dateStamp + ".log";
        
        //replace EMF_JOBLOG w/ log file
        
        
        // Run export task
        
        // Create an execution string and submit job to the queue,
        // if the key word $EMF_JOBLOG is in the queue options, 
        // replace w/ log file 
        String queueOptions = job.getQueOptions();
        String queueOptionsLog = queueOptions.replace("$EMF_JOBLOG", logFileName);
        if (queueOptionsLog.equals("")){
            executionStr = ofile.getName();
        } else {
            executionStr = queueOptionsLog + " " + ofile; 
        }
        System.out.println("Job execution string: " + executionStr);

    }
    

    private String setenvInput(CaseInput input, Case caseObj, ExportService exports) throws EmfException{
        /**
         * Creates a line of the run job file.  Sets the env variable
         * to the value input file.
         * 
         * For eg.  If the env variable is GRIDDESC, and the 
         * shell type is csh, this will return
         * "setenv GRIDDESC /home/azubrow/smoke/ge_dat/griddesc_12Apr2007_vo.txt"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other
         * languages, for example python, perl, etc.
         */
        
        EmfDataset dataset = input.getDataset();
        InputEnvtVar envvar = input.getEnvtVars();
        SubDir subdir = input.getSubdirObj();
        // check if dataset or env variable is null, if so e
        if (dataset == null){
            throw new EmfException("Input ("+input.getName()+
                    ") must have a dataset");
        }
        if (envvar == null) {
            throw new EmfException("Input ("+input.getName()+
            ") must have an environmental variable");
        }

        // Create a full path to the input file
        String fullPath = exports.getCleanDatasetName(input.getDataset(), input.getVersion());
        if ((subdir != null) && !(subdir.toString()).equals("")){
            fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator") + input.getSubdirObj()
                    + System.getProperty("file.separator") + fullPath;            
        } else {
            fullPath = caseObj.getInputFileDir() + System.getProperty("file.separator") + fullPath;
        }
        
        String setenvLine = shellSetenv(envvar.getName(), fullPath);
        return setenvLine;
    }
    
    private String shellSetenv(String envvariable, String envvalue){
        /**
         * Simply creates a setenv line from an environmental variable and a value.
         * 
         * For eg.  If the env variable is IOAPI_GRIDNAME, and the 
         * shell type is csh, this will return
         * "setenv GRIDDESC US36_148x112"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other
         * languages, for example python, perl, etc.
         */
        String setenvLine = this.runSet + " " + envvariable + this.runEq + envvalue + this.runTerminator;
        return setenvLine;
        
    }
    

    private String setenvParameter(CaseParameter parameter) throws EmfException{
        /**
         * Creates a line of the run job file.  Sets the env variable
         * to the value of the parameter.
         * 
         * For eg.  If the env variable is IOAPI_GRIDNAME, and the 
         * shell type is csh, this will return
         * "setenv GRIDDESC US36_148x112"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other
         * languages, for example python, perl, etc.
         */
        
        if (false) throw new EmfException();
        String setenvLine = shellSetenv(parameter.getEnvVar().getName(), parameter.getValue());
        return setenvLine;
    }

    private void writeJobFile(CaseJob job, User user, File ofile) throws EmfException {
        /**
         * Writes a job run file w/ all necessary inputs and
         * parameters set.  It will also export the input files. 
         * 
         * Input: 
         *    job - the Case Job 
         *    user - the user
         *    ofile - the file to write the job script to
         *    
         * Output:
         *    ofile - the job run file- File obj
         */
        
        // Some objects needed for accessing data
        Case caseObj = this.getCase(job.getCaseId());
        DbServer dbServerlocal = dbFactory.getDbServer();
        DbServer dbServerlineexporter = dbFactory.getDbServer();
        int caseId = job.getCaseId();
        int jobId = job.getId();
        CaseInput headerInput = null;
        List <CaseInput> inputsAA = null;  // inputs for all sectors and all jobs
        List <CaseInput> inputsSA = null;  // inputs for specific sector and all jobs
        List <CaseInput> inputsAJ = null;  // inputs for all sectors specific jobs
        List <CaseInput> inputsSJ = null;  // inputs for specific sectors specific jobs        
        PrintWriter oStream = null;
        String jobName = job.getName().replace(" ", "_");


        try {

            /*
             * Get the inputs in the following order:
             *      all sectors, all jobs
             *      sector specific, all jobs
             *      all sectors, job specific
             *      sector specific, job specific
             *      
             *  Need to search for inputs now, b/c want to see if there
             *  is an EMF_HEADER
             */

            // Create an export service to get names of the datasets as inputs to Smoke script
            ExportService exports = new ExportService(dbServerlocal, this.threadPool, this.sessionFactory);


            // Get case inputs (the datasets associated w/ the case)
            // All sectors, all jobs
            inputsAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS);
            
            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            List <CaseInput> exclInputs = this.excludeInputsEnv(inputsAA, "EMF_JOBHEADER");
            
            if (exclInputs.size() != 0){
                headerInput = exclInputs.get(0);   // get the first element as header
            }

            // Sector specific, all jobs
            Sector sector = job.getSector();
            if ( sector != this.ALL_SECTORS ){
                inputsSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector);

                // Exclude any inputs w/ environmental variable EMF_JOBHEADER
                exclInputs = this.excludeInputsEnv(inputsSA, "EMF_JOBHEADER");
                if (exclInputs.size() != 0){
                    headerInput = exclInputs.get(0);   // get the first element as header
                }                
            }
            
            // All sectors, job specific
            inputsAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS);

            // Exclude any inputs w/ environmental variable EMF_JOBHEADER
            exclInputs = this.excludeInputsEnv(inputsAJ, "EMF_JOBHEADER");
            if (exclInputs.size() != 0){
                headerInput = exclInputs.get(0);   // get the first element as header
            }

            // Specific sector and specific job
            if ( sector != this.ALL_SECTORS ){
                inputsSJ = this.getJobInputs(caseId, jobId, sector);

                // Exclude any inputs w/ environmental variable EMF_JOBHEADER
                exclInputs = this.excludeInputsEnv(inputsSJ, "EMF_JOBHEADER");
                if (exclInputs.size() != 0){
                    headerInput = exclInputs.get(0);   // get the first element as header
                }
            }   

            // Write header to file:
            if (headerInput != null){
                // Setup a line exporter for the EMF_JOBHEADER dataset and export to the file
                // will close file, so need to reopen for appending, test that dbServer is closed
                //headerInput.getDataset().s
                LineExporter headerExporter = new LineExporter(headerInput.getDataset(), 
                        dbServerlineexporter, dbServerlineexporter.getSqlDataTypes(), new Integer(10000));
                
                try{
                    headerExporter.export(ofile);
                } catch (Exception e){
                    LOG.error("Could not write EMF header to job script file, " + e.getMessage());
                    throw new EmfException("Could not write EMF header to job script file");
                } finally{
                    try {
                        dbServerlineexporter.disconnect();
                    } catch (Exception e) {
                        throw new EmfException("dbServer error");
                    }
                }
                
                // Setup output stream to append to already created file
                try {
                    oStream = new PrintWriter(new FileWriter(ofile, true));
                } catch (Exception e) {
                    throw new EmfException("IO error writing to job run file: " + ofile);
                }

            } else {
            
                // setup new output stream to write to new file
                try {
                    oStream = new PrintWriter(new FileWriter(ofile));
                } catch (Exception e) {
                    throw new EmfException("IO error writing to job run file: " + ofile);
                }

                // print header info to job run file -- shell or program 
                oStream.println(this.runShell);
            }
            
            // print job name to file
            oStream.println();
            oStream.println(this.runComment + " Job run file for job: "+ jobName);
            
            /*
             * Define some EMF specific variables
             */
            oStream.println();
            
            oStream.println(this.runComment + " EMF specific variables");
            oStream.println(shellSetenv("EMF_JOBID", String.valueOf(jobId)));
            oStream.println(shellSetenv("EMF_JOBNAME", jobName));
            oStream.println(shellSetenv("EMF_USER", user.getUsername()));
            // Generate and get a unique job key and write it to the script
            job.generateJobkey(user);
            oStream.println(shellSetenv("EMF_JOBKEY",job.getJobkey()));


            // Print the inputs to the file

            /* 
            * loop over inputs and write Env variables and input 
            * (full name and path) to job run file, print comments
            */
            // All sectors and all jobs
            oStream.println();
            oStream.println(this.runComment + " Inputs -- for all sectors and all jobs");            
            for (CaseInput input : inputsAA) {
                oStream.println(setenvInput(input, caseObj, exports));
            }

            // Sector specific and all jobs 
            oStream.println();
            oStream.println(this.runComment + " Inputs -- sector (" + sector + ") and all jobs");
            for (CaseInput input : inputsSA) {
                oStream.println(setenvInput(input, caseObj, exports));
            }

            // All sectors and specific job
            oStream.println();
            oStream.println(this.runComment + " Inputs -- for all sectors and job: " + job);
            for (CaseInput input : inputsAJ) {
                oStream.println(setenvInput(input, caseObj, exports));
            }
            
            // Sector and Job specific
            oStream.println();
            oStream.println(this.runComment + " Inputs -- sector (" + sector + ") and job: " + job);
            for (CaseInput input : inputsSJ) {
                oStream.println(setenvInput(input, caseObj, exports));
            }
            

            /*
             * Get the parameters for this job in following order:
             *      all sectors, all jobs
             *      sector specific, all jobs
             *      all sectors, job specific
             *      sector specific, job specific
             */
            
            // All sectors, all jobs
            oStream.println();
            oStream.println(this.runComment + " Parameters -- all sectors, all jobs ");
            CaseParameter[] parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, this.ALL_SECTORS);
            for (CaseParameter param: parameters) {
                oStream.println(setenvParameter(param));
            }
            
            // Specific sector, all jobs
            if (sector != this.ALL_SECTORS){
                oStream.println();
                oStream.println(this.runComment + " Parameters -- sectors (" + sector +"), all jobs ");
                parameters = this.getJobParameters(caseId, this.ALL_JOB_ID, sector);
                for (CaseParameter param: parameters) {
                    oStream.println(setenvParameter(param));
                }
            }

            // All sectors, specific job
            oStream.println();
            oStream.println(this.runComment + " Parameters -- all sectors, job: " + job);
            parameters = this.getJobParameters(caseId, jobId, this.ALL_SECTORS);
            for (CaseParameter param: parameters) {
                oStream.println(setenvParameter(param));
            }
            
            // Specific sector, specific job
            if (sector != this.ALL_SECTORS){
                oStream.println();
                oStream.println(this.runComment + " Parameters -- sectors (" + sector +"), job: " + job);
                parameters = this.getJobParameters(caseId, jobId, sector);
                for (CaseParameter param: parameters) {
                    oStream.println(setenvParameter(param));
                }
            }

            // Getting the executable object from the job
            Executable execVal = job.getExecutable();

            // path to executable
            String execPath = job.getPath();

            // executable string
            String execName = execVal.getName();

            // executable full name and arguments
            String execFull = execPath + System.getProperty("file.separator") 
                        + execName + " "  + job.getArgs();

            // print executable
            oStream.println();
            oStream.println(this.runComment + " job executable");
            oStream.println(execFull);

            // add a test of the status and send info through the 
            // command client -- should generalize so not csh specific
            oStream.println("if ( $status != 0 ) then");
            oStream.println("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Failed' -m \"ERROR running Job: $EMF_JOBNAME\"");
            oStream.println("\t exit(1)");
            oStream.println("else");
            oStream.println("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Completed' -m \"Completed job: $EMF_JOBNAME\"");
            oStream.println("endif");
            
            // close output stream to file and make it executable
            oStream.close();
            ofile.setExecutable(true);
            
        } finally {
            // close the db server
            try {
                dbServerlocal.disconnect();
            } catch (Exception e) {
                throw new EmfException("dbServer error");
            }

        }

    }
    
    public Executable addExecutable(Executable exe) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.exeutableExists(session, exe))
                dao.add(exe, session);

            return (Executable) dao.load(Executable.class, exe.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new executable '" + exe.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new executable '" + exe.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(envVar, session);
            return (ParameterEnvVar) dao.load(ParameterEnvVar.class, envVar.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new parameter env variable '" + envVar.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter env variable '" + envVar.getName() + "'");
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
            LOG.error("Could not get all parameter env variables.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter env variables.\n");
        } finally {
            session.close();
        }
    }

    public ValueType addValueType(ValueType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addValueType(type, session);
            return (ValueType) dao.load(ValueType.class, type.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new value type '" + type.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new value type '" + type.getName() + "'");
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
            LOG.error("Could not get all value types.\n" + e.getMessage());
            throw new EmfException("Could not get all value types.\n");
        } finally {
            session.close();
        }
    }

    public ParameterName addParameterName(ParameterName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addParameterName(name, session);
            return (ParameterName) dao.load(ParameterName.class, name.getName(), session);
        } catch (Exception e) {
            LOG.error("Could not add new parameter name '" + name.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new parameter name '" + name.getName() + "'");
        } finally {
            session.close();
        }
    }

    public ParameterName[] getParameterNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterName> type = dao.getParameterNames(session);

            return type.toArray(new ParameterName[0]);
        } catch (Exception e) {
            LOG.error("Could not get all parameter names.\n" + e.getMessage());
            throw new EmfException("Could not get all parameter names.\n");
        } finally {
            session.close();
        }
    }

    public CaseParameter addCaseParameter(CaseParameter param) throws EmfException {
        Session session = sessionFactory.getSession();

        if (dao.caseParameterExists(param, session))
            throw new EmfException(
                    "The combination of 'Parameter Name', 'Sector', 'Program', and 'Job' should be unique.");

        try {
            dao.addParameter(param, session);
            return (CaseParameter) dao.loadCaseParameter(param, session);
        } catch (Exception e) {
            LOG.error("Could not add new case parameter '" + param.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case parameter '" + param.getName() + "'");
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
            LOG.error("Could not get all parameters for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public void removeCaseParameters(CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseParameters(params, session);
        } catch (Exception e) {
            LOG.error("Could not remove case parameter " + params[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case parameter " + params[0].getName() + " etc.");
        } finally {
            session.close();
        }
    }

    public void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseParameter loaded = (CaseParameter) dao.loadCaseParameter(parameter, session);

            if (loaded != null && loaded.getId() != parameter.getId())
                throw new EmfException("Case parameter uniqueness check failed (" + loaded.getId() + ","
                        + parameter.getId() + ")");

            session.clear();
            dao.updateCaseParameter(parameter, session);
            setStatus(user, "Saved parameter " + parameter.getName() + " to database.", "Save Parameter");
        } catch (RuntimeException e) {
            LOG.error("Could not update case parameter: " + parameter.getName() + ".\n" + e);
            throw new EmfException("Could not update case parameter: " + parameter.getName() + ".");
        } finally {
            session.close();
        }
    }

    public void runJobs(CaseJob[] jobs, User user) throws EmfException {
        /**
         * Runs the jobs selected for this case.
         * 
         * Inputs: array of jobs
         * 
         * user
         */

        // need to create this user obj as final b.c. passed to threading
        final User localUser = user;
        
        // fill in access to jobTaskManager-- for now just loop over the 
        // the jobs
        for (CaseJob job : jobs){
            try{
                // set status as submitted and run the individual job
                setStatus(localUser, "Job " + job.getName() + " submitted for case " + getCase(job.getCaseId()) + ".", "Run Job");
                runJob(job, user);
            } catch (Exception e) {
                LOG.error("Could not run case job " + job.getName() + ".", e);
                throw new EmfException(e.getMessage());
            }   
                            
            
        }
        
        // loop over jobs and spawn a seperate thread for each -- old code
//        for (final CaseJob job : jobs) {
//            System.out.println("runJobs-- job: "+ job);
//            try {
//                threadPool.execute(new Runnable() {
//                    public void run() {
//                        // defining what the thread will run
//                        try {
//                         	setStatus(localUser, "Job " + job.getName() + " submitted for case " + getCase(job.getCaseId()) + ".", "Run Job");
//                            runJob(job);
//                        } catch (Exception e) {
//                            LOG.error("Could not run case job " + job.getName() + ".", e);
//                            //throw new EmfException(e.getMessage());
//                        }
//                    }
//                });
//            } catch (Exception e) {
//                throw new EmfException(e.getMessage());
//            }
//        }

    }
}

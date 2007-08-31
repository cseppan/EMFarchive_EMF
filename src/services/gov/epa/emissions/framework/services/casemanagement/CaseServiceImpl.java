package gov.epa.emissions.framework.services.casemanagement;

import java.util.Date;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseServiceImpl implements CaseService {
    private static Log log = LogFactory.getLog(CaseServiceImpl.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;

    private ManagedCaseService caseService;

    // all sectors and all jobs id in the case inputs tb
    // private static Sector ALL_SECTORS = null;
    //
    // private static int ALL_JOB_ID = 0;

    // FIXME: What is this next constructor for?
    public CaseServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public CaseServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServer = dbFactory.getDbServer();
        System.out.println("CaseServiceImpl::getCaseService  Is dBServer null? " + (dbServer==null));
        System.out.println("CaseServiceImpl::getCaseService  Is sessionFactory null? " + (sessionFactory==null));

        myTag();
        if (DebugLevels.DEBUG_1)
            System.out.println(myTag());

    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    private synchronized ManagedCaseService getCaseService() {
        log.info("CaseServiceImpl::getCaseService");

        if (caseService == null) {
            try {
                
                caseService = new ManagedCaseService(dbServer, sessionFactory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.caseService;
    }

    public void addCase(Case element) throws EmfException {
        getCaseService().addCase(element);
    }

    public void removeCase(Case caseObj) throws EmfException {
        getCaseService().removeCase(caseObj);
    }

    public Case[] getCases() throws EmfException {
        return getCaseService().getCases();
    }

    // private Case getCase(int caseId) throws EmfException {
    // return getCaseService().getCase(caseId);
    // }

    public Abbreviation[] getAbbreviations() throws EmfException {
        return getCaseService().getAbbreviations();
    }

    public AirQualityModel[] getAirQualityModels() throws EmfException {
        return getCaseService().getAirQualityModels();
    }

    public CaseCategory[] getCaseCategories() throws EmfException {
        return getCaseService().getCaseCategories();
    }

    public EmissionsYear[] getEmissionsYears() throws EmfException {
        return getCaseService().getEmissionsYears();
    }

    public Grid[] getGrids() throws EmfException {
        return getCaseService().getGrids();
    }

    public GridResolution[] getGridResolutions() throws EmfException {
        return getCaseService().getGridResolutions();
    }

    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        return getCaseService().getMeteorlogicalYears();
    }

    public Speciation[] getSpeciations() throws EmfException {
        return getCaseService().getSpeciations();
    }

    public Case obtainLocked(User owner, Case element) throws EmfException {
        return getCaseService().obtainLocked(owner, element);
    }

    public Case releaseLocked(Case locked) throws EmfException {
        return getCaseService().releaseLocked(locked);
    }

    public Case updateCase(Case element) throws EmfException {
        return getCaseService().updateCase(element);
    }

    public InputName[] getInputNames() throws EmfException {
        return getCaseService().getInputNames();
    }

    public InputEnvtVar[] getInputEnvtVars() throws EmfException {
        return getCaseService().getInputEnvtVars();
    }

    public CaseProgram[] getPrograms() throws EmfException {
        return getCaseService().getPrograms();
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        return getCaseService().getModelToRuns();
    }

    public void export(User user, String dirName, String purpose, boolean overWrite, int caseId) throws EmfException {
        if (false)
            throw new EmfException("");
        System.out.println("Called CaseServiceImpl::export() caseId= " + caseId);

        // getCaseService().exportInputsForCase(user, dirName, purpose, overWrite, caseId);
    }

    public InputName addCaseInputName(InputName name) throws EmfException {
        return getCaseService().addCaseInputName(name);
    }

    public CaseProgram addProgram(CaseProgram program) throws EmfException {
        return getCaseService().addProgram(program);
    }

    public InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        return getCaseService().addInputEnvtVar(inputEnvtVar);
    }

    public ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        return getCaseService().addModelToRun(model);
    }

    public GridResolution addGridResolution(GridResolution gridResolution) throws EmfException {
        return getCaseService().addGridResolution(gridResolution);
    }

    public SubDir[] getSubDirs() throws EmfException {
        return getCaseService().getSubDirs();

    }

    public SubDir addSubDir(SubDir subdir) throws EmfException {
        return getCaseService().addSubDir(subdir);
    }

    public CaseInput addCaseInput(CaseInput input) throws EmfException {
        return getCaseService().addCaseInput(input);
    }

    public void updateCaseInput(User user, CaseInput input) throws EmfException {
        getCaseService().updateCaseInput(user, input);
    }

    public void removeCaseInputs(CaseInput[] inputs) throws EmfException {
        getCaseService().removeCaseInputs(inputs);
    }

    public CaseInput[] getCaseInputs(int caseId) throws EmfException {
        return getCaseService().getCaseInputs(caseId);
    }

    public Case[] copyCaseObject(int[] toCopy) throws EmfException {
        return getCaseService().copyCaseObject(toCopy);
    }

    public CaseJob addCaseJob(CaseJob job) throws EmfException {
        return getCaseService().addCaseJob(job);
    }

    public CaseJob[] getCaseJobs(int caseId) throws EmfException {
        return getCaseService().getCaseJobs(caseId);
    }

    public CaseJob getCaseJob(int jobId) throws EmfException {
        return getCaseService().getCaseJob(jobId);
    }

    public JobRunStatus[] getJobRunStatuses() throws EmfException {
        return getCaseService().getJobRunStatuses();
    }

    public Executable[] getExecutables(int casejobId) throws EmfException {
        return getCaseService().getExecutables(casejobId);
    }

    public void removeCaseJobs(CaseJob[] jobs) throws EmfException {
        getCaseService().removeCaseJobs(jobs);
    }

    public void updateCaseJob(User user, CaseJob job) throws EmfException {
        getCaseService().updateCaseJob(user, job);
    }

    public Host[] getHosts() throws EmfException {
        return getCaseService().getHosts();
    }

    public Host addHost(Host host) throws EmfException {
        return getCaseService().addHost(host);
    }

    public String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        System.out.println("Called CaseServiceImpl::runJobs with Integer[] jobIds size of array= " + jobIds.length);
System.out.println("runJobs for caseId=" + caseId + " and submitted by User= " + user.getUsername());
        // submit the list of CaseJobs to the ManagedService
        return caseService.submitJobs(jobIds, caseId, user);
    }

    public void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        getCaseService().updateCaseParameter(user, parameter);
    }

    public Executable addExecutable(Executable exe) throws EmfException {
        return getCaseService().addExecutable(exe);
    }

    public ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        return getCaseService().addParameterEnvVar(envVar);
    }

    public ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        return getCaseService().getParameterEnvVars();
    }

    public ValueType addValueType(ValueType type) throws EmfException {
        return getCaseService().addValueType(type);
    }

    public ValueType[] getValueTypes() throws EmfException {
        return getCaseService().getValueTypes();
    }

    public ParameterName addParameterName(ParameterName name) throws EmfException {
        return getCaseService().addParameterName(name);
    }

    public ParameterName[] getParameterNames() throws EmfException {
        return getCaseService().getParameterNames();
    }

    public CaseParameter addCaseParameter(CaseParameter param) throws EmfException {
        return getCaseService().addCaseParameter(param);
    }

    public CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        return getCaseService().getCaseParameters(caseId);
    }

    public void removeCaseParameters(CaseParameter[] params) throws EmfException {
        getCaseService().removeCaseParameters(params);
    }

    public void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {
        System.out.println("CaseServiceImpl::exportCaseInputs Total inputs size for export= " + caseInputIds.length);
         getCaseService().exportCaseInputs(user, caseInputIds, purpose);
    }

    public void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose) throws EmfException {
        System.out.println("CaseServiceImpl::exportCaseInputsWithOverwrite Total inputs size for export= "
                + caseInputIds.length);
        getCaseService().exportCaseInputsWithOverwrite(user, caseInputIds, purpose);

    }

    // ****************************************************************

    // ****************************
    public void submitJob(CaseJob job, User user, Case jobCase) throws EmfException {
        /**
         * runs the job.
         * 
         * Creates a job run file which sets up the environment including input files and parameters for the run script
         * defines the run script, creates a log file, exports the inputs, and then executes it.
         */

        if (false)
            throw new EmfException("");
        // FIXME: block commenst for test of Jobsubmitter
        /*
         * String executionStr = null; String logFileName = null; // Job run file name File ofile = this.getOfile(job); //
         * Create job script writeJobFile(job, user, ofile); // get log file for job script try { logFileName =
         * getLog(ofile); } catch (Exception e) { e.printStackTrace(); throw new EmfException(e.getMessage()); }
         * 
         */
        // FIXME: block ABOVE comments for test of Jobsubmitter
        // Export all inputs
        // List<CaseInput> inputs = caseService.getAllJobInputs(job);
        // System.out.println("Number of inputs for this job: " + inputs.size());
        // FIXME: Need to flesh this string out
        // purpose = "Used by CaseName and JobName"
        String purpose = "Used by " + job.getName() + " of Case " + jobCase.getName();

        if (DebugLevels.DEBUG_6)
            System.out.println("User: " + user.getName());
        if (DebugLevels.DEBUG_6)
            System.out.println("Purpose: " + purpose);
        if (DebugLevels.DEBUG_6)
            System.out.println("Job: " + job.getName());
        if (DebugLevels.DEBUG_6)
            System.out.println("Job Case: " + jobCase.getName());
        // if (DebugLevels.DEBUG_6)
        // System.out.println("Case Inputs null? : " + (inputs == null));

        // pass the inputs to the exportJobSubmitter
        // String caseJobSubmitterId = exportService.exportForJob(user, inputs, purpose, job, jobCase);

        // if (DebugLevels.DEBUG_6)
        // System.out.println("Submitter Id for case job:" + caseJobSubmitterId);

        // FIXME: Commented out for job submitter
        // Create an execution string and submit job to the queue,
        // if the key word $EMF_JOBLOG is in the queue options,
        // replace w/ log file
        // String queueOptions = job.getQueOptions();
        // String queueOptionsLog = queueOptions.replace("$EMF_JOBLOG", logFileName);
        // if (queueOptionsLog.equals("")) {
        // executionStr = ofile.toString();
        // } else {
        // executionStr = queueOptionsLog + " " + ofile;
        // }
        //
        // /*
        // * execute the job script Note if hostname is localhost this is done locally w/o ssh and stdout and stderr is
        // * redirected to the log. This redirect is currently shell specific (should generalize) if hostname is not
        // * localhost it is through ssh
        // */
        // String username = user.getUsername();
        // String hostname = job.getHost().getName();
        // if (hostname.equals("localhost")) {
        // // execute on local machine
        // executionStr = executionStr + " " + this.runRedirect + " " + logFileName;
        // log.warn("Local Job execution string: " + executionStr);
        // RemoteCommand.executeLocal(executionStr);
        // } else {
        // // execute on remote machine and log stdout
        // InputStream inStream = RemoteCommand.execute(username, hostname, executionStr);
        // String outTitle = "stdout from (" + hostname + "): " + executionStr;
        // RemoteCommand.logStdout(outTitle, inStream);
        // }

    }

    // ****************************

    public void runJobs(CaseJob[] jobs, User user) throws EmfException {

        // need to create this user obj as final b.c. passed to threading
        // final User localUser = user;
        if (false)
            throw new EmfException("");
        // fill in access to jobTaskManager-- for now just loop over the
        // the jobs
        // for (CasseJob job : jobs) {
        // try {
        // Case jobCase = getCase(job.getCaseId());
        //
        // // set status as submitted and run the individual job
        // // setStatus(localUser, "Job " + job.getName() + " submitted for case " + jobCase + ".", "Run Job");
        // submitJob(job, user, jobCase);
        // } catch (Exception e) {
        // log.error("Could not run case job " + job.getName() + ".", e);
        // throw new EmfException(e.getMessage());
        // }
        //
        // }

        System.out.println("Called CaseServiceImpl::runJobs with CaseJob[] size of CaseJobs= " + jobs.length);
    }

    // for command line client
    public int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        return getCaseService().recordJobMessage(message, jobKey);
    }

    public JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        return getCaseService().getJobMessages(caseId, jobId);
    }

}

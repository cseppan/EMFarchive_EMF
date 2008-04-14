package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJobKey;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.outputs.QueueCaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    private LockingScheme lockingScheme;

    private HibernateSessionFactory sessionFactory;

    public CaseDAO(HibernateSessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
        daoInit();
    }

    public CaseDAO() {
        daoInit();
    }

    private void daoInit() {
        hibernateFacade = new HibernateFacade();
        lockingScheme = new LockingScheme();
    }

    public void add(JobMessage message) {
        Session session = sessionFactory.getSession();
        try {
            hibernateFacade.add(message, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
    }

    public CaseOutput add(CaseOutput output) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            hibernateFacade.add(output, session);

            return getCaseOutput(output, session);
        } catch (Exception ex) {
            throw new Exception("Problem adding case output: " + output.getName() + ". " + ex.getMessage());
        } finally {
            session.close();
        }
    }

    public CaseOutput updateCaseOutput(CaseOutput output) {
        Session session = sessionFactory.getSession();
        CaseOutput toReturn = null;

        try {
            hibernateFacade.updateOnly(output, session);
            toReturn = getCaseOutput(output, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return toReturn;
    }

    public CaseOutput updateCaseOutput(Session session, CaseOutput output) {
        hibernateFacade.updateOnly(output, session);
        return getCaseOutput(output, session);
    }

    public boolean caseOutputNameUsed(String outputName) {
        Session session = sessionFactory.getSession();
        List outputs = null;

        try {
            Criterion criterion = Restrictions.eq("name", outputName);
            outputs = hibernateFacade.get(CaseOutput.class, criterion, session);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return (outputs != null && outputs.size() > 0);
    }

    public void add(Executable exe, Session session) {
        addObject(exe, session);
    }

    public void add(SubDir subdir, Session session) {
        addObject(subdir, session);
    }

    public void add(AirQualityModel object, Session session) {
        addObject(object, session);
    }

    public void add(GridResolution object, Session session) {
        addObject(object, session);
    }

    public void add(CaseCategory object, Session session) {
        addObject(object, session);
    }

    public void add(EmissionsYear object, Session session) {
        addObject(object, session);
    }

    public void add(Grid object, Session session) {
        addObject(object, session);
    }

    public void add(MeteorlogicalYear object, Session session) {
        addObject(object, session);
    }

    public void add(Speciation object, Session session) {
        addObject(object, session);
    }

    public void add(CaseProgram object, Session session) {
        addObject(object, session);
    }

    public void add(InputName object, Session session) {
        addObject(object, session);
    }

    public void add(InputEnvtVar object, Session session) {
        addObject(object, session);
    }

    public void add(ModelToRun object, Session session) {
        addObject(object, session);
    }

    public void add(Case object, Session session) {
        addObject(object, session);
    }

    public void add(CaseInput object, Session session) {
        addObject(object, session);
    }

    public void add(CaseOutput object, Session session) {
        addObject(object, session);
    }

    public void add(Host object, Session session) {
        addObject(object, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List getAbbreviations(Session session) {
        return hibernateFacade.getAll(Abbreviation.class, Order.asc("name"), session);
    }

    public Abbreviation getAbbreviation(Abbreviation abbr, Session session) {
        Criterion criterion = Restrictions.eq("name", abbr.getName());

        return (Abbreviation) hibernateFacade.load(Abbreviation.class, criterion, session);
    }

    public List getAirQualityModels(Session session) {
        return hibernateFacade.getAll(AirQualityModel.class, Order.asc("name"), session);
    }

    public List getCaseCategories(Session session) {
        return hibernateFacade.getAll(CaseCategory.class, Order.asc("name"), session);
    }

    public List getEmissionsYears(Session session) {
        return hibernateFacade.getAll(EmissionsYear.class, Order.asc("name"), session);
    }

    public List getGrids(Session session) {
        return hibernateFacade.getAll(Grid.class, Order.asc("name"), session);
    }

    public List getGridResolutions(Session session) {
        return hibernateFacade.getAll(GridResolution.class, Order.asc("name"), session);
    }

    public List getMeteorlogicalYears(Session session) {
        return hibernateFacade.getAll(MeteorlogicalYear.class, Order.asc("name"), session);
    }

    public List getSpeciations(Session session) {
        return hibernateFacade.getAll(Speciation.class, Order.asc("name"), session);
    }

    public List getCases(Session session) {
        return hibernateFacade.getAll(Case.class, Order.asc("name"), session);
    }

    public Case getCase(int caseId, Session session) {
        Criterion criterion = Restrictions.eq("id", new Integer(caseId));
        return (Case) hibernateFacade.get(Case.class, criterion, session).get(0);
    }

    public Case getCaseFromAbbr(Abbreviation abbr, Session session) {
        Criterion crit = Restrictions.eq("abbreviation", abbr);
        return (Case) hibernateFacade.load(Case.class, crit, session);
    }

    public List getPrograms(Session session) {
        return hibernateFacade.getAll(CaseProgram.class, Order.asc("name"), session);
    }

    public List getInputNames(Session session) {
        return hibernateFacade.getAll(InputName.class, Order.asc("name"), session);
    }

    public List getInputEnvtVars(Session session) {
        return hibernateFacade.getAll(InputEnvtVar.class, Order.asc("name"), session);
    }

    public void remove(Case element, Session session) {
        hibernateFacade.remove(element, session);
    }

    public void removeCaseInputs(CaseInput[] inputs, Session session) {
        hibernateFacade.remove(inputs, session);
    }

    public void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset, Session session)
            throws EmfException {
        try {
            if (deleteDataset)
                removeDatasetsOnOutput(user, session, outputs);
        } finally {
            hibernateFacade.remove(outputs, session);
        }
    }

    public void removeCaseOutputs(CaseOutput[] outputs, Session session) {
        hibernateFacade.removeObjects(outputs, session);
    }

    public Case obtainLocked(User owner, Case element, Session session) {
        return (Case) lockingScheme.getLocked(owner, current(element, session), session);
    }

    public Case releaseLocked(User owner, Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(owner, current(locked, session), session);
    }

    public Case forceReleaseLocked(Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(current(locked, session), session);
    }

    public Case update(Case locked, Session session) throws EmfException {
        return (Case) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    private Case current(Case caze, Session session) {
        return (Case) hibernateFacade.current(caze.getId(), Case.class, session);
    }

    private Case current(int id, Class clazz, Session session) {
        return (Case) hibernateFacade.current(id, clazz, session);
    }

    public boolean caseInputExists(CaseInput input, Session session) {
        Criterion[] criterions = uniqueCaseInputCriteria(input);

        return hibernateFacade.exists(CaseInput.class, criterions, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    private Criterion[] uniqueCaseInputCriteria(CaseInput input) {
        Integer caseId = new Integer(input.getCaseID());
        InputName inputname = input.getInputName();
        Sector sector = input.getSector();
        CaseProgram program = input.getProgram();
        Integer jobID = new Integer(input.getCaseJobID());

        Criterion c1 = Restrictions.eq("caseID", caseId);
        Criterion c2 = (inputname == null) ? Restrictions.isNull("inputName") : Restrictions.eq("inputName", inputname);
        Criterion c3 = (sector == null) ? Restrictions.isNull("sector") : Restrictions.eq("sector", sector);
        Criterion c4 = (program == null) ? Restrictions.isNull("program") : Restrictions.eq("program", program);
        Criterion c5 = Restrictions.eq("caseJobID", jobID);

        return new Criterion[] { c1, c2, c3, c4, c5 };
    }

    public Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }

    public Object load(Class clazz, int id, Session session) {
        Criterion criterion = Restrictions.eq("id", new Integer(id));
        return hibernateFacade.load(clazz, criterion, session);
    }

    public Object loadCaseInput(CaseInput input, Session session) {
        Criterion[] criterions = uniqueCaseInputCriteria(input);

        return hibernateFacade.load(CaseInput.class, criterions, session);
    }

    public List getCaseInputs(int caseId, Sector sector, boolean showAll, Session session) {
        if (sector == null && showAll)
            return getCaseInputs(caseId, session);
        
        if (sector == null && !showAll)
            return getCaseInputsWithShow(caseId, session);
        
        if (sector != null && showAll)
            return getCaseInputsWithSector(caseId, sector, session);
        
        Criterion crit1 = Restrictions.eq("caseID", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("sector", sector);
        Criterion crit3 = Restrictions.eq("show", true);
        
        return hibernateFacade.get(CaseInput.class, new Criterion[]{ crit1, crit2, crit3}, session);
    }
    
    public List getCaseInputs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseID", new Integer(caseId));

        return hibernateFacade.get(CaseInput.class, crit, session);
    }
    
    private List getCaseInputsWithShow(int caseId, Session session) {
        Criterion crit1 = Restrictions.eq("caseID", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("show", true);
        
        return hibernateFacade.get(CaseInput.class, new Criterion[]{ crit1, crit2}, session);
    }

    private List getCaseInputsWithSector(int caseId, Sector sector, Session session) {
        Criterion crit1 = Restrictions.eq("caseID", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("sector", sector);
        
        return hibernateFacade.get(CaseInput.class, new Criterion[]{ crit1, crit2}, session);
    }

    public List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, Session session) {
        /**
         * Gets inputs for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = new Integer(caseId);
        Integer jobID = new Integer(jobId);

        // setup the 3 criteria
        Criterion c1 = Restrictions.eq("caseID", caseID);
        Criterion c2 = (sector == null) ? Restrictions.isNull("sector") : Restrictions.eq("sector", sector);
        Criterion c3 = Restrictions.eq("caseJobID", jobID);
        Criterion[] criterions = { c1, c2, c3 };

        // query the db using hibernate for the inputs that
        // match the criterias
        // what is the difference b/w hibernate get and getAll
        return hibernateFacade.get(CaseInput.class, criterions, session);

    }

    public List getJobParameters(int caseId, int jobId, Sector sector, Session session) {
        /**
         * Gets parameters for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = new Integer(caseId);
        Integer jobID = new Integer(jobId);

        // setup the 3 criteria
        Criterion c1 = Restrictions.eq("caseID", caseID);
        Criterion c2 = (sector == null) ? Restrictions.isNull("sector") : Restrictions.eq("sector", sector);
        Criterion c3 = Restrictions.eq("jobId", jobID);
        Criterion[] criterions = { c1, c2, c3 };

        // query the db using hibernate for the parameters that
        // match the criterias
        return hibernateFacade.get(CaseParameter.class, criterions, session);

    }

    public List getAllCaseInputs(Session session) {
        return hibernateFacade.getAll(CaseInput.class, Order.asc("id"), session);
    }

    public void updateCaseInput(CaseInput input, Session session) {
        hibernateFacade.updateOnly(input, session);
    }

    public List getModelToRuns(Session session) {
        return hibernateFacade.getAll(ModelToRun.class, Order.asc("name"), session);
    }

    public List getSubDirs(Session session) {
        return hibernateFacade.getAll(SubDir.class, Order.asc("name"), session);
    }

    public void add(CaseJob job, Session session) {
        addObject(job, session);
    }

    public List<CaseJob> getCaseJobs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseId", new Integer(caseId));

        return hibernateFacade.get(CaseJob.class, crit, session);
    }

    public List<CaseJob> getCaseJobs(int caseId) {
        Session session = sessionFactory.getSession();
        List<CaseJob> jobs = null;

        try {
            Criterion crit = Restrictions.eq("caseId", new Integer(caseId));
            jobs = hibernateFacade.get(CaseJob.class, crit, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return jobs;
    }

    public CaseJob getCaseJob(String jobKey) {
        Session session = sessionFactory.getSession();
        CaseJob job = null;

        try {
            Criterion crit = Restrictions.eq("jobkey", jobKey);
            job = (CaseJob) hibernateFacade.load(CaseJob.class, crit, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return job;
    }

    public List<JobMessage> getJobMessages(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseId", new Integer(caseId));

        return hibernateFacade.get(JobMessage.class, crit, session);
    }

    public List<JobMessage> getJobMessages(int caseId, int jobId, Session session) {
        Criterion crit1 = Restrictions.eq("caseId", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("jobId", new Integer(jobId));

        return hibernateFacade.get(JobMessage.class, new Criterion[] { crit1, crit2 }, session);
    }

    public CaseJob getCaseJob(int jobId, Session session) {
        Criterion crit = Restrictions.eq("id", new Integer(jobId));
        return (CaseJob) hibernateFacade.load(CaseJob.class, crit, session);
    }

    public CaseJob getCaseJob(int jobId) {
        CaseJob caseJob = null;
        Session session = sessionFactory.getSession();
        try {
            Criterion crit = Restrictions.eq("id", new Integer(jobId));
            caseJob = (CaseJob) hibernateFacade.load(CaseJob.class, crit, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        return caseJob;
    }

    public CaseJob getCaseJob(int caseId, CaseJob job, Session session) {
        Criterion crit1 = Restrictions.eq("caseId", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("name", job.getName());

        return (CaseJob) hibernateFacade.load(CaseJob.class, new Criterion[] { crit1, crit2 }, session);
    }

    public void updateCaseJob(CaseJob job) {
        Session session = sessionFactory.getSession();
        try {
            hibernateFacade.updateOnly(job, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void updateCaseJob(CaseJob job, Session session) throws Exception {
        try {
            hibernateFacade.updateOnly(job, session);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }
    
    public List getCaseJobKey(int jobId, Session session) {
        Criterion criterion = Restrictions.eq("jobId", new Integer(jobId));
        return hibernateFacade.get(CaseJobKey.class, criterion, session);
    }
    
    public CaseJob getCaseJobFromKey(String key) {
        Session session = sessionFactory.getSession();
        CaseJob job = null;
        
        try {
            Criterion criterion = Restrictions.eq("key", key);
            List<CaseJobKey> keyObjs = hibernateFacade.get(CaseJobKey.class, criterion, session);
            
            if (keyObjs == null || keyObjs.size() == 0)
                return null;
            
            job = getCaseJob(keyObjs.get(0).getJobId(), session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        
        return job;
    }

    public void updateCaseJobKey(int jobId, String jobKey, Session session) throws Exception {
        try {
            List keys = getCaseJobKey(jobId, session);
            CaseJobKey keyObj = (keys == null || keys.size() == 0) ? null : (CaseJobKey)keys.get(0);
            
            if (keyObj == null) {
                addObject(new CaseJobKey(jobKey, jobId), session);
                return;
            }
            
            if (keyObj.getKey().equals(jobKey))
                return;
            
            keyObj.setKey(jobKey);
            hibernateFacade.updateOnly(keyObj, session);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public List<JobRunStatus> getJobRunStatuses(Session session) {
        return hibernateFacade.getAll(JobRunStatus.class, Order.asc("name"), session);
    }

    public JobRunStatus getJobRunStatuse(String status, Session session) {
        Criterion crit = Restrictions.eq("name", status);
        return (JobRunStatus) hibernateFacade.load(JobRunStatus.class, crit, session);
    }

    public JobRunStatus getJobRunStatuse(String status) {
        if (DebugLevels.DEBUG_9)
            System.out
                    .println("In CaseDAO::getJobRunStatuse: Is the session Factory null? " + (sessionFactory == null));

        Session session = sessionFactory.getSession();
        JobRunStatus jrs = null;

        try {
            Criterion crit = Restrictions.eq("name", status);
            jrs = (JobRunStatus) hibernateFacade.load(JobRunStatus.class, crit, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return jrs;

    }

    public List<Host> getHosts(Session session) {
        return hibernateFacade.getAll(Host.class, Order.asc("name"), session);
    }

    public List<Executable> getExecutables(Session session) {
        return hibernateFacade.getAll(Executable.class, Order.asc("name"), session);
    }

    public boolean exeutableExists(Session session, Executable exe) {
        return hibernateFacade.exists(exe.getName(), Executable.class, session);
    }

    public void add(Abbreviation element, Session session) {
        addObject(element, session);
    }

    public void add(ParameterEnvVar envVar, Session session) {
        addObject(envVar, session);
    }

    public List<ParameterEnvVar> getParameterEnvVars(Session session) {
        return hibernateFacade.getAll(ParameterEnvVar.class, session);
    }

    public void addValueType(ValueType type, Session session) {
        addObject(type, session);
    }

    public List<ValueType> getValueTypes(Session session) {
        return hibernateFacade.getAll(ValueType.class, session);
    }

    public void addParameterName(ParameterName name, Session session) {
        addObject(name, session);
    }

    public List<ParameterName> getParameterNames(Session session) {
        return hibernateFacade.getAll(ParameterName.class, session);
    }

    public void addParameter(CaseParameter param, Session session) {
        addObject(param, session);
    }

    public Object loadCaseParameter(CaseParameter param, Session session) {
        Criterion[] criterions = uniqueCaseParameterCriteria(param);

        return hibernateFacade.load(CaseParameter.class, criterions, session);
    }

    private Criterion[] uniqueCaseParameterCriteria(CaseParameter param) {
        Integer caseId = new Integer(param.getCaseID());
        ParameterName paramname = param.getParameterName();
        Sector sector = param.getSector();
        CaseProgram program = param.getProgram();
        Integer jobID = new Integer(param.getJobId());

        Criterion c1 = Restrictions.eq("caseID", caseId);
        Criterion c2 = (paramname == null) ? Restrictions.isNull("parameterName") : Restrictions.eq("parameterName",
                paramname);
        Criterion c3 = (sector == null) ? Restrictions.isNull("sector") : Restrictions.eq("sector", sector);
        Criterion c4 = (program == null) ? Restrictions.isNull("program") : Restrictions.eq("program", program);
        Criterion c5 = Restrictions.eq("jobId", jobID);

        return new Criterion[] { c1, c2, c3, c4, c5 };
    }

    public List<CaseParameter> getCaseParameters(int caseId, Session session) {

        Criterion crit = Restrictions.eq("caseID", new Integer(caseId));

        return hibernateFacade.get(CaseParameter.class, crit, session);
    }

    public boolean caseParameterExists(CaseParameter param, Session session) {
        Criterion[] criterions = uniqueCaseParameterCriteria(param);

        return hibernateFacade.exists(CaseParameter.class, criterions, session);
    }

    public void updateCaseParameter(CaseParameter parameter, Session session) {
        hibernateFacade.updateOnly(parameter, session);
    }

    public Object loadCaseJobByName(CaseJob job) {
        Session session = sessionFactory.getSession();
        Object obj = null;
        try {
            Criterion c1 = Restrictions.eq("caseId", new Integer(job.getCaseId()));
            Criterion c2 = Restrictions.eq("name", job.getName());
            Criterion[] criterions = { c1, c2 };
            obj = hibernateFacade.load(CaseJob.class, criterions, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        return obj;

    }

    public void removeCaseJobs(CaseJob[] jobs, Session session) {
        hibernateFacade.remove(jobs, session);
    }

    public void removeCaseParameters(CaseParameter[] params, Session session) {
        hibernateFacade.remove(params, session);
    }

    public CaseInput getCaseInput(int inputId, Session session) {
        Criterion crit = Restrictions.eq("id", new Integer(inputId));
        return (CaseInput) hibernateFacade.load(CaseInput.class, crit, session);
    }

    // private boolean loopDepends(int jid, DependentJob job) {
    // boolean loopDetected = false;
    //
    // return loopDetected;
    // }

    public String[] getAllValidJobs(int jobId) {
        List<String> validJobNames = new ArrayList<String>();
        int caseId = getCaseJob(jobId).getCaseId();
        List<CaseJob> jobs = getCaseJobs(caseId);

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();

            if (canDependOn(jobId, job.getId()))
                validJobNames.add(job.getName());
        }

        return validJobNames.toArray(new String[0]);
    }

    public String[] getDependentJobs(int jobId) {
        DependentJob[] dependentJobs = getCaseJob(jobId).getDependentJobs();
        String[] dependentJobNames = new String[dependentJobs.length];

        for (int i = 0; i < dependentJobs.length; i++) {
            int id = dependentJobs[i].getJobId();
            dependentJobNames[i] = getCaseJob(id).getName();
        }

        return dependentJobNames;
    }

    private boolean canDependOn(int jobId, int dependentJobId) {
        // FIXME: this really should be a recursive check on all the possible dependencies
        // to avoid cycle dependencies.
        if (jobId == dependentJobId)
            return false;

        CaseJob job = getCaseJob(dependentJobId);
        DependentJob[] depentdentJobs = job.getDependentJobs();

        for (DependentJob dpj : depentdentJobs)
            if (jobId == dpj.getJobId())
                return false;

        return true;
    }

    public int[] getJobIds(int caseId, String[] jobNames) {
        int[] ids = new int[jobNames.length];
        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < jobNames.length; i++) {
                Criterion crit1 = Restrictions.eq("caseId", new Integer(caseId));
                Criterion crit2 = Restrictions.eq("name", jobNames[i]);
                CaseJob job = (CaseJob) hibernateFacade.load(CaseJob.class, new Criterion[] { crit1, crit2 }, session);
                ids[i] = job.getId();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return ids;
    }

    public synchronized List getPersistedWaitTasksByUser(int userId) {
        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::getPersistedWaitTasks Start method");

        Session session = sessionFactory.getSession();

        try {
            Criterion crit = Restrictions.eq("userId", new Integer(userId));
            return hibernateFacade.get(PersistedWaitTask.class, crit, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.clear();
            session.close();
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::getPersistedWaitTasks End method");
        return null;
    }

    public List getDistinctUsersOfPersistedWaitTasks() {
        Session session = sessionFactory.getSession();
        List userIds = null;

        try {
            String sql = "select id,user_id from cases.taskmanager_persist";

            userIds = session.createSQLQuery(sql).addEntity(IntegerHolder.class).list();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return userIds;
    }

    public void removePersistedTasks(PersistedWaitTask pwTask) {
        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::removePersistedTasks BEFORE num of tasks is pwTask null " + (pwTask == null));

        Session session = sessionFactory.getSession();

        try {
            session.clear();
            hibernateFacade.delete(pwTask, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::removePersistedTasks AFTER num of tasks= ");
    }

    public void addPersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::addPersistedTask BEFORE num of tasks= ");
        Session session = sessionFactory.getSession();

        try {
            //FIXME: should remove the old one if pesistedWaitTask already exists
            hibernateFacade.add(persistedWaitTask, session);
            if (DebugLevels.DEBUG_15)
                System.out.println("Adding job to persisted table, jobID: " + persistedWaitTask.getJobId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::addPersistedTask AFTER num of tasks= ");

    }

    public void removePersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::removePersistedTask (from CJTM) BEFORE num of tasks is pwTask null "
                    + (persistedWaitTask == null));

        Session session = sessionFactory.getSession();

        try {
            session.clear();
            Criterion crit1 = Restrictions.eq("userId", new Integer(persistedWaitTask.getUserId()));
            Criterion crit2 = Restrictions.eq("caseId", new Integer(persistedWaitTask.getCaseId()));
            Criterion crit3 = Restrictions.eq("jobId", new Integer(persistedWaitTask.getJobId()));
            Object object = hibernateFacade.load(PersistedWaitTask.class, new Criterion[] { crit1, crit2, crit3 },
                    session);
            if (object != null) {
                hibernateFacade.deleteTask(object, session);
            } else {
                if (DebugLevels.DEBUG_15) {
                    System.out.println("Removing from persisted table a job currently not there, jobID: "
                            + persistedWaitTask.getJobId());
                    int numberPersistedTasks = hibernateFacade.getAll(PersistedWaitTask.class, session).size();
                    System.out.println("Current size of persisted table: " + numberPersistedTasks);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9)
            System.out.println("CaseDAO::removePersistedTasks  (from CJTM) AFTER num of tasks= ");

    }

    public Case[] getCases(CaseCategory category) {
        Session session = sessionFactory.getSession();
        List cases = null;

        try {
            Criterion crit = Restrictions.eq("caseCategory", category);
            cases = hibernateFacade.get(Case.class, crit, session);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);
    }

    public CaseOutput getCaseOutput(CaseOutput output, Session session) {
        Criterion crit1 = Restrictions.eq("caseId", new Integer(output.getCaseId()));
        Criterion crit2 = Restrictions.eq("jobId", new Integer(output.getJobId()));
        Criterion crit3 = Restrictions.eq("name", output.getName());

        return (CaseOutput) hibernateFacade.load(CaseOutput.class, new Criterion[] { crit1, crit2, crit3 }, session);
    }

    public List<CaseOutput> getCaseOutputs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseId", new Integer(caseId));

        return hibernateFacade.get(CaseOutput.class, crit, session);
    }

    public List<CaseOutput> getCaseOutputs(int caseId, int jobId, Session session) {
        Criterion crit1 = Restrictions.eq("caseId", new Integer(caseId));
        Criterion crit2 = Restrictions.eq("jobId", new Integer(jobId));

        return hibernateFacade.get(CaseOutput.class, new Criterion[] { crit1, crit2 }, session);
    }

    public Case updateWithLock(Case caseObj, Session session) throws EmfException {
        return (Case) lockingScheme.renewLockOnUpdate(caseObj, current(caseObj, session), session);
    }

    public boolean canUpdate(Case caseObj, Session session) {
        if (!exists(caseObj.getId(), Case.class, session)) {
            return false;
        }

        Case current = current(caseObj.getId(), Case.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(caseObj.getName()))
            return true;

        return !nameUsed(caseObj.getName(), Case.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private void removeDatasetsOnOutput(User user, Session session, CaseOutput[] outputs) throws EmfException {
        DatasetDAO dsDao = new DatasetDAO();

        session.clear();

        for (CaseOutput output : outputs) {
            EmfDataset dataset = dsDao.getDataset(session, output.getDatasetId());

            if (dataset != null) {
                try {
                    dsDao.remove(user, dataset, session);
                } catch (EmfException e) {
                    if (DebugLevels.DEBUG_12)
                        System.out.println(e.getMessage());

                    throw new EmfException(e.getMessage());
                }
            }
        }
    }

    public Object loadCaseOutput(CaseOutput output, Session session) {
        Criterion[] criterions = uniqueCaseOutputCriteria(output);

        return hibernateFacade.load(CaseOutput.class, criterions, session);
    }

    private Criterion[] uniqueCaseOutputCriteria(CaseOutput output) {
        Integer caseID = new Integer(output.getCaseId());
        String outputname = output.getName();
        Integer datasetID = new Integer(output.getDatasetId());
        Integer jobID = new Integer(output.getJobId());

        Criterion c1 = Restrictions.eq("caseId", caseID);
        Criterion c2 = Restrictions.eq("name", outputname);
        Criterion c3 = (datasetID == null) ? Restrictions.isNull("datasetId") : Restrictions.eq("datasetId", datasetID);
        Criterion c4 = Restrictions.eq("jobId", jobID);

        return new Criterion[] { c1, c2, c3, c4 };
    }

    public boolean caseOutputExists(CaseOutput output, Session session) {
        Criterion[] criterions = uniqueCaseOutputCriteria(output);

        return hibernateFacade.exists(CaseOutput.class, criterions, session);
    }

    public void updateCaseOutput(CaseOutput output, Session session) {
        hibernateFacade.updateOnly(output, session);
    }

    public void removeJobMessages(JobMessage[] msgs, Session session) {
        hibernateFacade.remove(msgs, session);
    }
    
    public List<QueueCaseOutput> getQueueCaseOutputs(Session session) {
        return hibernateFacade.getAll(QueueCaseOutput.class, Order.asc("createDate"), session);
    }
    
    public void addQueueCaseOutput(QueueCaseOutput output, Session session) {
        hibernateFacade.add(output, session);
    }

    public void removeQedOutput(QueueCaseOutput output, Session session) {
        hibernateFacade.remove(output, session);
    }
}
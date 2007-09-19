package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
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

    public void add(Host object, Session session) {
        addObject(object, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List getAbbreviations(Session session) {
        return hibernateFacade.getAll(Abbreviation.class, Order.asc("name"), session);
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

    public Case obtainLocked(User owner, Case element, Session session) {
        return (Case) lockingScheme.getLocked(owner, current(element, session), session);
    }

    public Case releaseLocked(Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(current(locked, session), session);
    }

    public Case update(Case locked, Session session) throws EmfException {
        return (Case) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    private Case current(Case caze, Session session) {
        return (Case) hibernateFacade.current(caze.getId(), Case.class, session);
    }

    public boolean caseInputExists(CaseInput input, Session session) {
        Criterion[] criterions = uniqueCaseInputCriteria(input);

        return hibernateFacade.exists(CaseInput.class, criterions, session);
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

    public Object loadCaseInput(CaseInput input, Session session) {
        Criterion[] criterions = uniqueCaseInputCriteria(input);

        return hibernateFacade.load(CaseInput.class, criterions, session);
    }

    public List getCaseInputs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseID", new Integer(caseId));

        return hibernateFacade.get(CaseInput.class, crit, session);
    }

    public List getJobInputs(int caseId, int jobId, Sector sector, Session session) {
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

    public List<JobRunStatus> getJobRunStatuses(Session session) {
        return hibernateFacade.getAll(JobRunStatus.class, Order.asc("name"), session);
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

    public Object loadCaseJob(CaseJob job) {
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

    public String[] getAllValidJobs(int jobId) {
        List<String> validJobNames = new ArrayList<String>();
        int caseId = getCaseJob(jobId).getCaseId();
        List<CaseJob> jobs = getCaseJobs(caseId);
        
        for(Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            
            if (canDependOn(jobId, job.getId()))
                validJobNames.add(job.getName());
        }

        return validJobNames.toArray(new String[0]);
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

    public String[] getDependentJobs(int jobId) {
        DependentJob[] dependentJobs = getCaseJob(jobId).getDependentJobs();
        String[] dependentJobNames = new String[dependentJobs.length];

        for (int i = 0; i < dependentJobs.length; i++) {
            int id = dependentJobs[i].getJobId();
            dependentJobNames[i] = getCaseJob(id).getName();
        }

        return dependentJobNames;
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
}
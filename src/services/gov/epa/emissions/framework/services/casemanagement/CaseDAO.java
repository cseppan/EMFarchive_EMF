package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    private LockingScheme lockingScheme;

    public CaseDAO() {
        hibernateFacade = new HibernateFacade();
        lockingScheme = new LockingScheme();
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
        return (Case)hibernateFacade.get(Case.class, criterion, session).get(0);
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

        return new Criterion[]{ c1, c2, c3, c4, c5 };
    }

    public Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }

    public Object loadCaseInupt(CaseInput input, Session session) {
        Criterion[] criterions = uniqueCaseInputCriteria(input);
        
        return hibernateFacade.load(CaseInput.class, criterions, session);
    }

    public List getCaseInputs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseID", new Integer(caseId));

        return hibernateFacade.get(CaseInput.class, crit, session);
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

    public CaseJob getCaseJob(int jobId, Session session) {
        Criterion crit = Restrictions.eq("id", new Integer(jobId));
        return (CaseJob)hibernateFacade.load(CaseJob.class, crit, session);
    }
    
    public void updateCaseJob(CaseJob job, Session session) {
        hibernateFacade.updateOnly(job, session);
    }

    public List<JobRunStatus> getJobRunStatuses(Session session) {
        return hibernateFacade.getAll(JobRunStatus.class, Order.asc("name"), session);
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
        Criterion c2 = (paramname == null) ? Restrictions.isNull("parameterName") : Restrictions.eq("parameterName", paramname);
        Criterion c3 = (sector == null) ? Restrictions.isNull("sector") : Restrictions.eq("sector", sector);
        Criterion c4 = (program == null) ? Restrictions.isNull("program") : Restrictions.eq("program", program);
        Criterion c5 = Restrictions.eq("jobId", jobID);

        return new Criterion[]{ c1, c2, c3, c4, c5 };
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

    public Object loadCaseJob(CaseJob job, Session session) {
        Criterion c1 = Restrictions.eq("caseId", new Integer(job.getCaseId()));
        Criterion c2 = Restrictions.eq("name", job.getName());
        Criterion[] criterions = {c1, c2};
        
        return hibernateFacade.load(CaseJob.class, criterions, session);
    }
    
    public void removeCaseJobs(CaseJob[] jobs, Session session) {
        hibernateFacade.remove(jobs, session);
    }

    public void removeCaseParameters(CaseParameter[] params, Session session) {
        hibernateFacade.remove(params, session);
    }
}
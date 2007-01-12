package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void add(Abbreviation object, Session session) {
        addObject(object, session);
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
        Criterion[] criterions = caseInputDuplicateChecks(input);
        
        return hibernateFacade.exists(CaseInput.class, criterions, session);
    }

    private Criterion[] caseInputDuplicateChecks(CaseInput input) {
        Criterion c1 = Restrictions.eq("caseID", new Integer(input.getCaseID()));
        Criterion c2 = Restrictions.eq("inputName", input.getInputName());
        Criterion c3 = Restrictions.eq("sector", input.getSector());
        Criterion c4 = Restrictions.eq("program", input.getProgram());
        Criterion[] criterions = { c1, c2, c3, c4 };

        return criterions;
    }

    public Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }

    public Object loadCaseInupt(CaseInput input, Session session) {
        Map map = new HashMap();
        map.put("caseID", new Integer(input.getCaseID()));
        map.put("inputName", input.getInputName());
        map.put("sector", input.getSector());
        map.put("program", input.getProgram());
        Criterion criterion = Restrictions.allEq(map);

        return hibernateFacade.load(CaseInput.class, criterion, session);
    }

    public List getCaseInputs(int caseId, Session session) {
        Criterion crit = Restrictions.eq("caseID", new Integer(caseId));

        return hibernateFacade.get(CaseInput.class, crit, session);
    }

    public List getAllCaseInputs(Session session) {
        return hibernateFacade.getAll(CaseInput.class, Order.asc("id"), session);
    }

    public void updateCaseInput(CaseInput[] inputs, Session session) {
        hibernateFacade.update(inputs, session);
    }

    public List getModelToRuns(Session session) {
        return hibernateFacade.getAll(ModelToRun.class, Order.asc("name"), session);
    }

    public List getSubDirs(Session session) {
        return hibernateFacade.getAll(SubDir.class, Order.asc("name"), session);
    }

}
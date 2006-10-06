package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ControlMeasuresDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ControlMeasuresDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean exists(Class clazz, Criterion[] criterions, Session session) {
        return hibernateFacade.exists(clazz, criterions, session);
    }

    public ControlMeasure current(int id, Class clazz, Session session) {
        return (ControlMeasure) hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(ControlMeasure measure, Session session) {
        if (!exists(measure.getId(), ControlMeasure.class, session)) {
            return false;
        }

        ControlMeasure current = current(measure.getId(), ControlMeasure.class, session);
        session.clear();// clear to flush current
        if (current.getName().equals(measure.getName()))
            return true;

        return !nameUsed(measure.getName(), ControlMeasure.class, session);
    }

    /*
     * Return true if the name is already used
     */
    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public boolean exists(String name, Session session) {
        return hibernateFacade.exists(name, ControlMeasure.class, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(ControlMeasure.class, session);
    }

    // NOTE: it't not happening in one transaction. modify?
    public void add(ControlMeasure measure, Scc[] sccs, Session session) throws EmfException {
        checkForConstraints(measure, session);
        hibernateFacade.add(measure, session);
        controlMeasureIds(measure, sccs, session);
        hibernateFacade.add(sccs, session);
    }

    private void controlMeasureIds(ControlMeasure measure, Scc[] sccs, Session session) {
        ControlMeasure cm = (ControlMeasure) hibernateFacade.load(ControlMeasure.class, Restrictions.eq("name", measure
                .getName()), session);
        int cmId = cm.getId();
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(cmId);
        }
    }

    public void remove(ControlMeasure measure, Session session) {
        hibernateFacade.remove(measure, session);
    }

    public ControlMeasure obtainLocked(User user, ControlMeasure measure, Session session) {
        return (ControlMeasure) lockingScheme.getLocked(user, measure, session, all(session));
    }

    public ControlMeasure releaseLocked(ControlMeasure locked, Session session) {
        return (ControlMeasure) lockingScheme.releaseLock(locked, session, all(session));
    }

    public ControlMeasure update(ControlMeasure locked, Scc[] sccs, Session session) throws EmfException {
        checkForConstraints(locked, session);
        ControlMeasure releaseLockOnUpdate = (ControlMeasure) lockingScheme.releaseLockOnUpdate(locked, session,
                all(session));
        hibernateFacade.update(sccs, session);
        return releaseLockOnUpdate;
    }

    public void update(ControlMeasure[] measures, Session session) {
        hibernateFacade.update(measures, session);
    }

    public void add(ControlMeasure[] measures, Session session) {
        hibernateFacade.add(measures, session);
    }

    public Scc[] geSccs(ControlMeasure measure) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(measure, new EmfDbServer());
            return retrieveSCC.sccs();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void checkForConstraints(ControlMeasure controlMeasure, Session session) throws EmfException {
        if (nameExist(controlMeasure, session))
            throw new EmfException("The Control Measure name is already in use: " + controlMeasure.getName());

        if (abbrExist(controlMeasure, session))
            throw new EmfException("The Control Measure Abbreviation already in use: "
                    + controlMeasure.getAbbreviation());
    }

    private boolean nameExist(ControlMeasure controlMeasure, Session session) {
        String name = controlMeasure.getName();
        Criterion criterion1 = Restrictions.eq("name", name);
        Criterion criterion2 = Restrictions.ne("id", new Integer(controlMeasure.getId()));
        Criterion[] criterions = { criterion1, criterion2 };
        return exists(ControlMeasure.class, criterions, session);
    }

    private boolean abbrExist(ControlMeasure controlMeasure, Session session) {
        String abbr = controlMeasure.getAbbreviation();
        Criterion criterion1 = Restrictions.eq("abbreviation", abbr);
        Criterion criterion2 = Restrictions.ne("id", new Integer(controlMeasure.getId()));
        Criterion[] criterions = { criterion1, criterion2 };
        return exists(ControlMeasure.class, criterions, session);
    }

    public ControlMeasure load(ControlMeasure measure, Session session) {
        return (ControlMeasure) hibernateFacade.load(ControlMeasure.class, Restrictions.eq("name", measure.getName()),
                session);
    }

}

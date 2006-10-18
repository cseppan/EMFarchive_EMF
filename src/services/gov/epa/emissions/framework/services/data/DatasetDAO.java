package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public DatasetDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public EmfDataset current(int id, Class clazz, Session session) {
        return (EmfDataset) hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(EmfDataset dataset, Session session) {
        if (!exists(dataset.getId(), EmfDataset.class, session)) {
            return false;
        }

        EmfDataset current = current(dataset.getId(), EmfDataset.class, session);
        session.clear();// clear to flush current
        if (current.getName().equals(dataset.getName()))
            return true;

        return !nameUsed(dataset.getName(), EmfDataset.class, session);
    }

    public boolean exists(String name, Session session) {
        return hibernateFacade.exists(name, EmfDataset.class, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(EmfDataset.class, session);
    }

    public void add(EmfDataset dataset, Session session) {
        hibernateFacade.add(dataset, session);
    }

    public void updateWithoutLocking(EmfDataset dataset, Session session) {
        hibernateFacade.update(dataset, session);
    }

    public void remove(EmfDataset dataset, Session session) {
        hibernateFacade.remove(dataset, session);
    }

    public EmfDataset obtainLocked(User user, EmfDataset dataset, Session session) {
        return (EmfDataset) lockingScheme.getLocked(user, current(dataset, session), session);
    }

    public EmfDataset releaseLocked(EmfDataset locked, Session session) {
        return (EmfDataset) lockingScheme.releaseLock(current(locked, session), session);
    }

    public EmfDataset update(EmfDataset locked, Session session) throws EmfException {
        return (EmfDataset) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    private EmfDataset current(EmfDataset dataset, Session session) {
        return current(dataset.getId(), EmfDataset.class, session);
    }

    public List getDatasets(Session session, DatasetType datasetType) {
        Criterion criterion = Restrictions.eq("datasetType", datasetType);
        Order order = Order.asc("name");
        return hibernateFacade.get(EmfDataset.class, criterion, order, session);
    }

    public EmfDataset getDataset(Session session, String name) {
        Criterion criterion = Restrictions.eq("name", name);
        Order order = Order.asc("name");
        return (EmfDataset) hibernateFacade.get(EmfDataset.class, criterion, order, session).get(0);
    }

    public EmfDataset getDataset(Session session, int id) {
        Criterion criterion = Restrictions.eq("id", new Integer(id));
        return (EmfDataset) hibernateFacade.load(EmfDataset.class, criterion, session);
    }

}

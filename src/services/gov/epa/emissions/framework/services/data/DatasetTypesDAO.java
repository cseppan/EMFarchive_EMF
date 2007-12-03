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

public class DatasetTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public DatasetTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return hibernateFacade.getAll(DatasetType.class, Order.asc("name").ignoreCase(), session);
    }

    public DatasetType obtainLocked(User user, DatasetType type, Session session) {
        return (DatasetType) lockingScheme.getLocked(user, current(type, session), session);
    }

    public DatasetType releaseLocked(DatasetType locked, Session session) {
        return (DatasetType) lockingScheme.releaseLock(current(locked, session), session);
    }

    public DatasetType update(DatasetType type, Session session) throws EmfException {
        return (DatasetType) lockingScheme.releaseLockOnUpdate(type, current(type, session), session);
    }

    public DatasetType get(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        List list = hibernateFacade.get(DatasetType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (DatasetType) list.get(0);
    }

    public void add(DatasetType datasetType, Session session) {
        hibernateFacade.add(datasetType, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        if (!exists(datasetType.getId(), DatasetType.class, session)) {
            return false;
        }

        DatasetType current = current(datasetType.getId(), DatasetType.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(datasetType.getName()))
            return true;

        return !nameUsed(datasetType.getName(), DatasetType.class, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private DatasetType current(int id, Class clazz, Session session) {
        return (DatasetType) hibernateFacade.current(id, clazz, session);
    }

    private DatasetType current(DatasetType datasetType, Session session) {
        return current(datasetType.getId(), DatasetType.class, session);
    }

}

package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlProgramDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

//    private HibernateSessionFactory sessionFactory;
//
//    private DbServerFactory dbServerFactory;
    
    public ControlProgramDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public ControlProgramDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
//        this.dbServerFactory = dbServerFactory;
//        this.sessionFactory = sessionFactory;
    }

    public int add(ControlProgram element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    // return ControlStrategies orderby name
    public List all(Session session) {
//        return session.createQuery("select new ControlStrategy(cS.id, cS.name, " +
//                "cS.lastModifiedDate, cS.runStatus, " +
//                "cS.region, cS.targetPollutant, " +
//                "cS.project, cS.strategyType, " +
//                "cS.costYear, cS.inventoryYear, " +
//                "cS.creator, (select sum(sR.totalCost) from ControlStrategyResult sR where sR.controlStrategyId = cS.id), (select sum(sR.totalReduction) from ControlStrategyResult sR where sR.controlStrategyId = cS.id)) " +
//                "from ControlStrategy cS " +
//                "left join cS.targetPollutant " +
//                "left join cS.strategyType " +
//                "left join cS.region " +
//                "left join cS.project " +
//                "left join cS.region " +
//                "order by cS.name").list();
        return hibernateFacade.getAll(ControlProgram.class, Order.asc("name"), session);
    }

    public List getControlProgramTypes(Session session) {
        return hibernateFacade.getAll(ControlProgramType.class, Order.asc("name"), session);
    }

    public ControlProgram obtainLocked(User owner, int id, Session session) {
        return (ControlProgram) lockingScheme.getLocked(owner, current(id, ControlProgram.class, session), session);
    }

    public void releaseLocked(User user, int id, Session session) {
        ControlProgram current = getControlProgram(id, session);
        lockingScheme.releaseLock(user, current, session);
    }

    public ControlProgram update(ControlProgram locked, Session session) throws EmfException {
        return (ControlProgram) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public ControlProgram updateWithLock(ControlProgram locked, Session session) throws EmfException {
        return (ControlProgram) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private ControlProgram current(ControlProgram controlProgram, Session session) {
        return current(controlProgram.getId(), ControlProgram.class, session);
    }

    public boolean canUpdate(ControlProgram controlProgram, Session session) {
        if (!exists(controlProgram.getId(), ControlProgram.class, session)) {
            return false;
        }

        ControlProgram current = current(controlProgram.getId(), ControlProgram.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(controlProgram.getName()))
            return true;

        return !nameUsed(controlProgram.getName(), ControlProgram.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ControlProgram current(int id, Class clazz, Session session) {
        return (ControlProgram) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(ControlProgram controlProgram, Session session) throws EmfException {
        //see if control strategy is using the program, if so throw an error...
        List list = session.createQuery("select cS.name " +
                "from ControlStrategy cS, ControlProgram cP " +
                "where cP.id = :id").setInteger("id", controlProgram.getId()).list();
        if (list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        hibernateFacade.remove(controlProgram, session);
    }

    public ControlProgram getByName(String name, Session session) {
        ControlProgram cs = (ControlProgram) hibernateFacade.load(ControlProgram.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public ControlProgram getControlProgram(int id, Session session) {
        ControlProgram cs = (ControlProgram) hibernateFacade.load(ControlProgram.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }
    
    public List<ControlProgram> getControlProgramsByControlMeasures(int[] cmIds, Session session) {
        List<ControlProgram> list = new ArrayList<ControlProgram>();
        String idList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + cmIds[i];
        }
        try {
            Query query = session.createQuery("select distinct cp "
                    + "FROM ControlProgram AS cp "
                    + (cmIds != null && cmIds.length > 0 
                            ? "inner join cp.controlMeasures AS cpm "
                               + "WHERE cpm.id in (" + idList + ") " 
                            : "")
                    + "order by cp.name");
            query.setCacheable(true);
            list = query.list();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public void updateControlProgram(int controlProgramId, String msg, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update ControlProgram set description =  '' || "
                        + "description || '\n------\n' || :msg, lastModifiedDate = :date where id = :id")
            .setText("msg", msg)
            .setTimestamp("date", new Date())
            .setInteger("id", controlProgramId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}

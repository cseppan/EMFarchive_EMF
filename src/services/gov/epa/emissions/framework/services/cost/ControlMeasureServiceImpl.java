package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ControlMeasureServiceImpl implements ControlMeasureService {

    private static Log LOG = LogFactory.getLog(ControlMeasureServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private ControlMeasureDAO dao;

    private ControlTechnologiesDAO controlTechnologiesDAO;

    private DbServerFactory dbServerFactory;
    
    public ControlMeasureServiceImpl() throws Exception {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

//    public ControlMeasureServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
//        init(sessionFactory);
//    }
//

    public ControlMeasureServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
//        this(sessionFactory);
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        init();
    }

    private void init() {
        dao = new ControlMeasureDAO();
        controlTechnologiesDAO = new ControlTechnologiesDAO();
    }


    public ControlMeasure[] getMeasures() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.all(session);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measures.", e);
            throw new EmfException("Could not retrieve control measures.");
        } finally {
            session.close();
            
        }
    }
    
    public ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getControlMeasures(pollutant, session);
            return (ControlMeasure[]) all.toArray(new ControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve all control measures with major pollutant -- " + pollutant.getName(), e);
            throw new EmfException("Could not retrieve all control measureswith major pollutant -- " + pollutant.getName());
        } finally {
            session.close();
        }
    }

    public int addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.add(measure, sccs, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure: " + measure.getName(), e);
            throw new EmfException("Could not add control measure: " + measure.getName());
        } finally {
            session.close();
        }
    }

    public void removeMeasure(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.remove(controlMeasureId, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            session.close();
        }
    }

    public int copyMeasure(int controlMeasureId, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.copy(controlMeasureId, creator, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not remove control measure Id: " + controlMeasureId);
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public ControlMeasure obtainLockedMeasure(User owner, int controlMeasureId) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure locked = dao.obtainLocked(owner, controlMeasureId, session);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for ControlMeasure Id: " + controlMeasureId + " by owner: "
                    + owner.getUsername());
        } finally {
            if (session != null)
                session.close();
        }
    }

    public ControlMeasure getMeasure(int controlMeasureId) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            ControlMeasure measure = dao.current(controlMeasureId, session);
            return measure;
        } catch (RuntimeException e) {
            LOG.error("Could not get Control Measure for Control Measure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get Control Measure for Control Measure Id: " + controlMeasureId);
        } finally {
            if (session != null)
                session.close();
        }
    }

//    public ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            ControlMeasure released = dao.releaseLocked(locked, session);
//            return released;
//        } catch (RuntimeException e) {
//            LOG.error("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
//                    + locked.getLockOwner(), e);
//            throw new EmfException("Could not release lock for ControlMeasure: " + locked.getName() + " by owner: "
//                    + locked.getLockOwner());
//        } finally {
//            session.close();
//        }
//    }

    public void releaseLockedControlMeasure(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for control measure id: " + id, e);
            throw new EmfException("Could not release lock for control measure id: " + id);
        } finally {
            session.close();
        }
    }

    public ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasure updated = dao.update(measure, sccs, session);
            return updated;
        } catch (RuntimeException e) {
            LOG.error("Could not update for ControlMeasure: " + measure.getName(), e);
            throw new EmfException("Could not update for ControlMeasure: " + measure.getName());
        } finally {
            session.close();
        }
    }

    public Scc[] getSccsWithDescriptions(int controlMeasureId) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            Scc[] sccs = dao.getSccsWithDescriptions(controlMeasureId, dbServer);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get SCCs for ControlMeasure Id: " + controlMeasureId);
        } finally {
            close(dbServer);
        }
    }

    public Scc[] getSccs(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Scc[] sccs = dao.getSccs(controlMeasureId, session);
            return sccs;
        } catch (RuntimeException e) {
            LOG.error("Could not get SCCs for ControlMeasure Id: " + controlMeasureId, e);
            throw new EmfException("Could not get SCCs for ControlMeasure Id: " + controlMeasureId);
        } finally {
            session.close();
        }
    }

    public ControlTechnology[] getControlTechnologies() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = controlTechnologiesDAO.getAll(session);

            return (ControlTechnology[]) all.toArray(new ControlTechnology[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control technologies.", e);
            throw new EmfException("Could not retrieve control technologies.");
        } finally {
            session.close();
        }
    }

    public CostYearTable getCostYearTable(int targetYear) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            CostYearTableReader reader = new CostYearTableReader(dbServer, targetYear);
            return reader.costYearTable();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    private void close(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null)
                dbServer.disconnect();

        } catch (SQLException e) {
            LOG.error("Could not close database server", e);
            throw new EmfException("Could not close database server");
        }
    }

    public ControlMeasureClass[] getMeasureClasses() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.allCMClasses(session);
            return (ControlMeasureClass[]) all.toArray(new ControlMeasureClass[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure classes.", e);
            throw new EmfException("Could not retrieve control measure classes.");
        } finally {
            session.close();
        }
    }

    public ControlMeasureClass getMeasureClass(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCMClass(session, name);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure class.", e);
            throw new EmfException("Could not retrieve control measure class.");
        } finally {
            session.close();
        }
    }

    public LightControlMeasure[] getLightControlMeasures() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getLightControlMeasures(session);
            return (LightControlMeasure[]) all.toArray(new LightControlMeasure[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve light control measures.", e);
            throw new EmfException("Could not retrieve light control measures.");
        } finally {
            session.close();
        }
    }

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getEfficiencyRecords(controlMeasureId, session);
            return (EfficiencyRecord[]) all.toArray(new EfficiencyRecord[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } finally {
            session.close();
        }
    }

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getEfficiencyRecords(controlMeasureId, recordLimit, filter, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.addEfficiencyRecord(efficiencyRecord, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not add control measure efficiency record", e);
            throw new EmfException("Could not add control measure efficiency record");
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.updateEfficiencyRecord(efficiencyRecord, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not update for control measure efficiency record Id: " + efficiencyRecord.getId(), e);
            throw new EmfException("Could not update for control measure efficiency record Id: " + efficiencyRecord.getId());
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public void removeEfficiencyRecord(int efficiencyRecordId) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            dao.removeEfficiencyRecord(efficiencyRecordId, session, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control measure efficiency record Id: " + efficiencyRecordId, e);
            throw new EmfException("Could not remove control measure efficiency record Id: " + efficiencyRecordId);
        } finally {
            session.close();
            close(dbServer);
        }
    }

    public ControlMeasure[] getSummaryControlMeasures() throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getSummaryControlMeasures(dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public ControlMeasure[] getSummaryControlMeasures(int majorPollutantId) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getSummaryControlMeasures(majorPollutantId, dbServer);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException("Could not retrieve control measures efficiency records.");
        } catch (Exception e) {
            LOG.error("Could not retrieve control measure efficiency records.", e);
            throw new EmfException(e.getMessage());
        } finally {
            close(dbServer);
        }
    }

    public EquationType[] getEquationTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getEquationTypes(session);
            return (EquationType[]) all.toArray(new EquationType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure Equation Types.", e);
            throw new EmfException("Could not retrieve control measures Equation Types.");
        } finally {
            session.close();
        }
    }
}
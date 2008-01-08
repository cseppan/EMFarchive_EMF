package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDAO {

    private static Log LOG = LogFactory.getLog(DatasetDAO.class);

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private DbServerFactory dbServerFactory;

    public DatasetDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public DatasetDAO(DbServerFactory dbServerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session); // case insensitive comparison
    }

    public EmfDataset current(int id, Class clazz, Session session) {
        return (EmfDataset) hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(EmfDataset dataset, Session session) throws Exception {
        return canUpdate(dataset.getId(), dataset.getName(), session);
    }

    private boolean canUpdate(int id, String newName, Session session) throws Exception {
        if (!exists(id, EmfDataset.class, session)) {
            throw new EmfException("Dataset with id=" + id + " does not exist.");
        }

        EmfDataset current = current(id, EmfDataset.class, session);
        session.clear();// clear to flush current
        if (current.getName().equals(newName))
            return true;

        return !datasetNameUsed(newName);
    }

    public boolean exists(String name, Session session) {
        return hibernateFacade.exists(name, EmfDataset.class, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(EmfDataset.class, session);
    }

    // FIXME: to be deleted after dataset removed from db
    public List allNonDeleted(Session session) {
        Criterion crit = Restrictions.ne("status", "Deleted");
        return hibernateFacade.get(EmfDataset.class, crit, session);
    }

    public void add(EmfDataset dataset, Session session) {
        hibernateFacade.add(dataset, session);
    }

    public void updateWithoutLocking(EmfDataset dataset, Session session) throws Exception {
        try {
            renameEmissionTable(dataset, getDataset(session, dataset.getId()), session);
        } catch (Exception e) {
            LOG.info("Can not rename emission table: " + dataset.getInternalSources()[0].getTable());
        } finally {
            session.clear();
            hibernateFacade.updateOnly(dataset, session);
        }
    }

    public void remove(EmfDataset dataset, Session session) {
        if (DebugLevels.DEBUG_12)
            System.out.println("dataset dao remove(dataset, session) called: " + dataset.getId() + " "
                    + dataset.getName());

        hibernateFacade.remove(dataset, session);
    }

    public void remove(User user, EmfDataset dataset, Session session) throws EmfException {
        // NOTE: method to be modified to really remove dataset. It is only rename it for now.
        if (dataset.getStatus().equalsIgnoreCase("Deleted"))
            return;

        String datasetName = dataset.getName();

        if (dataset.isLocked())
            throw new EmfException("Could not remove dataset " + datasetName + ". It is locked by "
                    + dataset.getLockOwner());

        if (DebugLevels.DEBUG_12) {
            System.out.println("dataset dao remove(user, dataset, session) called: " + dataset.getId() + " "
                    + datasetName);
            System.out.println("Dataset status: " + dataset.getStatus() + " dataset retrieved null? "
                    + (getDataset(session, dataset.getId()) == null));
        }

        if (isUsedByCases(session, dataset))
            throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is used by a case.");

        if (isUsedByControlStrategies(session, dataset))
            throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is use by a control strategy.");

        String prefix = "DELETED_" + new Date().getTime() + "_";

        try {
            String newName = prefix + datasetName;

            if (!canUpdate(dataset.getId(), newName, session)) // Check to see if the new name is available
                throw new EmfException("The Dataset name is already in use: " + dataset.getName());

            DatasetType type = dataset.getDatasetType();

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset locked = obtainLocked(user, dataset, session);

            if (locked == null) {
                LOG.info("Could not get lock on dataset " + dataset.getName() + " to remove it.");
                return;
            }

            locked.setName(newName);
            locked.setStatus("Deleted");

            updateToRemove(locked, dataset, session);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not remove dataset " + datasetName + ". Reason: " + e.getMessage());
        }
    }

    public EmfDataset obtainLocked(User user, EmfDataset dataset, Session session) {
        return (EmfDataset) lockingScheme.getLocked(user, current(dataset, session), session);
    }

    public EmfDataset releaseLocked(EmfDataset locked, Session session) {
        return (EmfDataset) lockingScheme.releaseLock(current(locked, session), session);
    }

    public EmfDataset update(EmfDataset locked, Session session) throws Exception {
        EmfDataset toReturn = null;

        try {
            renameEmissionTable(locked, getDataset(session, locked.getId()), session);
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12)
                System.out.println("Update dataset " + locked.getName() + " with id: " + locked.getId());

            toReturn = (EmfDataset) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
        }

        return toReturn;
    }

    private void updateToRemove(EmfDataset locked, EmfDataset oldDataset, Session session) throws Exception {
        try {
            renameEmissionTable(locked, oldDataset, session);
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12)
                System.out.println("Update to remove " + locked.getName() + " with id: " + locked.getId());

            lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
        }
    }

    private EmfDataset current(EmfDataset dataset, Session session) {
        return current(dataset.getId(), EmfDataset.class, session);
    }

    public List getDatasets(Session session, DatasetType datasetType) {
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion typeCrit = Restrictions.eq("datasetType", datasetType);
        Criterion criterion = Restrictions.and(statusCrit, typeCrit);
        Order order = Order.asc("name");
        return hibernateFacade.get(EmfDataset.class, criterion, order, session);
    }

    public EmfDataset getDataset(Session session, String name) {
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion nameCrit = Restrictions.eq("name", name);
        Criterion criterion = Restrictions.and(statusCrit, nameCrit);
        Order order = Order.asc("name");
        return (EmfDataset) hibernateFacade.get(EmfDataset.class, criterion, order, session).get(0);
    }

    public EmfDataset getDataset(Session session, int id) {
        session.clear(); // to clear the cached objects in session if any
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion idCrit = Restrictions.eq("id", new Integer(id));
        Criterion criterion = Restrictions.and(statusCrit, idCrit);
        return (EmfDataset) hibernateFacade.load(EmfDataset.class, criterion, session);
    }

    public boolean isUsedByControlStrategies(Session session, EmfDataset dataset) {
        List strategies = hibernateFacade.getAll(ControlStrategy.class, session);

        if (strategies == null || strategies.isEmpty())
            return false;

        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy cs = (ControlStrategy) iter.next();
            if (datasetUsed(cs, dataset))
                return true;
        }

        return false;
    }

    public boolean isUsedByCases(Session session, EmfDataset dataset) {
        CaseDAO caseDao = new CaseDAO();

        List caseInputs = caseDao.getAllCaseInputs(session);

        if (caseInputs == null || caseInputs.isEmpty())
            return false;

        if (datasetUsed((CaseInput[]) caseInputs.toArray(new CaseInput[0]), dataset))
            return true;

        return false;
    }

    private boolean datasetUsed(CaseInput[] inputs, EmfDataset dataset) {
        for (int i = 0; i < inputs.length; i++) {
            EmfDataset caseInputDataset = inputs[i].getDataset();
            if (caseInputDataset != null && caseInputDataset.equals(dataset))
                return true;
        }
        return false;
    }

    private boolean datasetUsed(ControlStrategy cs, EmfDataset dataset) {
        ControlStrategyInputDataset[] controlStrategyInputDatasets = cs.getControlStrategyInputDatasets();
        for (int i = 0; i < controlStrategyInputDatasets.length; i++)
            if (controlStrategyInputDatasets[i].getInputDataset().equals(dataset))
                return true;

        return false;
    }

    public long getDatasetRecordsNumber(DbServer dbServer, Session session, EmfDataset dataset, Version version)
            throws SQLException {
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return dataset.getExternalSources().length;

        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource source = dataset.getInternalSources()[0];
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(version, session);
        long totalCount = 0;

        try {
            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(countQuery);
            resultSet.next();
            totalCount = resultSet.getInt(1);

            resultSet.close();
        } catch (SQLException e) {
            throw new SQLException("Cannot get total records number on dataset: " + dataset.getName() + " Reason: "
                    + e.getMessage());
        } finally {
            dbServer.disconnect();
        }

        return totalCount;
    }

    private String getWhereClause(Version version, Session session) {
        String versions = versionsList(version, session);
        String deleteClause = createDeleteClause(versions);

        String whereClause = " WHERE dataset_id = " + version.getDatasetId() + " AND version IN (" + versions + ")"
                + deleteClause;

        return whereClause;
    }

    private String createDeleteClause(String versions) {
        StringBuffer buffer = new StringBuffer();

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken();
            if (!version.equals("0")) {
                String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
                if (buffer.length() == 0) {
                    buffer.append(" AND ");
                }
                buffer.append(" delete_versions NOT SIMILAR TO '" + regex + "'");

                if (tokenizer.hasMoreTokens())
                    buffer.append(" AND ");
            }
        }

        return buffer.toString();
    }

    private String versionsList(Version finalVersion, Session session) {
        Versions versions = new Versions();
        Version[] path = versions.getPath(finalVersion.getDatasetId(), finalVersion.getVersion(), session);

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            result.append(path[i].getVersion());
            if ((i + 1) < path.length)
                result.append(",");
        }
        return result.toString();
    }

    private void renameEmissionTable(EmfDataset dataset, EmfDataset oldDataset, Session session) throws Exception {
        if (DebugLevels.DEBUG_12) {
            System.out.println("Check to rename. Dataset name: " + dataset.getName() + " Status: "
                    + dataset.getStatus() + " id: " + dataset.getId());
            System.out.println("Old dataset is null? " + (oldDataset == null));
            System.out.println("Old dataset name: " + ((oldDataset == null) ? "" : oldDataset.getName()));
            System.out.println("Old dataset status: " + ((oldDataset == null) ? "" : oldDataset.getStatus()));
            System.out.println("Old dataset exists? " + exists(dataset.getId(), EmfDataset.class, session));
        }

        if (!continueToRename(dataset, oldDataset))
            return;

        if (DebugLevels.DEBUG_12)
            System.out.println("Dataset ok to rename.");

        DbServer dbServer = getDbServer();

        try {
            renameTable(dataset, oldDataset, dbServer);
        } finally {
            dbServer.disconnect();
        }
    }

    private boolean continueToRename(EmfDataset dataset, EmfDataset oldDataset) {
        if (oldDataset == null) {
            System.out.println("old data set is null");
            return false;
        }

        DatasetType type = dataset.getDatasetType();
        InternalSource[] sources = dataset.getInternalSources();

        if (dataset.getName().equalsIgnoreCase(oldDataset.getName())) {
            System.out.println("new dataset has the same name");
            return false;
        }

        if (sources == null || sources.length == 0 || type.getTablePerDataset() != 1) {
            System.out.println("internal sources is null");
            return false;
        }

        return true;
    }

    private void renameTable(EmfDataset dataset, EmfDataset oldDataset, DbServer dbServer) throws ImporterException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource[] sources = dataset.getInternalSources();
        DataTable table = new DataTable(oldDataset, datasource);
        String oldTableName = oldDataset.getInternalSources()[0].getTable();
        String newTableName = table.createName(dataset.getName());
        
        if (DebugLevels.DEBUG_12)
            System.out.println("new table name: " + newTableName + " old table name:" + oldTableName);
        
        sources[0].setTable(newTableName);
        table.rename(oldTableName, newTableName);
    }

    public boolean datasetNameUsed(String name) throws Exception {
        String getDSNamesQuery = "SELECT name FROM emf.datasets";
        boolean nameexists = false;
        DbServer dbServer = null;

        try {
            dbServer = getDbServer();
            Datasource datasource = dbServer.getEmfDatasource();
            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(getDSNamesQuery);

            while (resultSet.next())
                if (resultSet.getString(1).trim().equalsIgnoreCase(name.trim())) {
                    LOG.warn("dataset name: " + name + " already exists.");
                    nameexists = true;
                    break;
                }
        } finally {
            if (dbServer != null)
                dbServer.disconnect();
        }

        return nameexists;
    }

    private DbServer getDbServer() throws Exception {
        DbServer dbServer;
        if (dbServerFactory == null)
            dbServer = new EmfDbServer();
        else
            dbServer = dbServerFactory.getDbServer();

        return dbServer;
    }

}

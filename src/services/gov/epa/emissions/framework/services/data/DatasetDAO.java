package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.editor.Revision;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
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

    public Object current(int id, Class clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(EmfDataset dataset, Session session) throws Exception {
        return canUpdate(dataset.getId(), dataset.getName(), session);
    }

    private boolean canUpdate(int id, String newName, Session session) throws Exception {
        if (!exists(id, EmfDataset.class, session)) {
            throw new EmfException("Dataset with id=" + id + " does not exist.");
        }

        EmfDataset current = (EmfDataset) current(id, EmfDataset.class, session);
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
        if (DebugLevels.DEBUG_14)
            System.out.println("DatasetDAO starts removing dataset " + dataset.getName() + " " + new Date());

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

        checkIfUsedByCases(new int[] { dataset.getId() }, session);

        // Disabled temporarily according to Alison's request 1/15/2008
        // if (isUsedByControlStrategies(session, dataset))
        // throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is use by a control strategy.");

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

        if (DebugLevels.DEBUG_14)
            System.out.println("DatasetDAO has finished removing dataset " + dataset.getName() + " " + new Date());
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
            if (DebugLevels.DEBUG_14)
                System.out.println("DatasetDAO starts renaming emission table for dataset: " + oldDataset.getName());
            renameEmissionTable(locked, oldDataset, session);
            if (DebugLevels.DEBUG_14)
                System.out.println("DatasetDAO has finished renaming emission table for dataset: "
                        + oldDataset.getName());
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12)
                System.out.println("Update to remove " + locked.getName() + " with id: " + locked.getId());

            lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
        }
    }

    private EmfDataset current(EmfDataset dataset, Session session) {
        return (EmfDataset) current(dataset.getId(), EmfDataset.class, session);
    }

    public List getDatasets(Session session, DatasetType datasetType) {
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion typeCrit = Restrictions.eq("datasetType", datasetType);
        Criterion criterion = Restrictions.and(statusCrit, typeCrit);
        Order order = Order.asc("name");
        return hibernateFacade.get(EmfDataset.class, criterion, order, session);
    }

    public List getDatasets(Session session, int datasetTypeId, String nameContains) {
        return session
                .createQuery(
                        "select new EmfDataset( DS.id, DS.name, DS.defaultVersion, DS.datasetType.id, DS.datasetType.name) from EmfDataset as DS where DS.datasetType.id = "
                                + datasetTypeId
                                + " and lower(DS.name) like "
                                + "'%"
                                + nameContains.toLowerCase().trim() + "%' and DS.status <> 'Deleted' order by DS.name")
                .list();
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
            if (DebugLevels.DEBUG_14)
                System.out.println("DatasetDAO starts renaming dataset table for dataset: " + dataset.getName());
            renameTable(dataset, oldDataset, dbServer);
            if (DebugLevels.DEBUG_14)
                System.out.println("DatasetDAO has finished renaming dataset table for dataset: " + dataset.getName());
        } finally {
            dbServer.disconnect();
        }
    }

    private boolean continueToRename(EmfDataset dataset, EmfDataset oldDataset) {
        if (oldDataset == null) {
            return false;
        }

        DatasetType type = dataset.getDatasetType();
        InternalSource[] sources = dataset.getInternalSources();

        if (dataset.getName().equalsIgnoreCase(oldDataset.getName())) {
            return false;
        }

        if (sources == null || sources.length == 0 || type.getTablePerDataset() != 1) {
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

    public void updateVersionNReleaseLock(Version target, Session session) throws EmfException {
        lockingScheme.releaseLockOnUpdate(target, (Version) current(target.getId(), Version.class, session), session);
    }

    public Version obtainLockOnVersion(User user, int id, Session session) {
        return (Version) lockingScheme.getLocked(user, (Version) current(id, Version.class, session), session);
    }

    public void deleteDatasets(EmfDataset[] datasets, DbServer dbServer, Session session)
            throws EmfException {
        
        if (datasets == null || datasets.length == 0)
            return;
        
        int[] datasetIDs = getIDs(datasets);

        // NOTE: list at least one of the cases or control strategies linked
        checkIfUsedByCases(datasetIDs, session);
        // NOTE: wait till decided by EPA OAQPS
        // checkIfUsedByStrategies(datasetIDs, session);

        Datasource datasource = dbServer.getEmissionsDatasource();
        TableCreator emissionTableTool = new TableCreator(datasource);

        // Need to search all the items associated with datasets and remove them properly
        // before remove the underline datasets
        deleteFromOutputsTable(datasetIDs, session);
        deleteFromEmfTables(datasetIDs, emissionTableTool, session);
        hibernateFacade.removeObjects(datasets, session);
        dropDataTables(datasets, emissionTableTool);
    }

    public void checkIfUsedByCases(int[] datasetIDs, Session session) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = session.createQuery(
                "select CI.caseID from CaseInput as CI " + "where (CI.dataset.id = "
                        + getAndOrClause(datasetIDs, "CI.dataset.id") + ")").list();

        if (list != null && list.size() > 0) {
            Criterion criterion = Restrictions.eq("id", list.get(0));
            Case usedCase = (Case) hibernateFacade.get(Case.class, criterion, session).get(0);
            throw new EmfException("Error: dataset used by case " + usedCase.getName() + ".");
        }
    }

    public void checkIfUsedByStrategies(int[] datasetIDs, Session session) throws EmfException {
        List list = null;

        // check if dataset is an input inventory for some strategy (via the StrategyInputDataset table)
        list = session.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        // check if dataset is an input inventory for some strategy (via the StrategyResult table, could be here for
        // historical reasons)
        list = session.createQuery(
                "select cS.name from ControlStrategyResult sR, ControlStrategy cS where "
                        + "sR.controlStrategyId = cS.id and (sR.inputDataset.id = "
                        + getAndOrClause(datasetIDs, "sR.inputDataset.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        // check if dataset is a detailed result dataset for some strategy
        list = session.createQuery(
                "select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id "
                        + "and (sR.detailedResultDataset.id = "
                        + getAndOrClause(datasetIDs, "sR.detailedResultDataset.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        // check if dataset is a controlled inventory for some strategy
        list = session.createQuery(
                "select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id "
                        + "and (sR.controlledInventoryDataset.id = "
                        + getAndOrClause(datasetIDs, "sR.controlledInventoryDataset.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy measures
        list = session.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join "
                        + "cM.regionDataset as rD with (rD.id = " + getAndOrClause(datasetIDs, "rD.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy
        list = session.createQuery(
                "select cS.name from ControlStrategy cS where (cS.countyDataset.id = "
                        + getAndOrClause(datasetIDs, "cS.countyDataset.id") + ")").list();

        if (list != null && list.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + list.get(0) + ".");
    }

    private void deleteFromEmfTables(int[] datasetIDs, TableCreator tableTool, Session session) throws EmfException {
        deleteFromObjectTable(datasetIDs, Version.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, AccessLog.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, Note.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, Revision.class, "datasetId", session);
        dropQAStepResultTable(datasetIDs, tableTool, session);
        deleteFromObjectTable(datasetIDs, QAStepResult.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, QAStep.class, "datasetId", session);
    }

    int deleteFromObjectTable(int[] datasetIDs, Class<?> clazz, String attrName, Session session) throws EmfException {
        int deletedEntities = 0;

        try {
            Transaction tx = session.beginTransaction();

            String hqlDelete = "DELETE FROM " + clazz.getSimpleName() + " obj WHERE obj." + attrName + " = "
                    + getAndOrClause(datasetIDs, "obj." + attrName);

            if (DebugLevels.DEBUG_16)
                System.out.println("hql delete string: " + hqlDelete);

            deletedEntities = session.createQuery(hqlDelete).executeUpdate();
            tx.commit();

            return deletedEntities;
        } catch (HibernateException e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16)
                LOG.warn(deletedEntities + " items deleted from " + clazz.getName() + " table.");
        }
    }

    public void deleteFromOutputsTable(int[] datasetIDs, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();
            
            String firstPart = "UPDATE " + CaseOutput.class.getSimpleName() + " obj SET ";
            String secondPart = " WHERE obj.datasetId = " + getAndOrClause(datasetIDs, "obj.datasetId");
            String updateQuery = firstPart + "obj.message = :msg, obj.datasetId = :id" + secondPart;
            
            if (DebugLevels.DEBUG_16)
                System.out.println("hql update string: " + updateQuery);

            updatedItems = session.createQuery(updateQuery)
                .setString("msg", "Associated dataset deleted")
                .setInteger("id", 0)
                .executeUpdate();
            tx.commit();
            
            if (DebugLevels.DEBUG_16)
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16)
                LOG.warn(updatedItems + " items updated from " + CaseOutput.class.getName() + " table.");
        }
    }

    private void dropDataTables(EmfDataset[] datasets, TableCreator tableTool) throws EmfException {
        for (int i = 0; i < datasets.length; i++)
            dropDataTables(tableTool, datasets[i]);
    }

    private void dropDataTables(TableCreator tableTool, EmfDataset dataset) throws EmfException {
        DatasetType type = dataset.getDatasetType();

        if (type != null && type.isExternal())
            return;

        InternalSource[] sources = dataset.getInternalSources();

        for (int i = 0; i < sources.length; i++)
            dropIndividualTable(tableTool, sources[i], (type != null) ? type.getName() : "", dataset.getId());
    }

    private void dropIndividualTable(TableCreator tableTool, InternalSource source, String type, int dsID)
            throws EmfException {
        String table = source.getTable();

        if (DebugLevels.DEBUG_16)
            System.out.println("Dropping data table  " + table);

        try {
            if (type.toUpperCase().contains("A/M/PTPRO") || type.toUpperCase().contains("COSTCY"))
                tableTool.deleteRecords(table, source.getCols()[1], "integer", "" + dsID); // 2nd column: dataset_id
            else
                tableTool.drop(table);
        } catch (Exception e) {
            if (DebugLevels.DEBUG_16)
                e.printStackTrace();

            throw new EmfException("Problem dropping data table " + table + ". " + e.getMessage());
        }

        if (DebugLevels.DEBUG_16)
            System.out.println("Data table  " + table + " dropped.");
    }

    private void dropQAStepResultTable(int[] datasetIDs, TableCreator tableTool, Session session) throws EmfException {
        List tables = session.createQuery(
                "SELECT obj.table from " + QAStepResult.class.getSimpleName() + " as obj WHERE obj.datasetId = "
                        + getAndOrClause(datasetIDs, "obj.datasetId")).list();

        for (Iterator<String> iter = tables.iterator(); iter.hasNext();) {
            String table = iter.next();
            
            try {
                tableTool.drop(table);
            } catch (Exception e) {
                if (DebugLevels.DEBUG_16)
                    e.printStackTrace();

                throw new EmfException("Error dropping qa step result table " + iter + ". " + e.getMessage());
            }
            
            if (DebugLevels.DEBUG_16)
                System.out.println("QA step result table " + table + " dropped.");
        }
    }

    private String getAndOrClause(int[] datasetIDs, String attrName) {
        StringBuffer sb = new StringBuffer();
        int numIDs = datasetIDs.length;

        if (numIDs == 1)
            return "" + datasetIDs[0];

        for (int i = 0; i < numIDs - 1; i++)
            sb.append(datasetIDs[i] + " OR " + attrName + " = ");

        sb.append(datasetIDs[numIDs - 1]);

        return sb.toString();
    }

    private int[] getIDs(EmfDataset[] datasets) {
        int len = datasets.length;
        int[] ids = new int[len];

        for (int i = 0; i < len; i++)
            ids[i] = datasets[i].getId();

        return ids;
    }

}

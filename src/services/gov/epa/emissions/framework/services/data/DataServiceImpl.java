package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.VersionedDatasetQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.TableToString;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class DataServiceImpl implements DataService {
    private static Log LOG = LogFactory.getLog(DataServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private DatasetDAO dao;

    public DataServiceImpl() {
        this(DbServerFactory.get(), HibernateSessionFactory.get());
    }

    public DataServiceImpl(HibernateSessionFactory sessionFactory) {
        this(null, sessionFactory);
    }

    public DataServiceImpl(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new DatasetDAO(dbServerFactory);
    }

    public synchronized EmfDataset[] getDatasets() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.allNonDeleted(session);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets", e);
            throw new EmfException("Could not get all Datasets");
        } finally {
            session.close();
        }

    }

    public synchronized EmfDataset getDataset(Integer datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = dao.getDataset(session, datasetId.intValue());
            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset with id=" + datasetId.intValue(), e);
            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset getDataset(String datasetName) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfDataset dataset = dao.getDataset(session, datasetName);

            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset " + datasetName, e);
            throw new EmfException("Could not get dataset " + datasetName);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset releaseLockedDataset(User user, EmfDataset locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset released = dao.releaseLocked(user, locked, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Dataset: " + locked.getName() + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Dataset: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetType type = dataset.getDatasetType();

            if (!dao.canUpdate(dataset, session))
                throw new EmfException("The Dataset name " + dataset.getName() + " is already in use.");

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset released = dao.update(dataset, session);

            return released;
        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + dataset.getName() + " " + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetType);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetType, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetType);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetTypeId);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetTypeId, nameContaining);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        String prefix = "DELETED_" + new Date().getTime() + "_";

        try {
            if (isRemovable(datasets, owner)) {
                for (int i = 0; i < datasets.length; i++) {
                    if (datasets[i].getStatus().equalsIgnoreCase("Deleted"))
                        continue;
                    datasets[i].setName(prefix + datasets[i].getName());
                    datasets[i].setStatus("Deleted");
                    updateDataset(datasets[i]);
                }
            }
        } catch (Exception e) {
            LOG.error("Could not delete datasets: ", e);
            throw new EmfException(e.getMessage());
        }
    }

    private synchronized boolean isRemovable(EmfDataset[] datasets, User owner) throws EmfException {
        int len = datasets.length;
        int[] dsIDs = new int[len];

        for (int i = 0; i < len; i++) {
            checkUser(datasets[i], owner);
            dsIDs[i] = datasets[i].getId();
        }

        checkCase(dsIDs);
        checkControlStrategy(dsIDs);

        return true;
    }

    private synchronized void checkUser(EmfDataset dataset, User owner) throws EmfException {
        if (!owner.isAdmin() && !dataset.getCreator().equalsIgnoreCase(owner.getUsername())) {
            releaseLockedDataset(owner, dataset);
            throw new EmfException("You are not the creator of " + dataset.getName() + ".");
        }
    }

    private synchronized void checkCase(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByCases(datasetIDs, session);
        } finally {
            session.close();
        }
    }

    private synchronized void checkControlStrategy(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByStrategies(datasetIDs, session);
        } finally {
            session.close();
        }
    }

    public synchronized String[] getDatasetValues(Integer datasetId) throws EmfException {
        EmfDataset dataset = null;
        List<String> values = new ArrayList<String>();

        if (datasetId == null || datasetId.intValue() == 0)
            dataset = new EmfDataset();
        else
            dataset = getDataset(datasetId);

        values.add("name," + (dataset.getName() == null ? "" : dataset.getName()));
        values.add("datasetType," + (dataset.getDatasetTypeName() == null ? "" : dataset.getDatasetTypeName()));
        values.add("creator," + (dataset.getCreator() == null ? "" : dataset.getCreator()));
        values.add("createdDateTime," + (dataset.getCreatedDateTime() == null ? "" : dataset.getCreatedDateTime()));
        values.add("status," + (dataset.getStatus() == null ? "" : dataset.getStatus()));

        return values.toArray(new String[0]);
    }

    public Version obtainedLockOnVersion(User user, int id) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.obtainLockOnVersion(user, id, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void updateVersionNReleaseLock(Version locked) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.updateVersionNReleaseLock(locked, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    public void checkIfDeletable(User user, int datasetID) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.checkIfUsedByCases(new int[] { datasetID }, session);
            dao.checkIfUsedByStrategies(new int[] { datasetID }, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void purgeDeletedDatasets(User user) throws EmfException {
        Session session = this.sessionFactory.getSession();
        DbServer dbServer = DbServerFactory.get().getDbServer();

        try {
            List<EmfDataset> list = dao.deletedDatasets(user, session);
            dao.deleteDatasets(list.toArray(new EmfDataset[0]), dbServer, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
            closeDB(dbServer);
        }
    }

    private void closeDB(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int getNumOfDeletedDatasets(User user) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.deletedDatasets(user, session).size();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized String getTableAsString(String qualifiedTableName) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return new TableToString(dbServer, qualifiedTableName, ",").toString();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName);
        } finally {
            closeDB(dbServer);
        }
    }

    public synchronized long getTableRecordCount(String qualifiedTableName) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        long recordCount = 0;
        try {
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select count(1) as record_count from " + qualifiedTableName);
            if (rs.next())
                recordCount = rs.getLong(1);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve table record count: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table record count: " + qualifiedTableName);
        } catch (SQLException e) {
            LOG.error("Could not retrieve table record count: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table record count: " + qualifiedTableName);
        } finally {
            closeDB(dbServer);
        }
        return recordCount;
    }

    public synchronized void appendData(int srcDSid, int srcDSVersion, String filter, int targetDSid,
            int targetDSVersion, DoubleValue targetStartLineNumber) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            Version srcVersion = dao.getVersion(session, srcDSid, srcDSVersion);
            EmfDataset srcDS = dao.getDataset(session, srcDSid);
            InternalSource[] srcSources = srcDS.getInternalSources();
            EmfDataset targetDS = dao.getDataset(session, targetDSid);
            InternalSource[] targetSources = targetDS.getInternalSources();

            DataModifier dataModifier = datasource.dataModifier();

            if (srcSources.length != targetSources.length)
                throw new EmfException("Source dataset set has different number of tables from target dataset.");

            if (srcDS.getDatasetType().getImporterClassName().equals(
                    "gov.epa.emissions.commons.io.generic.LineImporter")) {
                String srcTable = datasource.getName() + "." + srcSources[0].getTable();
                String targetTable = datasource.getName() + "." + targetSources[0].getTable();

                appendLineBasedData(filter, srcVersion, srcDS, srcTable, targetTable, targetDSid, targetDSVersion,
                        dataModifier, targetStartLineNumber.getValue());
                return;
            }

            for (int i = 0; i < targetSources.length; i++) {
                String srcTable = datasource.getName() + "." + srcSources[i].getTable();
                String targetTable = datasource.getName() + "." + targetSources[i].getTable();

                appendData2SingleTable(filter, srcVersion, srcDS, srcTable, targetTable, targetDSid, targetDSVersion,
                        dataModifier);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table: " + e.getMessage());
        } finally {
            closeDB(dbServer);
            session.close();
        }
    }

    private void appendLineBasedData(String filter, Version srcVersion, EmfDataset srcDS, String srcTable,
            String targetTable, int targetDSid, int targetDSVersion, DataModifier dataModifier, double startLineNum)
            throws Exception {
        String[] srcColumns = null;
        srcColumns = getTableColumns(dataModifier, srcTable, filter);

        String[] targetColumns = null;
        targetColumns = getTableColumns(dataModifier, targetTable, "");

        if (!areColumnsMatched(srcColumns, targetColumns))
            throw new EmfException("Cannot append data - source and target datasets have different table definitions.");

        VersionedDatasetQuery dsQuery = new VersionedDatasetQuery(srcVersion, srcDS);
        double nextBiggerLineNum, increatment;
        
        if (startLineNum < 0) {
            startLineNum = dataModifier.getLastRowLineNumber(targetTable);
            increatment = 1.0;
            
        } else {
            long records2Append = dataModifier.getRowCount(dsQuery.generateFilteringQueryWithoutOrderBy(" COUNT(*) ", srcTable, filter));
            nextBiggerLineNum = dataModifier.getNextBiggerLineNumber(targetTable, startLineNum);
            
            if (nextBiggerLineNum < 0)
                increatment = 1.0;
            else 
                increatment = (nextBiggerLineNum - startLineNum) / (records2Append + 1);
        }

        String query = "INSERT INTO "
                + targetTable
                + " ("
                + getTargetColString(targetColumns)
                + ") ("
                + getLineBasedSrcColString(targetDSid, targetDSVersion,
                        srcColumns, startLineNum, increatment, srcTable, filter, dsQuery) + ")";

        if (DebugLevels.DEBUG_17) {
            LOG.warn("Append data query: " + query);
            LOG.warn("Query starts at: " + new Date());
        }

        dataModifier.execute(query);

        if (DebugLevels.DEBUG_17)
            LOG.warn("Query ends at: " + new Date());
    }

    private void appendData2SingleTable(String filter, Version srcVersion, EmfDataset srcDS, String srcTable,
            String targetTable, int targetDSid, int targetDSVersion, DataModifier dataModifier) throws Exception {
        String[] srcColumns = null;
        srcColumns = getTableColumns(dataModifier, srcTable, filter);

        String[] targetColumns = null;
        targetColumns = getTableColumns(dataModifier, targetTable, "");

        if (!areColumnsMatched(srcColumns, targetColumns))
            throw new EmfException("Cannot append data - source and target datasets have different table definitions.");

        VersionedDatasetQuery dsQuery = new VersionedDatasetQuery(srcVersion, srcDS);
        String query = "INSERT INTO "
                + targetTable
                + " ("
                + getTargetColString(targetColumns)
                + ") ("
                + dsQuery.generateFilteringQuery(getSrcColString(targetDSid, targetDSVersion, srcColumns), srcTable,
                        filter) + ")";

        if (DebugLevels.DEBUG_17) {
            LOG.warn("Append data query: " + query);
            LOG.warn("Query starts at: " + new Date());
        }

        dataModifier.execute(query);

        if (DebugLevels.DEBUG_17)
            LOG.warn("Query ends at: " + new Date());
    }

    private String[] getTableColumns(DataModifier mod, String table, String filter) throws Exception {
        ResultSetMetaData md = null;
        String query = null;

        if (filter == null || filter.isEmpty())
            query = "SELECT * FROM " + table + " LIMIT 0";
        else
            query = "SELECT * FROM " + table + " WHERE (" + filter + ") LIMIT 0";

        try {
            md = mod.getMetaData(query);
        } catch (SQLException e) {
            if (filter.isEmpty())
                throw e;

            throw new Exception("Filter format is incorrect: " + filter);
        }

        List<String> cols = new ArrayList<String>();
        int colCount = md.getColumnCount();

        for (int i = 1; i <= colCount; i++)
            cols.add(md.getColumnName(i));

        return cols.toArray(new String[0]);
    }

    private boolean areColumnsMatched(String[] srcColumns, String[] targetColumns) {
        if (srcColumns.length != targetColumns.length)
            return false;

        for (int i = 0; i < srcColumns.length; i++)
            if (!srcColumns[i].equalsIgnoreCase(targetColumns[i]))
                return false;

        // NOTE: better if column types are also compared.

        return true;
    }

    private String getLineBasedSrcColString(int targetDSid, int targetDSVersion, String[] cols, double startLineNum,
            double increatment, String srcTable, String filter, VersionedDatasetQuery dsQuery) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        // delete_versions is overwritten with blank string ''
        String colString = "SELECT " + targetDSid + " AS dataset_id, " + targetDSVersion + " AS version, '' AS delete_versions";

        int numOfCols = cols.length;
        String colsOtherThanLineNumber = "";

        for (int i = 4; i < numOfCols; i++) {
            if (cols[i].equals("line_number"))
                colString += ", public.run_sum(" + startLineNum + ", incrementor, 'line_no'::text) AS line_number";
            else {
                colString += ", " + cols[i];
                colsOtherThanLineNumber += cols[i] + ", ";
            }
        }
        
        colString += " FROM (SELECT " + colsOtherThanLineNumber + " " + increatment + " AS incrementor " +
        		"FROM " + srcTable + " WHERE " + dsQuery.getVersionQuery() + filter + " ORDER BY line_number) AS srctbl";

        return colString;
    }

    private String getSrcColString(int targetDSid, int targetDSVersion, String[] cols) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        // delete_versions is overwritten with blank string ''
        String colString = targetDSid + " AS dataset_id, " + targetDSVersion + " AS version,  '' AS delete_versions";
        int numOfCols = cols.length;

        for (int i = 4; i < numOfCols; i++)
            colString += ", " + cols[i];

        return colString;
    }

    private String getTargetColString(String[] cols) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        String colString = cols[1];
        int numOfCols = cols.length;

        for (int i = 2; i < numOfCols; i++)
            colString += ", " + cols[i];

        return colString;
    }

}

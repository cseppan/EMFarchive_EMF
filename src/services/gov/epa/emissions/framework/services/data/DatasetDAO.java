package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.StringTokenizer;

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
    
    public long getDatasetRecordsNumber(DbServer dbServer, Session session, EmfDataset dataset, Version version) throws SQLException {
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
            throw new SQLException("Cannot get total records number on dataset: " + dataset.getName() +
                    " Reason: " + e.getMessage());
        } finally {
            dbServer.disconnect();
        }
        
        return totalCount;
    }

    private String getWhereClause(Version version, Session session) {
        String versions = versionsList(version, session);
        String deleteClause = createDeleteClause(versions);

        String whereClause = " WHERE dataset_id = " + version.getDatasetId() + " AND version IN ("
                + versions + ") AND " + deleteClause;
        
        return whereClause;
    }

    private String createDeleteClause(String versions) {
        StringBuffer buffer = new StringBuffer();

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken();
            String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
            buffer.append(" delete_versions NOT SIMILAR TO '" + regex + "'");

            if (tokenizer.hasMoreTokens())
                buffer.append(" AND ");
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

}

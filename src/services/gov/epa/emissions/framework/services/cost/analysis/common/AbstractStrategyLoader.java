package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.other.StrategyMessagesFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.hibernate.Session;

public abstract class AbstractStrategyLoader implements StrategyLoader {

    protected TableFormat detailedResultTableFormat;

    protected ControlStrategy controlStrategy;
    
    protected double totalCost = 0.0;

    protected double totalReduction = 0.0;
    
//    private DecimalFormat decFormat;

    protected RecordGenerator recordGenerator;
    
    protected int recordCount = 0;
    
    protected Datasource datasource;
    
    protected DatasetCreator creator;
    
    protected HibernateSessionFactory sessionFactory;
    
    protected User user;
    
    protected DbServerFactory dbServerFactory;
    
    protected DbServer dbServer;
    
    protected Keywords keywords;

    private DatasetType controlStrategyDetailedResultDatasetType;
    
    private StrategyResultType detailedStrategyResultType;
    
    private String filterForSourceQuery;

    protected int daysInMonth = 31; //useful only if inventory is monthly based and not yearly.

    protected int month = -1; //useful only if inventory is monthly based and not yearly.

    private StatusDAO statusDAO;
    
    protected ControlStrategyResult[] results;

    protected ControlStrategyDAO controlStrategyDAO;
    
    protected ControlStrategyResult strategyMessagesResult;

    public AbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.controlStrategy = controlStrategy;
//        this.decFormat = new DecimalFormat("0.###E0");
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.dbServer = dbServerFactory.getDbServer();
        this.detailedResultTableFormat = new StrategyDetailedResultTableFormat(dbServer.getSqlDataTypes());
        this.datasource = dbServer.getEmissionsDatasource();
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.statusDAO = new StatusDAO(sessionFactory);
        this.controlStrategyDAO = new ControlStrategyDAO(dbServerFactory, sessionFactory);
        this.results = getControlStrategyResults();
    }

    //call this to process the input and create the output in a batch fashion
    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        return null;
    }

    //implement code that is specific to the strategy type
    abstract protected void doBatchInsert(ResultSet resultSet) throws Exception;

    public final void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }

    }

    protected ControlStrategyResult createStrategyResult(EmfDataset inputDataset, int inputDatasetVersion) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inputDataset);
        result.setInputDatasetVersion(inputDatasetVersion);
        result.setDetailedResultDataset(createResultDataset(inputDataset));
        
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing dataset");

        //persist result
        saveControlStrategyResult(result);
        return result;
    }

    protected void saveControlStrategyResult(ControlStrategyResult strategyResult) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.updateControlStrategyResult(strategyResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected void deleteDatasets(EmfDataset[] datasets) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.removeResultDatasets(datasets, user, session, dbServer);
        } catch (RuntimeException e) {
            throw new EmfException("Could not delete control strategy result dataset(s): " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected StrategyResultType getDetailedStrategyResultType() throws EmfException {
        if (detailedStrategyResultType == null) {
            Session session = sessionFactory.getSession();
            try {
                detailedStrategyResultType = controlStrategyDAO.getDetailedStrategyResultType(session);
            } catch (RuntimeException e) {
                throw new EmfException("Could not get detailed strategy result type");
            } finally {
                session.close();
            }
        }
        return detailedStrategyResultType;
    }

    private EmfDataset createResultDataset(EmfDataset inputDataset) throws EmfException {
        return creator.addDataset("Strategy", "CSDR", 
                inputDataset, getControlStrategyDetailedResultDatasetType(), 
                detailedResultTableFormat);
    }

    private EmfDataset createStrategyMessagesDataset(EmfDataset inventory) throws Exception {
      return creator.addDataset("DS", 
              DatasetCreator.createDatasetName(inventory.getName() + "_strategy_msgs"), 
              getDatasetType("Strategy Messages (CSV)"), 
              new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
              strategyMessagesDatasetDescription());
    }
  
    protected EmfDataset createStrategyMessagesDataset(String namePrefix, EmfDataset inventory) throws Exception {
        return creator.addDataset("DS", 
                DatasetCreator.createDatasetName(namePrefix + "_" + inventory.getName() + "_strategy_msgs"), 
                getDatasetType("Strategy Messages (CSV)"), 
                new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
                strategyMessagesDatasetDescription());
      }
    
    private String strategyMessagesDatasetDescription() {
        return "#Strategy Messages\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
    }

    protected DatasetType getControlStrategyDetailedResultDatasetType() {
        if (controlStrategyDetailedResultDatasetType == null) {
            Session session = sessionFactory.getSession();
            try {
                controlStrategyDetailedResultDatasetType = new DatasetTypesDAO().get(DatasetType.strategyDetailedResult, session);
            } finally {
                session.close();
            }
        }
        return controlStrategyDetailedResultDatasetType;
    }

    protected OptimizedTableModifier dataModifier(String table) throws EmfException {
        try {
            return new OptimizedTableModifier(datasource, table);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public final int getRecordCount() {
        return recordCount;
    }
    
    protected boolean inventoryHasTargetPollutant(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();

        String query = "SELECT 1 as Found "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + " and poll = '" + controlStrategy.getTargetPollutant().getName() + "' "
            + getFilterForSourceQuery() + " limit 1;";
        //System.out.println(System.currentTimeMillis() + " " + query);
        ResultSet rs = null;
        Statement statement = null;
        try {
            statement = datasource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
//            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                if (rs.getInt(1) > 0)
                    return true;
            }
            rs.close();
            rs = null;
            statement.close();
            statement = null;
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { /**/ }
                rs = null;
            }
            if (statement != null) {
                try { statement.close(); } catch (SQLException e) { /**/ }
                statement = null;
            }
        }
        return false;
    }

    public void makeSureInventoryDatasetHasIndexes(ControlStrategyInputDataset controlStrategyInputDataset) {
        String query = "SELECT public.create_orl_table_indexes('" + emissionTableName(controlStrategyInputDataset.getInputDataset()).toLowerCase() + "');analyze " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()).toLowerCase() + ";";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    private String getFilterFromRegionDataset() {
        if (controlStrategy.getCountyDataset() == null) return "";
        String sqlFilter = "";
        String versionedQuery = new VersionedQuery(version(controlStrategy.getCountyDataset().getId(), controlStrategy.getCountyDatasetVersion())).query();
        String query = "SELECT distinct fips "
            + " FROM " + qualifiedEmissionTableName(controlStrategy.getCountyDataset()) 
            + " where " + versionedQuery;
//        ResultSet rs = null;
//        try {
//            rs = datasource.query().executeQuery(query);
//            while (rs.next()) {
//                if (sqlFilter.length() > 0) {
//                    sqlFilter += ",'" + rs.getString(1) + "'";
//                } else {
//                    sqlFilter = "'" + rs.getString(1) + "'";
//                }
//            }
//        } catch (SQLException e) {
//            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
//        } finally {
//            if (rs != null)
//                try {
//                    rs.close();
//                } catch (SQLException e) {
//                    //
//                }
//        }
        return sqlFilter.length() > 0 ? " and fips in (" + query + ")" : "" ;
    }

    protected void setResultTotalCostTotalReductionAndCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT count(1) as record_count, sum(Annual_Cost) as total_cost, sum(case when poll = '" + controlStrategy.getTargetPollutant().getName() 
            + "' then Emis_Reduction else null end) as total_reduction "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
                controlStrategyResult.setRecordCount(recordCount);
                controlStrategyResult.setTotalCost(rs.getDouble(2));
                controlStrategyResult.setTotalReduction(rs.getDouble(3));
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
    }

    protected void setResultCount(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT count(1) as record_count "
            + " FROM " + qualifiedEmissionTableName(controlStrategyResult.getDetailedResultDataset());
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                recordCount = rs.getInt(1);
                controlStrategyResult.setRecordCount(recordCount);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
    }

    public String getFilterForSourceQuery() {
        if (filterForSourceQuery == null) {
            String sqlFilter = getFilterFromRegionDataset();
            String filter = controlStrategy.getFilter();
            
            //get and build strategy filter...
            if (filter == null || filter.trim().length() == 0)
                sqlFilter = "";
            else 
                sqlFilter = " and (" + filter + ") "; 

            filterForSourceQuery = sqlFilter;
        }
        return filterForSourceQuery;
    }

    protected Version version(ControlStrategyInputDataset controlStrategyInputDataset) {
        return version(controlStrategyInputDataset.getInputDataset().getId(), controlStrategyInputDataset.getVersion());
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }

    protected String qualifiedEmissionTableName(Dataset dataset) {
        return qualifiedName(emissionTableName(dataset));
    }

    protected String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    protected String qualifiedName(String table) {
        return datasource.getName() + "." + table;
    }

    protected int getDaysInMonth(int month) {
        return month != - 1 ? DateUtil.daysInMonth(controlStrategy.getInventoryYear(), month) : 31;
    }
    
    protected double getEmission(double annEmis, double avdEmis) {
        return month != - 1 ? (avdEmis == 0.0 ? annEmis : avdEmis * daysInMonth) : annEmis;
    }
    
    protected void updateDataset(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            dao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not update dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }

    protected void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("Strategy");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDAO.add(endStatus);
    }
    
    public ControlStrategyResult[] getControlStrategyResults() {
        if (results == null) {
            Session session = sessionFactory.getSession();
            try {
                results = controlStrategyDAO.getControlStrategyResults(controlStrategy.getId(), session).toArray(new ControlStrategyResult[0]);
            } finally {
                session.close();
            }
        }
        return results;
    }
    
    protected double getUncontrolledEmission(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();
        int month = controlStrategyInputDataset.getInputDataset().applicableMonth();
        int daysInMonth = getDaysInMonth(month);
        double uncontrolledEmission = 0.0D;
        
        String query = "SELECT sum("  + (month != -1 ? "coalesce(avd_emis * " + daysInMonth + ", ann_emis)" : "ann_emis") + ") "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + " and poll = '" + controlStrategy.getTargetPollutant().getName() + "' "
            + getFilterForSourceQuery() + ";";
        ResultSet rs = null;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                uncontrolledEmission = rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return uncontrolledEmission;
    }

    protected void createDetailedResultTableIndexes(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.create_strategy_detailed_result_table_indexes('" + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + "');analyze emissions." + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + ";";
        
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    protected void removeControlStrategyResult(int resultId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), resultId, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not remove previous control strategy result(s)");
        } finally {
            session.close();
        }
    }
    
    public int getMessageDatasetRecordCount() {
        return strategyMessagesResult != null ? strategyMessagesResult.getRecordCount() : 0;
    }
    
    public ControlStrategyResult getStrategyMessagesResult() {
        return strategyMessagesResult;
    }

    private StrategyResultType getStrategyMessagesResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.strategyMessages, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private DatasetType getDatasetType(String name) {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
    }

    protected ControlStrategyResult createStrategyMessagesResult(EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inventory);
        result.setInputDatasetVersion(inventoryVersion);
        result.setDetailedResultDataset(createStrategyMessagesDataset(inventory));

        result.setStrategyResultType(getStrategyMessagesResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing strategy messages result");

        //persist result
        saveControlStrategyResult(result);

        return result;
    }
    
    protected ControlStrategyResult createStrategyMessagesResult(String namePrefix, EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inventory);
        result.setInputDatasetVersion(inventoryVersion);
        result.setDetailedResultDataset(createStrategyMessagesDataset(namePrefix, inventory));

        result.setStrategyResultType(getStrategyMessagesResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing strategy messages result");

        //persist result
        saveControlStrategyResult(result);

        return result;
    }
    
    protected void deleteStrategyMessageResult(ControlStrategyResult strategyMessagesResult) throws EmfException {
        //get rid of strategy results...
        Session session = sessionFactory.getSession();
        try {
            EmfDataset[] ds = controlStrategyDAO.getResultDatasets(controlStrategy.getId(), strategyMessagesResult.getId(), session);
            
            //get rid of old strategy results...
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), strategyMessagesResult.getId(), session);
            //delete and purge datasets
            controlStrategyDAO.removeResultDatasets(ds, user, session, dbServer);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove control strategy message result.");
        } finally {
            session.close();
        }
    }

    protected void populateStrategyMessagesDataset(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult strategyMessagesResult, ControlStrategyResult detailedResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_max_emis_red_strategy_messages("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + strategyMessagesResult.getId() + ", " + detailedResult.getId() + ");";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }
}
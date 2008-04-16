package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
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
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.Pollutants;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public abstract class AbstractStrategyLoader implements StrategyLoader {

    protected TableFormat detailedResultTableFormat;

    protected OptimizedTableModifier modifier;

    protected ControlStrategy controlStrategy;
    
    protected CostYearTable costYearTable;

    protected double totalCost = 0.0;

    protected double totalReduction = 0.0;
    
//    private DecimalFormat decFormat;

    protected RecordGenerator recordGenerator;
    
    protected int recordCount = 0;
    
    protected Datasource datasource;
    
    private int batchSize;

    protected Pollutants pollutants;

    protected boolean pointDatasetType;
    
    protected DatasetCreator creator;
    
    protected HibernateSessionFactory sessionFactory;
    
    protected User user;
    
    protected DbServerFactory dbServerFactory;
    
    protected DbServer dbServer;
    
    protected Keywords keywords;

    private DatasetType controlStrategyDetailedResultDatasetType;
    
    private StrategyResultType detailedStrategyResultType;
    
    private String filterForSourceQuery;

    protected String sourceScc = "";
    
    protected String sourceFips = "";

    protected String sourcePlantId = "";
    
    protected String sourcePointId = "";

    protected String sourceStackId = "";
    
    protected String sourceSegment = "";

    protected boolean newSource;

    protected long matchTime;
    
    protected long insertSourceTime;

    protected long getSourceTime;
    
    protected long getSourceMeasuresTime;
    
    protected long currentTime;
    
    protected int daysInMonth = 31; //useful only if inventory is monthly based and not yearly.

    protected int month = -1; //useful only if inventory is monthly based and not yearly.

    protected boolean useSQLApproach;
    protected String strategyType;

    private StatusDAO statusDAO;
    
    protected ControlStrategyResult[] results;

    private ControlStrategyDAO controlStrategyDAO;
    
    public AbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize, boolean useSQLApproach) throws EmfException {
        this(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
        this.useSQLApproach = useSQLApproach;
    }

    public AbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.controlStrategy = controlStrategy;
        this.batchSize = batchSize;
//        this.decFormat = new DecimalFormat("0.###E0");
        this.pollutants = new Pollutants(sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.dbServer = dbServerFactory.getDbServer();
        this.detailedResultTableFormat = new StrategyDetailedResultTableFormat(dbServer.getSqlDataTypes());
        this.datasource = dbServer.getEmissionsDatasource();
        this.creator = new DatasetCreator(controlStrategy, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.costYearTable = new CostYearTableReader(dbServer, controlStrategy.getCostYear()).costYearTable();
        this.strategyType = controlStrategy.getStrategyType().getName();
        this.statusDAO = new StatusDAO(sessionFactory);
        this.controlStrategyDAO = new ControlStrategyDAO(dbServerFactory, sessionFactory);
        this.results = getControlStrategyResults();
    }

    //call this to process the input and create the output in a batch fashion
    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //make sure inventory has indexes created...
        makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset);
        //make sure inventory has the target pollutant, if not don't run
        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
        }
        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        sourceScc = "";
        sourceFips = "";
        sourcePlantId = "";
        sourcePointId = "";
        sourceStackId = "";
        sourceSegment = "";
        newSource = false;
        month = inputDataset.applicableMonth();
        daysInMonth = getDaysInMonth(month);
        
        //setup result
        ControlStrategyResult result = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
//        if (!useSQLApproach) {
            
//            //set class level variable if inputdataset is a point type
//            this.pointDatasetType = inputDataset.getDatasetType().getName().equalsIgnoreCase(DatasetType.orlPointInventory)
//                || inputDataset.getDatasetType().getName().equalsIgnoreCase("ORL CoST Point Inventory (PTINV)");
//            //get the record generator appropriate for the input/output Dataset
//            this.recordGenerator = new RecordGeneratorFactory(costYearTable, inputDataset.getDatasetType(), result, decFormat, controlStrategy.getDiscountRate(), controlStrategy.getUseCostEquations()).getRecordGenerator();
//            //get the OptimizedQuery, really just a batch of resultsets
//            OptimizedQuery optimizedQuery = sourceQuery(controlStrategyInputDataset);
//            //get the OptimizedTableModifier so we can batch insert output
//            modifier = dataModifier(emissionTableName(result.getDetailedResultDataset()));
//            try {
//                modifier.start();
//                long startTime;
//                long endTime;
//                startTime = System.currentTimeMillis();
//                boolean hasSources = optimizedQuery.execute();
//                endTime = System.currentTimeMillis();
//                getSourceTime += endTime - startTime;
//                System.out.println("Get batch of sources for strategy, in " + ((endTime - startTime) / (1000))  + " secs");
//                while (hasSources) {
//                    ResultSet resultSet = optimizedQuery.getResultSet();
//                    doBatchInsert(resultSet);
//                    resultSet.close();
//                    startTime = System.currentTimeMillis();
//                    hasSources = optimizedQuery.execute();
//                    endTime = System.currentTimeMillis();
//                    getSourceTime += endTime - startTime;
//                    System.out.println("Get batch of sources for strategy, in " + ((endTime - startTime) / (1000))  + " secs");
//                }
//            } catch(Exception ex) {
//                ex.printStackTrace();
//                result.setRunStatus("Failed: " + ex.getMessage());
//            } finally {
//                currentTime = System.currentTimeMillis();
//                modifier.finish();
//                insertSourceTime += System.currentTimeMillis() - currentTime;
//                modifier.close();
//                optimizedQuery.close();
//            }
//            result.setTotalCost(totalCost);
//            result.setTotalReduction(totalReduction);
//            System.out.println("Total time to get all sources = " + (getSourceTime / (1000))  + " secs");
//            System.out.println("Total time to get measures for sources = " + (getSourceMeasuresTime / (1000))  + " secs");
//            System.out.println("Total time to match sources to measures = " + (matchTime / (1000))  + " secs");
//            System.out.println("Total time to insert results = " + (insertSourceTime / (1000))  + " secs");
//        } else {
            runStrategyUsingSQLApproach(controlStrategyInputDataset, result);
            runStrategyUsingSQLApproach2(controlStrategyInputDataset, result);
            if (strategyType.equals("Apply Measures In Series")) {
                runStrategyUsingSQLApproach3(controlStrategyInputDataset, result);
            }
            System.out.println(System.currentTimeMillis() + " done with");
            //still need to calculate the total cost and reduction...
            setResultTotalCostTotalReductionAndCount(result);
//        }
        return result;
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
            ControlStrategyDAO dao = new ControlStrategyDAO();
            dao.updateControlStrategyResult(strategyResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not save control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    protected StrategyResultType getDetailedStrategyResultType() throws EmfException {
        if (detailedStrategyResultType == null) {
            Session session = sessionFactory.getSession();
            try {
                detailedStrategyResultType = new ControlStrategyDAO().getDetailedStrategyResultType(session);
            } catch (RuntimeException e) {
                throw new EmfException("Could not get detailed strategy result type");
            } finally {
                session.close();
            }
        }
        return detailedStrategyResultType;
    }

    private EmfDataset createResultDataset(EmfDataset inputDataset) throws EmfException {
        return creator.addDataset("Strategy_", "CSDR_", 
                inputDataset, getControlStrategyDetailedResultDatasetType(), 
                detailedResultTableFormat);
    }

    protected DatasetType getControlStrategyDetailedResultDatasetType() {
        if (controlStrategyDetailedResultDatasetType == null) {
            Session session = sessionFactory.getSession();
            try {
                controlStrategyDetailedResultDatasetType = new DatasetTypesDAO().get("Control Strategy Detailed Result", session);
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

    protected void insertRecord(Record record, OptimizedTableModifier dataModifier) throws Exception {
        try {
            int colsSize = detailedResultTableFormat.cols().length;
            if (record.size() < colsSize)
                throw new EmfException("The number of tokens in the record are " + record.size()
                        + ", It's less than the number of columns expected(" + colsSize + ")");
            dataModifier.insert((String[]) record.tokens().toArray(new String[0]));
            ++recordCount;
        } catch (SQLException e) {
            throw new EmfException("Error processing insert query: " + e.getMessage());
        }
    }

    public final int getRecordCount() {
        return recordCount;
    }
    
    protected boolean inventoryHasTargetPollutant(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();

        String query = "SELECT DISTINCT ON (poll) 1 as Found "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + " and poll = '" + controlStrategy.getTargetPollutant().getName() + "' "
            + getFilterForSourceQuery() + " limit 1;";
        ResultSet rs = null;
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                if (rs.getInt(1) > 0)
                    return true;
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
        return false;
    }

    private void runStrategyUsingSQLApproach(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        if (strategyType.equals("Max Emissions Reduction")) {
            query = "SELECT public.run_max_emis_red_strategy("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        } else if (strategyType.equals("Apply Measures In Series")) {
            query = "SELECT public.run_apply_measures_in_series_strategy("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        }
//        String query = "SELECT public.run_apply_measures_in_series_strategy("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
//SELECT public.create_strategy_detailed_result_table_indexes('" + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + "');        
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //3
        }
    }

    private void runStrategyUsingSQLApproach2(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SET work_mem TO '128MB';SELECT public.create_strategy_detailed_result_table_indexes('" + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + "');analyze emissions." + emissionTableName(controlStrategyResult.getDetailedResultDataset()) + ";";
        
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void runStrategyUsingSQLApproach3(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "SELECT public.run_apply_measures_in_series_strategy_finalize("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    public void makeSureInventoryDatasetHasIndexes(ControlStrategyInputDataset controlStrategyInputDataset) {
        String query = "SELECT public.create_orl_table_indexes('" + emissionTableName(controlStrategyInputDataset.getInputDataset()).toLowerCase() + "')";
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    public String[] getSourcePollutantList(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();
        List<String> pollutants = new ArrayList<String>();
        String query = "SELECT distinct poll "
            + " FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + getFilterForSourceQuery();
        ResultSet rs = null;
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                pollutants.add(rs.getString(1));
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
        return pollutants.toArray(new String[0]);
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
        System.out.println(System.currentTimeMillis() + " " + query);
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

    protected OptimizedQuery sourceQuery(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String versionedQuery = new VersionedQuery(version(controlStrategyInputDataset)).query();

        String query = "SELECT *, case when poll = '" + controlStrategy.getTargetPollutant().getName() 
            + "' then 1 else 0 end as sort FROM " + qualifiedEmissionTableName(controlStrategyInputDataset.getInputDataset()) 
            + " where " + versionedQuery
            + getFilterForSourceQuery()
            + " order by scc, fips" 
            + (pointDatasetType ? ", plantid, pointid, stackid, segment" : "" ) 
            + ", sort desc, poll ";
        try {
            return datasource.optimizedQuery(query, batchSize);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
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
}
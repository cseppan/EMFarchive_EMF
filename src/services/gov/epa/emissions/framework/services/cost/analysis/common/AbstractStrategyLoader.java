package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.db.OptimizedTableModifier;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTableReader;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.cost.controlStrategy.io.CSCountyFileFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.io.CSCountyImporter;
import gov.epa.emissions.framework.services.cost.controlmeasure.io.Pollutants;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

import org.hibernate.Session;

public abstract class AbstractStrategyLoader implements StrategyLoader {

    private TableFormat tableFormat;

    protected OptimizedTableModifier modifier;

    protected ControlStrategy controlStrategy;
    
    protected CostYearTable costYearTable;

    protected double totalCost = 0.0;

    protected double totalReduction = 0.0;
    
    private DecimalFormat decFormat;

    protected RecordGenerator recordGenerator;
    
    protected long recordCount = 0;
    
    private Datasource datasource;
    
    private int batchSize;

    protected Pollutants pollutants;

    protected boolean pointDatasetType;
    
    private DatasetCreator creator;
    
    protected HibernateSessionFactory sessionFactory;
    
    protected User user;
    
    protected DbServerFactory dbServerFactory;
    
    protected DbServer dbServer;
    
    private Keywords keywords;

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

    public AbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        this.user = user;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.controlStrategy = controlStrategy;
        this.batchSize = batchSize;
        this.decFormat = new DecimalFormat("0.###E0");
        this.pollutants = new Pollutants(sessionFactory);
        this.keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        this.dbServer = dbServerFactory.getDbServer();
        this.tableFormat = new StrategyDetailedResultTableFormat(dbServer.getSqlDataTypes());
        this.datasource = dbServer.getEmissionsDatasource();
        this.creator = new DatasetCreator("Strategy_", "CSDR_", 
                controlStrategy, user, 
                sessionFactory, dbServerFactory,
                datasource, keywords);
        this.costYearTable = new CostYearTableReader(dbServer, controlStrategy.getCostYear()).costYearTable();
    }

    //call this to process the input and create the output in a batch fashion
    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //reset record counter
        recordCount = 0;
        //setup result
        ControlStrategyResult result = createStrategyResult(inputDataset);
        //set class level variable if inputdataset is a point type
        this.pointDatasetType = inputDataset.getDatasetType().getName().equalsIgnoreCase("ORL Point Inventory (PTINV)");
        //get the record generator appropriate for the input/output Dataset
        this.recordGenerator = new RecordGeneratorFactory(inputDataset.getDatasetType(), result, this.decFormat).getRecordGenerator();
        //get the OptimizedQuery, really just a batch of resultsets
        OptimizedQuery optimizedQuery = sourceQuery(inputDataset);
        //get the OptimizedTableModifier so we can batch insert output
        modifier = dataModifier(emissionTableName(result.getDetailedResultDataset()));
        try {
            modifier.start();
            while (optimizedQuery.execute()) {
                ResultSet resultSet = optimizedQuery.getResultSet();
                doBatchInsert(resultSet);
                resultSet.close();
            }
        } catch(Exception ex) {
            result.setRunStatus("Failed: " + ex.getMessage());
        } finally {
            modifier.finish();
            modifier.close();
            optimizedQuery.close();
        }
        result.setTotalCost(totalCost);
        result.setTotalReduction(totalReduction);
        return result;
    }

    //implement code that is specific to the strategy type
    public abstract void doBatchInsert(ResultSet resultSet) throws Exception;

    public final void disconnectDbServer() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
        }

    }
    
    private ControlStrategyResult createStrategyResult(EmfDataset inputDataset) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDatasetId(inputDataset.getId());
        result.setDetailedResultDataset(createResultDataset(inputDataset));
        
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Created for input dataset: " + inputDataset.getName());
        
        return result;
    }

    private StrategyResultType getDetailedStrategyResultType() throws EmfException {
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
        return creator.addDataset(inputDataset, getControlStrategyDetailedResultDatasetType(), 
                tableFormat);
    }

    private DatasetType getControlStrategyDetailedResultDatasetType() {
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

    private OptimizedTableModifier dataModifier(String table) throws EmfException {
        try {
            return new OptimizedTableModifier(datasource, table);
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }
    }

    protected void insertRecord(Record record, OptimizedTableModifier dataModifier) throws Exception {
        try {
            int colsSize = tableFormat.cols().length;
            if (record.size() < colsSize)
                throw new EmfException("The number of tokens in the record are " + record.size()
                        + ", It's less than the number of columns expected(" + colsSize + ")");
            dataModifier.insert((String[]) record.tokens().toArray(new String[0]));
            ++recordCount;
        } catch (SQLException e) {
            throw new EmfException("Error processing insert query: " + e.getMessage());
        }
    }

    public final long getRecordCount() {
        return recordCount;
    }
    
    private OptimizedQuery sourceQuery(EmfDataset inputDataset) throws EmfException {
        String query = "SELECT *, case when poll = '" + controlStrategy.getTargetPollutant().getName() 
            + "' then 1 else 0 end as sort FROM " + qualifiedEmissionTableName(inputDataset) 
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

    private String getFilterForSourceQuery() throws EmfException {
        if (filterForSourceQuery == null) {
            String sqlFilter = "";
            String filter = controlStrategy.getFilter();
            String countyFile = controlStrategy.getCountyFile();

            //get and build strategy filter...
            if (filter == null || filter.trim().length() == 0)
                sqlFilter = "";
            else 
                sqlFilter = " where " + filter; 

            //get and build county filter...
            if (countyFile != null && countyFile.trim().length() > 0) {
                CSCountyImporter countyImporter = new CSCountyImporter(new File(countyFile), 
                        new CSCountyFileFormat());
                String[] fips;
                try {
                    fips = countyImporter.run();
                } catch (ImporterException e) {
                    throw new EmfException(e.getMessage());
                }
                if (fips.length > 0) 
                    sqlFilter += (sqlFilter.length() == 0 ? " where " : " and ") 
                        + " fips in (";
                for (int i = 0; i < fips.length; i++) {
                    sqlFilter += (i > 0 ? "," : "") + "'" 
                        + fips[i] + "'";
                }
                if (fips.length > 0) 
                    sqlFilter += ")";
            }
            filterForSourceQuery = sqlFilter;
        }
        return filterForSourceQuery;
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

}

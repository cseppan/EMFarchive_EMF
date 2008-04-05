package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.common.LeastCostCurveSummaryTableFormat;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLeastCostCMWorksheetTableFormat;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class StrategyLoader extends AbstractStrategyLoader {
    
    private ControlStrategyResult leastCostCMWorksheetResult;
    
    private ControlStrategyResult leastCostCurveSummaryResult;

    private double maxEmisReduction;

    private double uncontrolledEmis;

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize, boolean useSQLApproach) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public ControlStrategyResult loadLeastCostCMWorksheetResult() throws EmfException {
        this.leastCostCMWorksheetResult = createLeastCostCMWorksheetResult();
        return this.leastCostCMWorksheetResult;
    }
    
    public ControlStrategyResult loadLeastCostCurveSummaryResult() throws EmfException {
        this.leastCostCurveSummaryResult = createLeastCostCurveSummaryResult();
        return this.leastCostCurveSummaryResult;
    }
    
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
        
        //setup result
        populateWorksheet(controlStrategyInputDataset);
        
        Double pctRedIncrement = controlStrategy.getConstraint().getDomainWidePctReductionIncrement();
        Double pctRed = 0.0;
        Double pctRedStart = controlStrategy.getConstraint().getDomainWidePctReductionStart();
        if (pctRedStart == null || pctRedStart == 0) pctRedStart = pctRedIncrement;
        Double pctRedEnd = controlStrategy.getConstraint().getDomainWidePctReductionEnd();
        if (pctRedEnd == null) pctRedEnd = 100.0;
        if (pctRedEnd > maxEmisReduction / uncontrolledEmis * 100) pctRedEnd = maxEmisReduction / uncontrolledEmis * 100;
        
        for (pctRed = pctRedStart; pctRed < pctRedEnd + pctRedIncrement; pctRed += pctRedIncrement) {
            ControlStrategyResult result = createStrategyResult(pctRed, inputDataset, controlStrategyInputDataset.getVersion());
            populateDetailedResult(controlStrategyInputDataset, result, uncontrolledEmis * pctRed / 100);
            //still need to calculate the total cost and reduction...
            setResultTotalCostTotalReductionAndCount(result);
            result.setCompletionTime(new Date());
            result.setRunStatus("Completed.");
            saveControlStrategyResult(result);
        }
        
        return null;
    }

    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        //
    }

    private EmfDataset createDataset() throws EmfException {
        //"LeatCostCM_", 
        return creator.addDataset("CSLCM_", 
                DatasetCreator.createDatasetName("Measure Worksheet " + controlStrategy.getName()), getControlStrategyLeastCostCMWorksheetDatasetType(), 
                new StrategyLeastCostCMWorksheetTableFormat(dbServer.getSqlDataTypes()), leastCostCMWorksheetDescription());
    }

    private EmfDataset createLeastCostCurveSummaryDataset() throws EmfException {
        //"LeatCostCM_", 
        return creator.addDataset("CSLCCS_", 
                DatasetCreator.createDatasetName("Cost Curve Summary " + controlStrategy.getName()), getControlStrategyLeastCostCurveSummaryDatasetType(), 
                new LeastCostCurveSummaryTableFormat(dbServer.getSqlDataTypes()), leastCostCurveSummaryDescription());
    }

    private EmfDataset createResultDataset(double targetPctRedcution, EmfDataset inputDataset) throws EmfException {
        return creator.addDataset("pct_" + targetPctRedcution + "_" + controlStrategy.getName(), 
                inputDataset, getControlStrategyDetailedResultDatasetType(), 
                detailedResultTableFormat, creator.detailedResultDescription(inputDataset));
    }

    protected ControlStrategyResult createStrategyResult(double targetPctRedcution, EmfDataset inputDataset, int inputDatasetVersion) throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inputDataset);
        result.setInputDatasetVersion(inputDatasetVersion);
        result.setDetailedResultDataset(createResultDataset(targetPctRedcution, inputDataset));
        
        result.setStrategyResultType(getDetailedStrategyResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing dataset");

        //persist result
        saveControlStrategyResult(result);
        return result;
    }

    private String leastCostCMWorksheetDescription() {
        return "#Control strategy least cost control measure worksheet\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
        }

    private String leastCostCurveSummaryDescription() {
        return "#Control strategy least cost curve summary\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
        }

   private DatasetType getControlStrategyLeastCostCMWorksheetDatasetType() {
       Session session = sessionFactory.getSession();
       try {
           return new DatasetTypesDAO().get("Control Strategy Least Cost Control Measure Worksheet", session);
       } finally {
           session.close();
       }
   }

   private DatasetType getControlStrategyLeastCostCurveSummaryDatasetType() {
       Session session = sessionFactory.getSession();
       try {
           return new DatasetTypesDAO().get("Control Strategy Least Cost Curve Summary", session);
       } finally {
           session.close();
       }
   }

    private ControlStrategyResult createLeastCostCMWorksheetResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setDetailedResultDataset(createDataset());

        result.setStrategyResultType(getLeastCostCMWorksheetResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing LeastCost CM Worksheet result");

        //persist result
        saveControlStrategyResult(result);
        
        //create indexes on the datasets table...
        createLeastCostCMWorksheetIndexes((EmfDataset)result.getDetailedResultDataset());

        return result;
    }

    private ControlStrategyResult createLeastCostCurveSummaryResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setDetailedResultDataset(createLeastCostCurveSummaryDataset());

        result.setStrategyResultType(getLeastCostCurveSummaryResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing LeastCost Curve Summary result");

        //persist result
        saveControlStrategyResult(result);
        
        return result;
    }

    private StrategyResultType getLeastCostCMWorksheetResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.leastCostControlMeasureWorksheetResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private StrategyResultType getLeastCostCurveSummaryResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.leastCostCurveSummaryResult, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private void populateWorksheet(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String query = "";
        query = "SELECT * from public.populate_least_cost_strategy_worksheet("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ");";
        ResultSet rs = null;
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                uncontrolledEmis = rs.getDouble(1);
                maxEmisReduction = rs.getDouble(2);
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

    private void populateDetailedResult(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult,
            double emisReduction) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        String query = "";
        query = "SELECT public.populate_least_cost_strategy_detailed_result("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ", " + emisReduction + "::double precision);";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            dbServer.getEmissionsDatasource().query().executeQuery(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void createLeastCostCMWorksheetIndexes(EmfDataset leastCostCMWorksheetDataset) {
        String query = "SELECT public.create_least_cost_worksheet_table_indexes('" + emissionTableName(leastCostCMWorksheetDataset).toLowerCase() + "')";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

}

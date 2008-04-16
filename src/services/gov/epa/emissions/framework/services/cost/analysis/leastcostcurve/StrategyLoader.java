package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.VersionedQuery;
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
        if (controlStrategy.getDeleteResults() || results.length == 0)
            populateWorksheet(controlStrategyInputDataset);
        else {
            for (ControlStrategyResult result : results) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheetResult)) {
                    leastCostCMWorksheetResult = result;
                } else if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostCurveSummaryResult)) {
                    leastCostCurveSummaryResult = result;
                }
            }
            resetWorksheetMeasureStatus(leastCostCMWorksheetResult);
            
            //also get the uncontrolled emission...
            uncontrolledEmis = getUncontrolledEmission(controlStrategyInputDataset);
            maxEmisReduction = getMaximumEmissionReduction(leastCostCMWorksheetResult);
        }
        
        Double pctRedIncrement = controlStrategy.getConstraint().getDomainWidePctReductionIncrement();
        Double pctRed = 0.0;
        Double pctRedStart = controlStrategy.getConstraint().getDomainWidePctReductionStart();
        if (pctRedStart == null || pctRedStart == 0) pctRedStart = pctRedIncrement;
        Double pctRedEnd = controlStrategy.getConstraint().getDomainWidePctReductionEnd();
        if (pctRedEnd == null) pctRedEnd = 100.0;
        if (maxEmisReduction > 0) if (pctRedEnd > maxEmisReduction / uncontrolledEmis * 100) pctRedEnd = maxEmisReduction / uncontrolledEmis * 100;
        for (pctRed = pctRedStart; pctRed < pctRedEnd + pctRedIncrement; pctRed += pctRedIncrement) {

            setStatus("Populating the " + pctRed + " percent target detailed result.");

            ControlStrategyResult result = createStrategyResult(pctRed, inputDataset, controlStrategyInputDataset.getVersion());
            populateDetailedResult(controlStrategyInputDataset, result, uncontrolledEmis * pctRed / 100);

            //still need to calculate the total cost and reduction...
            setResultTotalCostTotalReductionAndCount(result);

            //add summary information as keywords to the detailed result dataset
            addDetailedResultSummaryDatasetKeywords((EmfDataset)result.getDetailedResultDataset(), uncontrolledEmis * pctRed / 100);
            
            //finalize status
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

    private double getUncontrolledEmission(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
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
        System.out.println(System.currentTimeMillis() + " " + query);
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

    private double getMaximumEmissionReduction(ControlStrategyResult leastCostCMWorksheetResult) throws EmfException {
        double maximumEmissionReduction = 0.0D;
        String query = "SELECT cum_emis_reduction from ("
            + "SELECT public.run_sum(emis_reduction::numeric, 'max_emis_reduction'::text) as cum_emis_reduction"
            + " from ("
            + " SELECT distinct on (scc, fips, plantid, pointid, stackid, segment)"
            + " emis_reduction"
            + " FROM " + qualifiedEmissionTableName(leastCostCMWorksheetResult.getDetailedResultDataset()) 
            + " where status is null "
            + " and poll = '" + controlStrategy.getTargetPollutant().getName() + "'"
            + " ORDER BY scc, fips, plantid, pointid, stackid, segment, marginal desc, emis_reduction, record_id desc"
            + " ) tbl"
            + " ) tbl order by cum_emis_reduction desc limit 1";

        ResultSet rs = null;
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                maximumEmissionReduction = rs.getDouble(1);
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
        return maximumEmissionReduction;
    }

    private void resetWorksheetMeasureStatus(ControlStrategyResult leastCostCMWorksheetResult) throws EmfException {
        String query = "";
        query = "update " + qualifiedEmissionTableName(leastCostCMWorksheetResult.getDetailedResultDataset()) + " set status = null where status = 1;";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void populateDetailedResult(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult,
            double emisReduction) throws EmfException {
        String query = "";
        //, " + rnd.nextInt() + "::integer
        query = "SELECT public.populate_least_cost_strategy_detailed_result("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ", " + emisReduction + "::double precision);";
        System.out.println(System.currentTimeMillis() + " " + query);
        DbServer dbSvr = dbServerFactory.getDbServer();
        try {
            dbSvr.getEmissionsDatasource().query().executeQuery(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            try {
                dbSvr.disconnect();
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

    private void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywords.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addDetailedResultSummaryDatasetKeywords(EmfDataset dataset,
            double emisReduction) throws EmfException {
        String query = "select TO_CHAR(sum(annual_cost), 'FM999999999999999990.09')::double precision as total_annual_cost, "
            + "TO_CHAR(sum(annual_cost) / sum(emis_reduction), 'FM999999999999999990.09')::double precision as average_ann_cost_per_ton, "
            + "TO_CHAR(sum(annual_oper_maint_cost), 'FM999999999999999990.09')::double precision as Total_Annual_Oper_Maint_Cost, "
            + "TO_CHAR(sum(annualized_capital_cost), 'FM999999999999999990.09')::double precision as Total_Annualized_Capital_Cost, "
            + "TO_CHAR(sum(total_capital_cost), 'FM999999999999999990.09')::double precision as Total_Capital_Cost, "
            + "TO_CHAR(" + emisReduction + " / " + uncontrolledEmis + " * 100, 'FM990.099')::double precision as Target_Percent_Reduction, " 
            + "TO_CHAR(sum(emis_reduction) / " + uncontrolledEmis + " * 100, 'FM990.099')::double precision as Actual_Percent_Reduction, "
            + "sum(emis_reduction) as Total_Emis_Reduction " 
            + "FROM " + qualifiedEmissionTableName(dataset)
            + " where poll='" + controlStrategy.getTargetPollutant().getName() + "'"
            + " group by poll";
//        System.out.println(System.currentTimeMillis() + " " + query);
        ResultSet rs = null;
        try {
            rs = datasource.query().executeQuery(query);
            while (rs.next()) {
                addKeyVal(dataset, "TOTAL_ANNUAL_COST", rs.getDouble("total_annual_cost") + "");
                addKeyVal(dataset, "AVERAGE_ANNUAL_COST_PER_TON", rs.getDouble("average_ann_cost_per_ton") + "");
                addKeyVal(dataset, "TOTAL_ANNUAL_OPERATION_MAINTENANCE_COST", rs.getDouble("Total_Annual_Oper_Maint_Cost") + "");
                addKeyVal(dataset, "TOTAL_ANNUALIZED_CAPITAL_COST", rs.getDouble("Total_Annualized_Capital_Cost") + "");
                addKeyVal(dataset, "TOTAL_CAPITAL_COST", rs.getDouble("Total_Capital_Cost") + "");
                addKeyVal(dataset, "TARGET_PERCENT_REDUCTION", rs.getDouble("Target_Percent_Reduction") + "");
                addKeyVal(dataset, "ACTUAL_PERCENT_REDUCTION", rs.getDouble("Actual_Percent_Reduction") + "");
                addKeyVal(dataset, "TOTAL_EMISSION_REDUCTION", rs.getDouble("Total_Emis_Reduction") + "");
                addKeyVal(dataset, "UNCONTROLLED_EMISSION", uncontrolledEmis + "");
                try {
                    updateDataset(dataset);
                } catch (Exception e) {
                    //suppress exceptions for now
                }
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

}

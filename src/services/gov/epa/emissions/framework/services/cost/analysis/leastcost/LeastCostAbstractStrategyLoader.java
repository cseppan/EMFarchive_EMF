package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
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

public class LeastCostAbstractStrategyLoader extends AbstractStrategyLoader {
    
    protected ControlStrategyResult leastCostCMWorksheetResult;
    
    protected ControlStrategyResult leastCostCurveSummaryResult;

    protected double maxEmisReduction;

    protected double uncontrolledEmis;

    public LeastCostAbstractStrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadLeastCostCMWorksheetResult() throws EmfException {
        this.leastCostCMWorksheetResult = createLeastCostCMWorksheetResult();
        return this.leastCostCMWorksheetResult;
    }
    
    public ControlStrategyResult loadLeastCostCurveSummaryResult() throws EmfException {
        this.leastCostCurveSummaryResult = createLeastCostCurveSummaryResult();
        return this.leastCostCurveSummaryResult;
    }
    
    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        //
    }

    private EmfDataset createDataset() throws EmfException {
        //"LeatCostCM_", 
        return creator.addDataset("CSLCM", 
                DatasetCreator.createDatasetName("Measure Worksheet " + controlStrategy.getName()), getControlStrategyLeastCostCMWorksheetDatasetType(), 
                new StrategyLeastCostCMWorksheetTableFormat(dbServer.getSqlDataTypes()), leastCostCMWorksheetDescription());
    }

    private EmfDataset createLeastCostCurveSummaryDataset() throws EmfException {
        //"LeatCostCM_", 
        return creator.addDataset("CSLCCS", 
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

   protected ControlStrategyResult createLeastCostCMWorksheetResult() throws EmfException {
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

    protected ControlStrategyResult createLeastCostCurveSummaryResult() throws EmfException {
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

    protected StrategyResultType getLeastCostCMWorksheetResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.leastCostControlMeasureWorksheet, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    protected StrategyResultType getLeastCostCurveSummaryResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.leastCostCurveSummary, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    protected void populateWorksheet(ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException {
        String query = "";
        String query2 = "";
        String query3 = "";
        query = "SELECT public.populate_least_cost_strategy_worksheet("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ");";
        query2 = "analyze "  + qualifiedEmissionTableName(leastCostCMWorksheetResult.getDetailedResultDataset()) + ";";
        query3 = "SELECT public.eliminate_least_cost_strategy_source_measures("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ");analyze "  + qualifiedEmissionTableName(leastCostCMWorksheetResult.getDetailedResultDataset()) + ";";
        try {
            datasource.query().execute(query);
            System.out.println(System.currentTimeMillis() + " finished " + query);
            datasource.query().execute(query2);
            System.out.println(System.currentTimeMillis() + " finished " + query2);
            datasource.query().execute(query3);
            System.out.println(System.currentTimeMillis() + " finished " + query3);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    protected double getMaximumEmissionReduction(ControlStrategyResult leastCostCMWorksheetResult) throws EmfException {
        double maximumEmissionReduction = 0.0D;
        String query = "SELECT sum(emis_reduction) "
            + " from ( "
            + " SELECT distinct on (source) " 
            + "    emis_reduction "
            + " FROM " + qualifiedEmissionTableName(leastCostCMWorksheetResult.getDetailedResultDataset()) 
            + " where status is null " 
            + "    and poll = '" + controlStrategy.getTargetPollutant().getName() + "' "
            + " ORDER BY source, marginal desc, emis_reduction, record_id desc "
            + " ) tbl";

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

    protected void populateDetailedResult(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult,
            double emisReduction) throws EmfException {
        String query = "";
        //, " + rnd.nextInt() + "::integer
        query = "SELECT public.populate_least_cost_strategy_detailed_result("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ", " + emisReduction + "::double precision);";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().executeQuery(query);
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
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
    
    protected void addDetailedResultSummaryDatasetKeywords(EmfDataset dataset,
            double emisReduction) throws EmfException {
        String query = "select TO_CHAR(sum(annual_cost), 'FM999999999999999990.09')::double precision as total_annual_cost, "
            + "TO_CHAR(case when sum(emis_reduction) <> 0 then sum(annual_cost) / sum(emis_reduction) else null::double precision end, 'FM999999999999999990.09')::double precision as average_ann_cost_per_ton, "
            + "TO_CHAR(sum(annual_oper_maint_cost), 'FM999999999999999990.09')::double precision as Total_Annual_Oper_Maint_Cost, "
            + "TO_CHAR(sum(annualized_capital_cost), 'FM999999999999999990.09')::double precision as Total_Annualized_Capital_Cost, "
            + "TO_CHAR(sum(total_capital_cost), 'FM999999999999999990.09')::double precision as Total_Capital_Cost, "
            + "TO_CHAR(case when " + uncontrolledEmis + " <> 0 then " + emisReduction + " / " + uncontrolledEmis + " * 100 else null::double precision end, 'FM990.099')::double precision as Target_Percent_Reduction, " 
            + "TO_CHAR(case when " + uncontrolledEmis + " <> 0 then sum(emis_reduction) / " + uncontrolledEmis + " * 100 else null::double precision end, 'FM990.099')::double precision as Actual_Percent_Reduction, "
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

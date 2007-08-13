package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PointRecordGenerator implements RecordGenerator {
    private ControlStrategyResult strategyResult;
    private String comment = "";
    private double reducedEmission;
    private double invenControlEfficiency;
    private double invenRulePenetration;
    private double invenRuleEffectiveness;
    private double discountRate;
//    private double originalEmissions;
//    private double finalEmissions;

    private DecimalFormat decFormat;
    boolean useCostEquation;

    public PointRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat, double discountRate, boolean useCostEquation) {
        this.strategyResult = result;
        this.decFormat = decFormat;
        this.discountRate= discountRate; 
        this.useCostEquation=useCostEquation; 
    }

    public Record getRecord(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions,  boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM, originalEmissions, displayOriginalEmissions, displayFinalEmissions, true));

        return record;
    }

    public double reducedEmission() {
        return reducedEmission;
    }

    public List tokens(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions,
            boolean hasSICandNAICS) throws SQLException, EmfException {
        List<String> tokens = new ArrayList<String>();
        double effectiveReduction = maxCM.effectiveReduction();
        Double om;
        Double annulizedCCost;
        Double capitalCost;
        Double annualCost;
       
        calculateEmissionReduction(resultSet,maxCM);
        reducedEmission = originalEmissions * effectiveReduction;
        CostEquations costEquations = new CostEquationsFactory().getCostEquations(reducedEmission, maxCM, discountRate, useCostEquation);
        
        
        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());  //dataset ID
        tokens.add("" + 0);  // version
        tokens.add("");     //delete_versions

        tokens.add("false");    //disable
        tokens.add(maxCM.measure().getAbbreviation());  //measure abbreviation
        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        String fullFips = resultSet.getString("fips").trim();
        tokens.add(fullFips);  // 5 digit FIPS state+county code

        tokens.add(resultSet.getString("PLANTID")); //plant ID
        tokens.add(resultSet.getString("POINTID")); //point ID
        tokens.add(resultSet.getString("STACKID")); //stack ID
        tokens.add(resultSet.getString("SEGMENT")); //segment
        
//        tokens.add("" + (costEquations.getTotalAnnualizedCost() != null ? decFormat.format(costEquations.getTotalAnnualizedCost() : ""));
        annualCost=costEquations.getAnnualCost();
        tokens.add("" +  (annualCost!= null ? decFormat.format(annualCost) : ""));
                //maxCM.adjustedCostPerTon() * reducedEmission));//annual cost for source
        om=costEquations.getOperationMaintenanceCost();
        tokens.add("" + (om!= null ? decFormat.format(om) : ""));
        annulizedCCost=costEquations.getAnnualizedCapitalCost();
        tokens.add("" + (annulizedCCost != null ? decFormat.format(annulizedCCost) : ""));//capital cost
        capitalCost=costEquations.getCapitalCost();
        tokens.add("" + (capitalCost != null ? decFormat.format(capitalCost) : ""));//Total capital cost
        
        tokens.add("" + decFormat.format(maxCM.adjustedCostPerTon())); //annual cost per ton
        tokens.add("" + decFormat.format(maxCM.controlEfficiency()));   //control efficiency
        tokens.add("" + 100);
        tokens.add("" + maxCM.ruleEffectiveness()); //rule effectiveness
        tokens.add("" + decFormat.format(maxCM.effectiveReduction() * 100));

        tokens.add("" + invenControlEfficiency);    // inventory CE
        tokens.add("" + invenRulePenetration);      // inventory RP   
        tokens.add("" + invenRuleEffectiveness);    // inventory RE
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0));  //final emissions
        tokens.add("" + decFormat.format(reducedEmission));     // emissions reduction
        tokens.add("" + (displayOriginalEmissions ? originalEmissions : 0));    //inventory emissions

        tokens.add("" + fullFips.substring(fullFips.length()-5,2));  // FIPS state
        tokens.add("" + fullFips.substring(fullFips.length()-3));    // FIPS county - accounts for possible country code
        tokens.add("" + resultSet.getString("sic"));  // SIC
        tokens.add("" + resultSet.getString("naics")); // NAICS
        
        tokens.add("" + resultSet.getInt("Record_Id")); // sourceID from inventory
        tokens.add("" + strategyResult.getInputDatasetId());    //inputDatasetID
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + maxCM.measure().getId());   // control measureID
        tokens.add("" + comment);

        return tokens;
    }
    
    public void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord maxMeasure) throws SQLException {
        invenControlEfficiency = resultSet.getFloat("CEFF");
        invenRulePenetration = 100;
        invenRuleEffectiveness = resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF");
/*
        originalEmissions = resultSet.getFloat("ANN_EMIS");

//        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
//                / (100 * 100 * 100);
        double effectiveReduction = maxMeasure.effectiveReduction();

        reducedEmission = 0.0;
        finalEmissions = 0.0;

        //FIXME -- TEMPORARY - Ignore if inv item has an exisiting measure, just replace for now...
        reducedEmission = originalEmissions * effectiveReduction;
        finalEmissions = originalEmissions - reducedEmission;
*/
        if (1 == 2) throw new SQLException("");
        return;

//        if (invenEffectiveReduction == 0.0) {
//            reducedEmission = originalEmissions * effectiveReduction;
//            finalEmissions = originalEmissions - reducedEmission;
//            return;
//        }
//
//        if (invenEffectiveReduction < effectiveReduction) {
//            this.comment += "Existing control measure replaced; ";
//            originalEmissions = originalEmissions / invenEffectiveReduction;
//            reducedEmission = originalEmissions * effectiveReduction;
//            finalEmissions = originalEmissions - reducedEmission;
//            return;
//        }
//
//        this.comment += "Controlled with existing control measure; ";
//        originalEmissions = originalEmissions / invenControlEfficiency;
//        reducedEmission = originalEmissions * invenEffectiveReduction;
//        finalEmissions = originalEmissions - reducedEmission;
    }

}

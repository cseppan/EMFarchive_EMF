package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NonpointRecordGenerator implements RecordGenerator {

    private ControlStrategyResult strategyResult;

    private String comment;

    private double reducedEmission;

    private double invenControlEfficiency;

    private double invenRulePenetration;

    private double invenRuleEffectiveness;

//    private double originalEmissions;

//    private double finalEmissions;

    private DecimalFormat decFormat;

    public NonpointRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat) {
        this.strategyResult = result;
        this.comment = "";
        this.decFormat = decFormat;
    }

    public Record getRecord(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM, originalEmissions, displayOriginalEmissions, displayFinalEmissions));

        return record;
    }

    public List tokens(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        List<String> tokens = new ArrayList<String>();
        double effectiveReduction = maxCM.effectiveReduction();
        
        calculateEmissionReduction(resultSet,maxCM);
        reducedEmission = originalEmissions * effectiveReduction;
        
        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());
        tokens.add("" + 0);
        tokens.add("");

        tokens.add("false");
        tokens.add(maxCM.measure().getAbbreviation());
        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        tokens.add(resultSet.getString("fips"));

        tokens.add("");
        tokens.add("");
        tokens.add("");
        tokens.add("");

        tokens.add("" + decFormat.format(maxCM.adjustedCostPerTon() * reducedEmission));
        tokens.add("" + decFormat.format(maxCM.adjustedCostPerTon()));
        tokens.add("" + decFormat.format(maxCM.controlEfficiency()));
        tokens.add("" + maxCM.rulePenetration());
        tokens.add("" + maxCM.ruleEffectiveness());
        tokens.add("" + decFormat.format(effectiveReduction * 100));

        tokens.add("" + invenControlEfficiency);
        tokens.add("" + invenRulePenetration);
        tokens.add("" + invenRuleEffectiveness);
        tokens.add("" + (displayFinalEmissions ? decFormat.format(originalEmissions - reducedEmission) : 0));
        tokens.add("" + decFormat.format(reducedEmission));
        tokens.add("" + (displayOriginalEmissions ? decFormat.format(originalEmissions) : 0));

        tokens.add("" + resultSet.getInt("Record_Id"));
        tokens.add("" + strategyResult.getInputDatasetId());
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + maxCM.measure().getId());
        tokens.add("" + comment);

        return tokens;
    }

    public void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord maxMeasure) throws SQLException {
        invenControlEfficiency = resultSet.getDouble("CEFF");
        invenRulePenetration = resultSet.getDouble("RPEN");
        invenRuleEffectiveness = resultSet.getDouble("REFF");
/*
        invenControlEfficiency = resultSet.getDouble("CEFF");
        invenRulePenetration = resultSet.getDouble("RPEN");
        invenRuleEffectiveness = resultSet.getDouble("REFF");
        originalEmissions = resultSet.getDouble("ANN_EMIS");

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

    public double reducedEmission() {
        return reducedEmission;
    }

}

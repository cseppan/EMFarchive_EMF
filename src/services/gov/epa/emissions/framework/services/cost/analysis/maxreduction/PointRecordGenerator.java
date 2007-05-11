package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

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
    private double originalEmissions;
    private double finalEmissions;

    private DecimalFormat decFormat;

    public PointRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat) {
        this.strategyResult = result;
        this.decFormat = decFormat;
    }

    public Record getRecord(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM));

        return record;
    }

    public double reducedEmission() {
        return reducedEmission;
    }

    public List tokens(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException {
        List tokens = new ArrayList();
        
        calculateEmissionReduction(resultSet,maxCM);

        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());
        tokens.add("" + 0);
        tokens.add("");

        tokens.add("false");
        tokens.add(maxCM.measure().getAbbreviation());
        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        tokens.add(resultSet.getString("fips"));

        tokens.add(resultSet.getString("PLANTID"));
        tokens.add(resultSet.getString("POINTID"));
        tokens.add(resultSet.getString("STACKID"));
        tokens.add(resultSet.getString("SEGMENT"));
        
        tokens.add("" + decFormat.format(maxCM.adjustedCostPerTon() * reducedEmission));
        tokens.add("" + decFormat.format(maxCM.adjustedCostPerTon()));
        tokens.add("" + decFormat.format(maxCM.controlEfficiency()));
        tokens.add("" + 100);
        tokens.add("" + maxCM.ruleEffectiveness());
        tokens.add("" + decFormat.format(maxCM.effectiveReduction() * 100));

        tokens.add("" + invenControlEfficiency);
        tokens.add("" + invenRulePenetration);
        tokens.add("" + invenRuleEffectiveness);
        tokens.add("" + decFormat.format(finalEmissions));
        tokens.add("" + decFormat.format(reducedEmission));
        tokens.add("" + originalEmissions);

        tokens.add("" + resultSet.getInt("Record_Id"));
        tokens.add("" + strategyResult.getInputDatasetId());
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + maxCM.measure().getId());
        tokens.add("" + comment);

        return tokens;
    }
    
    public void calculateEmissionReduction(ResultSet resultSet, MaxEmsRedControlMeasure maxMeasure) throws SQLException {
        invenControlEfficiency = resultSet.getFloat("CEFF");
        invenRulePenetration = 100;
        invenRuleEffectiveness = resultSet.getFloat("CEFF") > 0 && resultSet.getFloat("REFF") == 0 ? 100 : resultSet.getFloat("REFF");
        originalEmissions = resultSet.getFloat("ANN_EMIS");

//        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
//                / (100 * 100 * 100);
        double effectiveReduction = maxMeasure.effectiveReduction();

        reducedEmission = 0.0;
        finalEmissions = 0.0;

        //FIXME -- TEMPORARY - Ignore if inv item has an exisiting measure, just replace for now...
        reducedEmission = originalEmissions * effectiveReduction;
        finalEmissions = originalEmissions - reducedEmission;
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

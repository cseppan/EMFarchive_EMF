package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NonpointRecordGenerator implements RecordGenerator {

    private ControlStrategyResult strategyResult;

    private String comment;

    private double reducedEmission;

    private double invenControlEfficiency;

    private double invenRulePenetration;

    private double invenRuleEffectiveness;

    private double originalEmissions;

    private double finalEmissions;

    public NonpointRecordGenerator(ControlStrategyResult result) {
        this.strategyResult = result;
        comment = "";
    }

    public Record getRecord(ResultSet resultSet, MaxControlEffControlMeasure maxCM) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM));

        return record;
    }

    private List tokens(ResultSet resultSet, MaxControlEffControlMeasure maxCM) throws SQLException, EmfException {
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

        tokens.add("" + maxCM.adjustedCostPerTon() * reducedEmission);
        tokens.add("" + maxCM.adjustedCostPerTon());
        tokens.add("" + maxCM.controlEfficiency());
        tokens.add("" + maxCM.rulePenetration());
        tokens.add("" + maxCM.ruleEffectiveness());
        tokens.add("" + maxCM.effectiveReduction() * 100);

        tokens.add("" + invenControlEfficiency);
        tokens.add("" + invenRulePenetration);
        tokens.add("" + invenRuleEffectiveness);
        tokens.add("" + finalEmissions);
        tokens.add("" + reducedEmission);
        tokens.add("" + originalEmissions);

        tokens.add("" + resultSet.getInt("Record_Id"));
        tokens.add("" + strategyResult.getInputDatasetId());
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + maxCM.measure().getId());
        tokens.add("" + comment);

        return tokens;
    }

    private void calculateEmissionReduction(ResultSet resultSet, MaxControlEffControlMeasure maxMeasure) throws SQLException {
        invenControlEfficiency = resultSet.getFloat("CEFF");
        invenRulePenetration = resultSet.getFloat("RPEN");
        invenRuleEffectiveness = resultSet.getFloat("REFF");
        originalEmissions = resultSet.getFloat("ANN_EMIS");

        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness
                / (100 * 100 * 100);
        double effectiveReduction = maxMeasure.effectiveReduction();

        reducedEmission = 0.0;
        finalEmissions = 0.0;
        if (invenEffectiveReduction == 0.0) {
            reducedEmission = originalEmissions * effectiveReduction;
            finalEmissions = originalEmissions - reducedEmission;
            return;
        }

        if (invenEffectiveReduction < effectiveReduction) {
            this.comment += "Existing control measure replaced; ";
            originalEmissions = originalEmissions / invenEffectiveReduction;
            reducedEmission = originalEmissions * effectiveReduction;
            finalEmissions = originalEmissions - reducedEmission;
            return;
        }

        this.comment += "Controlled with existing control measure; ";
        originalEmissions = originalEmissions / invenControlEfficiency;
        reducedEmission = originalEmissions * invenEffectiveReduction;
        finalEmissions = originalEmissions - reducedEmission;
    }

    public double reducedEmission() {
        return reducedEmission;
    }

}

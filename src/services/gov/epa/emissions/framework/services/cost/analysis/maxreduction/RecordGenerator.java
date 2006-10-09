package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordGenerator {

    private ControlStrategyResult strategyResult;

    private String comment;

    private double reducedEmission;

    public RecordGenerator(ControlStrategyResult result) {
        this.strategyResult = result;
        comment = "";
    }

    public Record getRecord(ResultSet resultSet, MaxControlEffContorlMeasure maxCM) throws SQLException, EmfException {
        Record record = new Record();
        record.add(tokens(resultSet, maxCM));

        return record;
    }

    private List tokens(ResultSet resultSet, MaxControlEffContorlMeasure maxCM) throws SQLException, EmfException {
        List tokens = new ArrayList();

        tokens.add(""); // record id
        tokens.add("" + strategyResult.getDetailedResultDataset().getId());
        tokens.add("" + 0);
        tokens.add("");

        tokens.add("false");
        tokens.add(maxCM.measure().getAbbreviation());

        tokens.add(resultSet.getString("poll"));
        tokens.add(resultSet.getString("scc"));
        tokens.add(resultSet.getString("fips"));

        tokens.add("" + maxCM.cost());
        tokens.add("" + maxCM.costPerTon());
        tokens.add("" + maxCM.controlEfficiency());
        tokens.add("" + maxCM.rulePenetration() ); 
        tokens.add("" + maxCM.ruleEffectiveness());
        tokens.add("" + maxCM.effectiveReduction()*100);

        emissions(tokens, resultSet, maxCM);

        tokens.add("" + resultSet.getInt("Record_Id"));
        tokens.add("" + strategyResult.getInputDatasetId());
        tokens.add("" + strategyResult.getControlStrategyId());
        tokens.add("" + maxCM.measure().getId());
        tokens.add("" + comment);

        return tokens;
    }

    private void emissions(List tokens, ResultSet resultSet, MaxControlEffContorlMeasure maxMeasure) throws SQLException {
        double invenControlEfficiency = resultSet.getFloat("CEFF");
        double invenRulePenetration = resultSet.getFloat("RPEN");
        double invenRuleEffectiveness = resultSet.getFloat("REFF");
        double originalEmissions = resultSet.getFloat("ANN_EMIS");

        double invenEffectiveReduction = invenControlEfficiency * invenRulePenetration * invenRuleEffectiveness/(100*100*100); 
        double effectiveReduction = maxMeasure.effectiveReduction();

        reducedEmission = 0.0;
        double finalEmissions = 0.0;

        tokens.add("" + invenControlEfficiency);
        tokens.add("" + invenRulePenetration);
        tokens.add("" + invenRuleEffectiveness);
        if (invenEffectiveReduction == 0.0) {
            reducedEmission = originalEmissions * effectiveReduction;
            finalEmissions = originalEmissions - reducedEmission;
            //tokens.add("" + maxMeasure.controlEfficiency());
            //tokens.add("" + maxMeasure.rulePenetration());
            //tokens.add("" + maxMeasure.ruleEffectiveness());
            tokens.add("" + finalEmissions);
            tokens.add("" + reducedEmission);
            tokens.add("" + originalEmissions);
            return;
        }

        if (invenEffectiveReduction < effectiveReduction) {
            this.comment += "Existing control measure replaced; ";
            originalEmissions = originalEmissions / invenEffectiveReduction;
            reducedEmission = originalEmissions * effectiveReduction;
            finalEmissions = originalEmissions - reducedEmission;
            //tokens.add("" + maxMeasure.controlEfficiency());
            //tokens.add("" + maxMeasure.rulePenetration());
            //tokens.add("" + maxMeasure.ruleEffectiveness());
            tokens.add("" + finalEmissions);
            tokens.add("" + reducedEmission);
            tokens.add("" + originalEmissions);
            return;
        }

        this.comment += "Controlled with existing control measure; ";
        originalEmissions = originalEmissions / invenControlEfficiency;
        reducedEmission = originalEmissions * invenEffectiveReduction;
        finalEmissions = originalEmissions - reducedEmission;
        //tokens.add("" + invenControlEfficiency);
        //tokens.add("" + invenRulePenetration);
        //tokens.add("" + invenRuleEffectiveness);
        tokens.add("" + finalEmissions);
        tokens.add("" + reducedEmission);
        tokens.add("" + originalEmissions);
    }

    public double reducedEmission() {
        return reducedEmission;
    }

}

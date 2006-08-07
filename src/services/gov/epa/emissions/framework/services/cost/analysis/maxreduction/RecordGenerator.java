package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordGenerator {

    private ControlStrategy controlStrategy;

    private int inputDatasetId;

    private String scc;

    private ResultSet resultSet;

    private ControlMeasure maxRedMeasure;

    private float inventoryEmissions;

    private float percentReduction;

    private float inventoryCE;

    private float inventoryRE;

    private float inventoryRP;

    private int resultDatasetId;

    private String controlEfficiency;

    private String comment;

    private float originalEmissions;

    public RecordGenerator(int inputDatasetId, int resultDatasetId, ResultSet resultSet, ControlMeasure measure,
            ControlStrategy controlStrategy) throws SQLException {
        this.inputDatasetId = inputDatasetId;
        this.resultDatasetId = resultDatasetId;
        this.controlStrategy = controlStrategy;
        this.resultSet = resultSet;

        this.scc = resultSet.getString("scc");
        this.inventoryEmissions = resultSet.getFloat("ANN_EMIS");
        this.inventoryCE = resultSet.getFloat("CEFF");
        this.inventoryRE = resultSet.getFloat("REFF");
        this.inventoryRP = resultSet.getFloat("RPEN");
        this.originalEmissions = inventoryEmissions;
        this.maxRedMeasure = measure;
    }

    public ControlMeasure getMaxEmsRedMeasure() {
        return this.maxRedMeasure;
    }

    public Record getRecord() throws SQLException {
        Record record = new Record();
        record.add(tokens());

        return record;
    }

    private List tokens() throws SQLException {
        int index = 0;
        List tokens = new ArrayList();

        tokens.add(index++, ""); // record id
        tokens.add(index++, "" + resultDatasetId);
        tokens.add(index++, "" + 0);
        tokens.add(index++, "");
        tokens.add(index++, "false");
        tokens.add(index++, maxRedMeasure.getAbbreviation());

        double reducedEmission = getReducedEmissions();
        String finalEmissions = "" + (originalEmissions - reducedEmission);

        tokens.add(index++, resultSet.getString("poll"));
        tokens.add(index++, scc);
        tokens.add(index++, resultSet.getString("fips"));
        tokens.add(index++, "" + getCost());
        tokens.add(index++, "" + getCostPerTon(maxRedMeasure));
        tokens.add(index++, controlEfficiency);
        tokens.add(index++, "");
        tokens.add(index++, "");
        tokens.add(index++, "" + percentReduction);
        tokens.add(index++, "" + inventoryCE);
        tokens.add(index++, "" + inventoryRP);
        tokens.add(index++, "" + inventoryRE);
        tokens.add(index++, finalEmissions);
        tokens.add(index++, "" + reducedEmission);
        tokens.add(index++, "" + inventoryEmissions);
        tokens.add(index++, "" + resultSet.getInt("Record_Id"));
        tokens.add(index++, "" + inputDatasetId);
        tokens.add(index++, "" + controlStrategy.getId());
        tokens.add(index++, "" + maxRedMeasure.getId());
        tokens.add(index++, comment);

        return tokens;
    }

    private float getEfficiency(ControlMeasure measure) {
        EfficiencyRecord[] records = measure.getEfficiencyRecords();
        String targetPollutant = controlStrategy.getTargetPollutant();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant().getName();
            if (pollutant.equalsIgnoreCase(targetPollutant))
                return records[i].getEfficiency();
        }

        return 0; // assume efficiency >= 0;
    }

    private float getCostPerTon(ControlMeasure measure) {
        EfficiencyRecord[] records = measure.getEfficiencyRecords();

        String targetPollutant = controlStrategy.getTargetPollutant();
        int costYear = controlStrategy.getCostYear();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant().getName();

            if (pollutant.equalsIgnoreCase(targetPollutant) && costYear == records[i].getCostYear())
                return records[i].getCostPerTon();
        }

        return 0; // assume cost per ton >= 0;
    }

    public double getCost() {
        return getReducedEmissions() * getCostPerTon(maxRedMeasure);
    }

    public float getReducedEmissions() {
        float newEfficiency = getEfficiency(maxRedMeasure);
        this.controlEfficiency = "" + newEfficiency;
        this.percentReduction = getPercentReduction();

        if (inventoryCE == 0) {
            return inventoryEmissions * percentReduction;
        }

        if (inventoryCE < newEfficiency) {
            this.comment += "Existing control measure replaced; ";
            originalEmissions = inventoryEmissions / inventoryCE;
            return originalEmissions * percentReduction;
        }

        this.comment += "Controlled with existing control measure; ";
        this.controlEfficiency = "" + inventoryCE;
        this.percentReduction = inventoryCE * inventoryRE * inventoryRP;

        return 0;
    }

    private float getPercentReduction() {
        EfficiencyRecord[] records = maxRedMeasure.getEfficiencyRecords();
        String targetPollutant = controlStrategy.getTargetPollutant();
        for (int i = 0; i < records.length; i++) {
            if (targetPollutant.equalsIgnoreCase(records[i].getPollutant().getName()))
                // FIXME: include cost year constraint && costYear==records[i].getCostYear()){
                // FIXME: default values for the rule effectiveness and penetration is 0;
                // return
                // records[i].getRuleEffectiveness()*records[i].getRulePenetration()*records[i].getPercentReduction();
                return records[i].getEfficiency();
        }
        return 0;
    }

}

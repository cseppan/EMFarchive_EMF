package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
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

    private double annEmissions;

    private int resultDatasetId;
    
    private String controlEfficiency;
    
    private String comment;

    public RecordGenerator(int inputDatasetId, int resultDatasetId, ResultSet resultSet, ControlMeasure measure, ControlStrategy controlStrategy)
            throws SQLException {
        this.inputDatasetId = inputDatasetId;
        this.resultDatasetId = resultDatasetId;
        this.controlStrategy = controlStrategy;
        this.resultSet = resultSet;

        this.scc = resultSet.getString("scc");
        this.annEmissions = resultSet.getDouble("ANN_EMIS");
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
        String controlledEmission = "" + (annEmissions - reducedEmission);

        tokens.add(index++, resultSet.getString("poll"));
        tokens.add(index++, scc);
        tokens.add(index++, resultSet.getString("fips"));
        tokens.add(index++, "" + getCost());
        tokens.add(index++, "" + getCostPerTon(maxRedMeasure));
        tokens.add(index++, controlEfficiency);
        tokens.add(index++, controlledEmission);
        tokens.add(index++, "" + reducedEmission);
        tokens.add(index++, "" + annEmissions);
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
            String pollutant = records[i].getPollutant();
            if (pollutant.equalsIgnoreCase(targetPollutant))
                return records[i].getEfficiency();
        }

        return 0; // assume efficiency >= 0;
    }

    private float getCostPerTon(ControlMeasure measure) {
        CostRecord[] records = measure.getCostRecords();
        String targetPollutant = controlStrategy.getTargetPollutant();
        int costYear = controlStrategy.getCostYear();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant();

            if (pollutant.equalsIgnoreCase(targetPollutant) && costYear == records[i].getCostYear())
                return records[i].getCostPerTon();
        }

        return 0; // assume cost per ton >= 0;
    }

    public double getCost() throws SQLException {
        return getReducedEmissions() * getCostPerTon(maxRedMeasure);
    }

    public double getReducedEmissions() throws SQLException {
        float newEfficiency = getEfficiency(maxRedMeasure);
        float oldEfficiency = resultSet.getFloat("CEFF");
        controlEfficiency = "" + newEfficiency;

        if (oldEfficiency == 0) {
            return annEmissions * newEfficiency;
        }

        if (oldEfficiency < newEfficiency) {
            comment += "Existing control replaced; ";
            annEmissions = annEmissions / oldEfficiency;
            return annEmissions * newEfficiency;
        }

        comment += "Not controlled; ";
        controlEfficiency = "";

        return 0;
    }

}

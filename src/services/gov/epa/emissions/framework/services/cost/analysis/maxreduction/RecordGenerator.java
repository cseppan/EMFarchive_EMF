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

    private int datasetId;

    private String scc;

    private ResultSet resultSet;

    private ControlMeasure maxRedMeasure;

    private double annEmissions;

    private int resultDatasetId;

    public RecordGenerator(int datasetId, int resultDatasetId, ResultSet resultSet, ControlMeasure measure, ControlStrategy controlStrategy)
            throws SQLException {
        this.datasetId = datasetId;
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
        tokens.add(index++, "" + resultSet.getInt("Record_Id"));
        tokens.add(index++, "" + datasetId);
        tokens.add(index++, "" + resultDatasetId);
        tokens.add(index++, "" + controlStrategy.getId());

        String controlMeasureId = "";
        String controlMeasureAbbr = "";
        String pollutant = resultSet.getString("poll");
        String fips = resultSet.getString("fips");
        String totalCost = "";
        String costPerTon = "";
        String controlEfficiency = "";
        String controlledEmission = "";
        String totalReduction = "";
        String originalEmissions = "";
        String disable = "false";
        String comment = "";

        double reducedEmission = getReducedEmissions(comment, controlEfficiency);
        controlMeasureId += maxRedMeasure.getId();
        controlMeasureAbbr += maxRedMeasure.getAbbreviation();
        totalCost += getCost();
        costPerTon += getCostPerTon(maxRedMeasure);
        totalReduction += reducedEmission;
        originalEmissions += annEmissions;
        controlledEmission += annEmissions - reducedEmission;

        tokens.add(index++, controlMeasureId);
        tokens.add(index++, controlMeasureAbbr);
        tokens.add(index++, pollutant);
        tokens.add(index++, scc);
        tokens.add(index++, fips);
        tokens.add(index++, totalCost);
        tokens.add(index++, costPerTon);
        tokens.add(index++, controlEfficiency);
        tokens.add(index++, controlledEmission);
        tokens.add(index++, totalReduction);
        tokens.add(index++, originalEmissions);
        tokens.add(index++, disable);
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

    public double getCost() {
        return annEmissions * getCostPerTon(maxRedMeasure);
    }

    public double getReducedEmissions(String comment, String controlEfficiency) throws SQLException {
        float newEfficiency = getEfficiency(maxRedMeasure);
        float oldEfficiency = resultSet.getFloat("CEFF");
        controlEfficiency = "" + newEfficiency;
        System.out.println("record generator: " + oldEfficiency);

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

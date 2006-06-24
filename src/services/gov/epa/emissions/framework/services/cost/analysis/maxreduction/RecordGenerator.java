package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.SCCControlMeasureMap;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordGenerator {

    private ControlStrategy controlStrategy;

    private int datasetId;

    private int sourceId;

    private String scc;

    private ControlMeasure maxRedMeasure;

    private double annEmissions;

    public RecordGenerator(int datasetId, ResultSet resultSet, SCCControlMeasureMap map, 
            ControlStrategy controlStrategy) throws SQLException {
        this.datasetId = datasetId;
        this.controlStrategy = controlStrategy;
        
        this.scc = resultSet.getString("scc");
        this.sourceId = resultSet.getInt("Record_Id");
        this.annEmissions = resultSet.getDouble("ANN_EMIS");
        this.maxRedMeasure = map.getMaxRedControlMeasure(scc);
    }
    
    public ControlMeasure getMaxEmsRedMeasure() {
        return this.maxRedMeasure;
    }

    public Record getRecord() {
        Record record = new Record();
        record.add(Arrays.asList(tokens()));

        return record;
    }

    private String[] tokens() {
        List tokens = new ArrayList();
        tokens.add(0, ""); // record id
        tokens.add(1, "" + sourceId);
        tokens.add(2, "" + datasetId);

        if (maxRedMeasure == null) {
            tokens.add(3, "");
            tokens.add(4, "");
            tokens.add(5, controlStrategy.getName());
            tokens.add(6, scc);
            tokens.add(7, "");
            tokens.add(8, "");
            tokens.add(9, "");
        } else {
            tokens.add(3, "" + maxRedMeasure.getId());
            tokens.add(4, maxRedMeasure.getAbbreviation());
            tokens.add(5, controlStrategy.getName());
            tokens.add(6, scc);
            tokens.add(7, "" + getCost());
            tokens.add(8, "" + getCostPerTon(maxRedMeasure));
            tokens.add(9, "" + getReducedEmissions());
        }
        
        return (String[]) tokens.toArray(new String[0]);
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

    public double getReducedEmissions() {
        return annEmissions * getEfficiency(maxRedMeasure);
    }

}

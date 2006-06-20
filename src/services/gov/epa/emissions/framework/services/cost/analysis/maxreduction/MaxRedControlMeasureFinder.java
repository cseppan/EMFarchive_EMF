package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaxRedControlMeasureFinder {

    private List measures;

    private ControlStrategy controlStrategy;

    private int datasetId;

    private int sourceId;

    private ControlMeasure selectedMeasure;

    private String scc;

    private ControlMeasure previous;

    private ControlMeasure next;
    
    private float annEmissions;

    public MaxRedControlMeasureFinder(int datasetId, int sourceId, List measures, String scc,
            float annEmissions, ControlStrategy controlStrategy) {
        this.measures = measures;
        this.datasetId = datasetId;
        this.sourceId = sourceId;
        this.controlStrategy = controlStrategy;
        this.scc = scc;
        this.annEmissions = annEmissions;
    }
    
    public ControlMeasure getMaxEmsRedMeasure() {
        return this.selectedMeasure;
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
        tokens.add(5, controlStrategy.getName());
        tokens.add(6, scc);

        this.selectedMeasure = calculateMeasure(calculateMaxMeasure(measures));

        if (selectedMeasure == null) {
            tokens.add(3, "");
            tokens.add(4, "");
            tokens.add(7, "");
            tokens.add(8, "");
            tokens.add(9, "");
        } else {
            tokens.add(3, "" + selectedMeasure.getId());
            tokens.add(4, selectedMeasure.getAbbreviation());
            tokens.add(7, "" + getCost(selectedMeasure));
            tokens.add(8, "" + getCostPerTon(selectedMeasure));
            tokens.add(9, "" + getReducedEmissions(selectedMeasure));
        }
        
        setControlStrategy(selectedMeasure);

        return (String[]) tokens.toArray(new String[0]);
    }

    private ControlMeasure calculateMeasure(ControlMeasure init) {
        previous = init;
        if (previous == null)
            return previous;
        measures.remove(previous);

        next = calculateMaxMeasure(measures);
        if (next == null)
            return previous;
        measures.remove(next);

        if (getEfficiency(previous) == getEfficiency(next)) {
            previous = getMeasureWithSmallerCost(previous, next);
            return calculateMeasure(previous);
        }

        return previous;
    }

    private ControlMeasure getMeasureWithSmallerCost(ControlMeasure previous, ControlMeasure next) {
        return getCostPerTon(previous) <= getCostPerTon(next) ? previous : next;
    }

    private ControlMeasure calculateMaxMeasure(List measureList) {
        if (measureList.size() == 0)
            return null;

        ControlMeasure temp = (ControlMeasure) measureList.get(0);

        for (int i = 1; i < measureList.size(); i++) {
            if (getEfficiency(temp) < getEfficiency((ControlMeasure) measureList.get(i)))
                temp = (ControlMeasure) measureList.get(i);
        }

        return temp;
    }

    private float getEfficiency(ControlMeasure measure) {
        EfficiencyRecord[] records = measure.getEfficiencyRecords();
        String targetPollutant = controlStrategy.getMajorPollutant();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant();
            if (pollutant.equalsIgnoreCase(targetPollutant))
                return records[i].getEfficiency();
        }

        return 0; // assume efficiency >= 0;
    }

    private float getCostPerTon(ControlMeasure measure) {
        CostRecord[] records = measure.getCostRecords();
        String targetPollutant = controlStrategy.getMajorPollutant();
        int costYear = controlStrategy.getCostYear();

        for (int i = 0; i < records.length; i++) {
            String pollutant = records[i].getPollutant();
            if (pollutant.equalsIgnoreCase(targetPollutant) && costYear == records[i].getCostYear())
                return records[i].getCostPerTon();
        }

        return 0; // assume cost per ton >= 0;
    }

    private float getCost(ControlMeasure measure) {
        return annEmissions * getCostPerTon(measure);
    }

    private float getReducedEmissions(ControlMeasure measure) {
        return annEmissions * getEfficiency(measure);
    }
    
    private void setControlStrategy(ControlMeasure measure) {
        controlStrategy.setTotalCost(getCost(measure));
        controlStrategy.setReduction(getReducedEmissions(measure));
    }
    
}

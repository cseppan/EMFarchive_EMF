package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.text.DecimalFormat;

public class RecordGeneratorFactory {

    private DatasetType datasetType;
    
    private ControlStrategyResult result;
    
    private DecimalFormat decFormat;
    
    private double discountRate; 
    private boolean useCostEquation;
    
    public RecordGeneratorFactory(DatasetType datasetType, ControlStrategyResult result, DecimalFormat decFormat, double discountRate, boolean useCostEquation) {
        this.datasetType = datasetType;
        this.result = result;
        this.decFormat = decFormat;
        this.discountRate= discountRate; 
        this.useCostEquation=useCostEquation;
    }

    public RecordGenerator getRecordGenerator() {

        if (datasetType.getName().equalsIgnoreCase("ORL Nonpoint Inventory (ARINV)"))
            return new NonpointRecordGenerator(result, decFormat);
        else if (datasetType.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)"))
            return new PointRecordGenerator(result, decFormat, discountRate, useCostEquation);
        else if (datasetType.getName().equalsIgnoreCase("ORL Onroad Inventory (MBINV)"))
            return new OnroadRecordGenerator(result, decFormat);
        else if (datasetType.getName().equalsIgnoreCase("ORL Nonroad Inventory (ARINV)"))
            return new NonroadRecordGenerator(result, decFormat);

        return null;
    }

}

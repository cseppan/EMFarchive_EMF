package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.text.DecimalFormat;

public class RecordGeneratorFactory {

    private DatasetType datasetType;
    
    private ControlStrategyResult result;
    
    private DecimalFormat decFormat;
    
    private CostEquationFactory costEquationsFactory;

    public RecordGeneratorFactory(DatasetType datasetType, ControlStrategyResult result, DecimalFormat decFormat, double discountRate, boolean useCostEquation) {
        this.datasetType = datasetType;
        this.result = result;
        this.decFormat = decFormat;
        this.costEquationsFactory = new CostEquationFactory(useCostEquation, discountRate);
    }

    public RecordGenerator getRecordGenerator() {
        if (datasetType.getName().equalsIgnoreCase("ORL Nonpoint Inventory (ARINV)"))
            return new NonpointRecordGenerator(result, decFormat, costEquationsFactory);
        else if (datasetType.getName().equalsIgnoreCase("ORL Point Inventory (PTINV)") ||
                datasetType.getName().equalsIgnoreCase("ORL CoST Point Inventory (PTINV)"))
            return new PointRecordGenerator(datasetType, result, decFormat, costEquationsFactory);
        else if (datasetType.getName().equalsIgnoreCase("ORL Onroad Inventory (MBINV)"))
            return new OnroadRecordGenerator(result, decFormat, costEquationsFactory);
        else if (datasetType.getName().equalsIgnoreCase("ORL Nonroad Inventory (ARINV)"))
            return new NonroadRecordGenerator(result, decFormat, costEquationsFactory);

        return null;
    }

}

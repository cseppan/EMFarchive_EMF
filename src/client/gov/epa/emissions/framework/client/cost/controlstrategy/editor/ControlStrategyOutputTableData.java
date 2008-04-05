package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlStrategyOutputTableData extends AbstractTableData {

    private List rows;

    public ControlStrategyOutputTableData(ControlStrategyResult[] controlStrategyResults) {
        this.rows = createRows(controlStrategyResults);
    }

    private List createRows(ControlStrategyResult[] controlStrategyResults) {
        List rows = new ArrayList();
        for (int i = 0; i < controlStrategyResults.length; i++) {
            addRow(rows, controlStrategyResults[i]);
        }
        return rows;
    }

    private void addRow(List rows, ControlStrategyResult controlStrategyResult) {
        Object[] values = values(controlStrategyResult);
        Row row = new ViewableRow(controlStrategyResult, values);
        rows.add(row);
    }

    private Object[] values(ControlStrategyResult result) {
        EmfDataset outputDataset = (EmfDataset) result.getDetailedResultDataset();
        EmfDataset controlledInvDataset = (EmfDataset) result.getControlledInventoryDataset();
        EmfDataset inputDataset = result.getInputDataset();
//        boolean isStrategySummaryResult = result.getStrategyResultType().getName().equals(StrategyResultType.strategySummaryResult);
//        boolean isDetailedStrategyResult = result.getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult);
//        boolean isLeastCostControlMeasureWorksheetResult = result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheetResult);
        Object[] values = { 
                result.getStrategyResultType().getName(),
                inputDataset != null ? inputDataset.getName() : "", //isDetailedStrategyResult ? result.getInputDataset().getName() : isLeastCostControlMeasureWorksheetResult ? StrategyResultType.leastCostControlMeasureWorksheetResult : isStrategySummaryResult ? StrategyResultType.strategySummaryResult : "", 
                result.getRecordCount() == null ? 0 : result.getRecordCount(), 
                inputDataset != null && result.getInputDatasetVersion() != null ? result.getInputDatasetVersion() : new Integer(0), 
                outputDataset != null ? outputDataset.getName() : "", 
                controlledInvDataset == null ? "" : controlledInvDataset.getName(), 
                result.getRunStatus(), 
                result.getTotalCost() != null ? result.getTotalCost() : Double.NaN, 
                result.getTotalReduction() != null ? result.getTotalReduction() : Double.NaN, 
                format(result.getStartTime()),
                format(result.getCompletionTime())
                };
        return values;
    }

    public String[] columns() {
        return new String[] { 
                "Result Type", 
                "Input Inventory", 
                "Record Count", 
                "Input Version", 
                "Result", 
                "Controlled Inventory", 
                "Status", "Total Cost", 
                "Total Reduction", "Start Time", 
                "Completion Time" };
    }

    public Class getColumnClass(int col) {
        if (col == 3 || col == 2)
            return Integer.class;

        if (col == 7 || col == 8)
            return Double.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
}
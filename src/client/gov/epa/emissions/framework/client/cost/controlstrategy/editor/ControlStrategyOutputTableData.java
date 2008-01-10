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
        boolean stratSummary = result.getStrategyResultType().getName().equals("Strategy Summary");
        Object[] values = { !stratSummary ? result.getInputDataset().getName() : "Strategy Summary", result.getRecordCount() == null ? 0 : result.getRecordCount(), inputDataset != null ? result.getInputDatasetVersion() : new Integer(0), 
                outputDataset != null ? outputDataset.getName() : "", controlledInvDataset == null ? "" : controlledInvDataset.getName(), 
                result.getRunStatus(), !stratSummary ? result.getTotalCost() : Double.NaN, 
                !stratSummary ? result.getTotalReduction() : Double.NaN, format(result.getStartTime()),
                format(result.getCompletionTime()) };
        return values;
    }

    public String[] columns() {
        return new String[] { "Input Inventory", "Record Count", "Input Version", 
                "Detailed Result", "Controlled Inventory", 
                "Status", "Total Cost", 
                "Total Reduction", "Start Time", 
                "Completion Time" };
    }

    public Class getColumnClass(int col) {
        if (col == 2 || col == 1)
            return Integer.class;

        if (col == 6 || col == 7)
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
package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlStrategyOutputTableData extends AbstractTableData {

    private List rows;

    private ControlStrategyInputDataset[] controlStrategyInputDatasets;

    public ControlStrategyOutputTableData(ControlStrategyInputDataset[] controlStrategyInputDatasets, ControlStrategyResult[] controlStrategyResults) {
        this.controlStrategyInputDatasets = controlStrategyInputDatasets;
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
        ControlStrategyInputDataset controlStrategyInputDataset = getControlStrategyInputDataset(result.getInputDatasetId());
        Object[] values = { result.getInputDataset().getName(), controlStrategyInputDataset != null ? controlStrategyInputDataset.getVersion() : result.getInputDataset().getDefaultVersion(), 
                outputDataset.getName(), controlledInvDataset == null ? "" : controlledInvDataset.getName(), 
                result.getRunStatus(), result.getTotalCost(), 
                result.getTotalReduction(), format(result.getStartTime()),
                format(result.getCompletionTime()), result.getRecordCount() == null ? 0 : result.getRecordCount() };
        return values;
    }

    private ControlStrategyInputDataset getControlStrategyInputDataset(int datasetId) {
        ControlStrategyInputDataset inputDataset = null;
        for (int j = 0; j < controlStrategyInputDatasets.length; j++) {
            if (controlStrategyInputDatasets[j].getInputDataset().getId() == datasetId) {
                inputDataset = controlStrategyInputDatasets[j];
                break;
            }
        }
        return inputDataset;
    }

    public String[] columns() {
        return new String[] { "Input Inventory", "Input Version", 
                "Detailed Result", "Controlled Inventory", 
                "Status", "Total Cost", 
                "Total Reduction", "Start Time", 
                "Completion Time", "Record Count" };
    }

    public Class getColumnClass(int col) {
        if (col == 1 || col == 9)
            return Integer.class;

        if (col == 5 || col == 6)
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
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
        Object[] values = { inputDatasetName(result.getInputDatasetId()), outputDataset.getName(),
                controlledInvDataset == null ? "" : controlledInvDataset.getName(), result.getRunStatus(), 
                result.getTotalCost(), result.getTotalReduction() };
        return values;
    }

    private String inputDatasetName(int inputDatasetId) {
        for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
            if (inputDatasetId == controlStrategyInputDatasets[i].getInputDataset().getId())
                return controlStrategyInputDatasets[i].getInputDataset().getName();
        }
        return "";
    }

    public String[] columns() {
        return new String[] { "Input Inventory", "Detailed Result", "Controlled Inventory", "Status", "Total Cost", "Total Reduction" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
}
package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlStrategyOutputTableData extends AbstractTableData {

    private List rows;

    private EmfDataset[] inputDatasets;

    public ControlStrategyOutputTableData(EmfDataset[] inputDatasets, ControlStrategyResult strategyResult) {
        this.inputDatasets = inputDatasets;
        this.rows = createRows(strategyResult);
    }

    private List createRows(ControlStrategyResult result) {
        List rows = new ArrayList();
        addDetailDatasetRow(rows, result);
        addControlInvenRow(rows, result);

        return rows;
    }

    private void addControlInvenRow(List rows, ControlStrategyResult result) {
        EmfDataset dataset = (EmfDataset) result.getControlledInventoryDataset();
        if (dataset == null)
            return;
        Object[] values = controlInvenValues(result, dataset);
        Row row = new ViewableRow(dataset, values);
        rows.add(row);

    }

    private Object[] controlInvenValues(ControlStrategyResult result, EmfDataset dataset) {
        Object[] values = { inputDatasetName(result.getInputDatasetId()), dataset.getName(), "Controlled Inventory" };
        return values;
    }

    private void addDetailDatasetRow(List rows, ControlStrategyResult result) {
        Dataset detailedResultDataset = result.getDetailedResultDataset();
        if (detailedResultDataset == null)
            return;
        Object[] values = detailValues(result);
        Row row = new ViewableRow(detailedResultDataset, values);
        rows.add(row);
    }

    private Object[] detailValues(ControlStrategyResult result) {
        EmfDataset outputDataset = (EmfDataset) result.getDetailedResultDataset();
        Object[] values = { inputDatasetName(result.getInputDatasetId()), outputDataset.getName(),
                result.getStrategyResultType().getName() };
        return values;
    }

    private String inputDatasetName(int inputDatasetId) {
        for (int i = 0; i < inputDatasets.length; i++) {
            if (inputDatasetId == inputDatasets[i].getId())
                return inputDatasets[i].getName();
        }
        return "";
    }

    public String[] columns() {
        return new String[] { "Input Dataset", "Output Dataset", "Product" };
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

package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlStrategiesTableData extends AbstractTableData {

    private List rows;

    public ControlStrategiesTableData(ControlStrategy[] controlStrategies) {
        this.rows = createRows(controlStrategies);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified", "Region", "Project", "Analysis Type", "Dataset Type",
                "Discount Rate", "Cost Year" };

    }

    public Class getColumnClass(int col) {
        if (col == 1)
            return Date.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ControlStrategy[] controlStrategies) {
        List rows = new ArrayList();
        for (int i = 0; i < controlStrategies.length; i++) {
            ControlStrategy element = controlStrategies[i];
            Object[] values = { element.getName(), format(element.getLastModifiedDate()), region(element),
                    project(element), analysisType(element), datasetType(element), discountRate(element),
                    costYear(element) };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String project(ControlStrategy element) {
        Project project = element.getProject();
        return project != null ? project.getName() : "";
    }

    private String region(ControlStrategy element) {
        return element.getRegion() != null ? element.getRegion().getName() : "";
    }

    private String analysisType(ControlStrategy element) {
        return "" + element.getAnalysisType();
    }

    private String datasetType(ControlStrategy element) {
        DatasetType datasetType = element.getDatasetType();
        return (datasetType != null) ? datasetType.getName() : "";
    }

    private String discountRate(ControlStrategy element) {
        return "" + element.getDiscountRate();
    }

    private String costYear(ControlStrategy element) {
        return "" + element.getCostYear();
    }

}

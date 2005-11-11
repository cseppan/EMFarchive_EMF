package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfDataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmfDatasetTableData extends AbstractTableData {

    private List rows;

    public EmfDatasetTableData(EmfDataset[] datasets) {
        this.rows = createRows(datasets);
    }

    public String[] columns() {
        return new String[] { "Name", "Type", "Status", "Creator", "Region", "Start Date", "End Date" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(EmfDataset[] datasets) {
        List rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            EmfDataset dataset = datasets[i];
            Object[] values = { dataset.getName(), dataset.getDatasetTypeName(), dataset.getStatus(),
                    dataset.getCreator(), dataset.getRegion(), dataset.getStartDateTime(), dataset.getStopDateTime() };

            Row row = new ViewableRow(dataset, values);
            rows.add(row);
        }

        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col == 5 || col == 6)
            return Date.class;

        return String.class;
    }

}

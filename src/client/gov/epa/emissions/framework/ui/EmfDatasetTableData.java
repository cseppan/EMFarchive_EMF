package gov.epa.emissions.framework.ui;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.client.exim.Row;
import gov.epa.emissions.framework.services.EmfDataset;

public class EmfDatasetTableData extends AbstractEmfTableData {

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
            Object[] values = { dataset.getName(), dataset.getDatasetType(), dataset.getStatus(), dataset.getCreator(),
                    dataset.getRegion(), dataset.getStartDateTime(), dataset.getStopDateTime() };

            Row row = new Row(dataset, values);
            rows.add(row);
        }

        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

}

package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfDataset;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmfDatasetTableData extends AbstractTableData {

    private List rows;

    private DateFormat dateFormat;

    public EmfDatasetTableData(EmfDataset[] datasets) {
        dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        this.rows = createRows(datasets);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified Date", "Type", "Status", "Creator", "Region", "Start Date" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(EmfDataset[] datasets) {
        List rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            EmfDataset dataset = datasets[i];
            Object[] values = { dataset.getName(), format(dataset.getModifiedDateTime()), dataset.getDatasetTypeName(),
                    dataset.getStatus(), dataset.getCreator(), dataset.getRegion(),
                    formatStartDate(dataset.getStartDateTime()) };

            Row row = new ViewableRow(dataset, values);
            rows.add(row);
        }

        return rows;
    }

    private String formatStartDate(Date date) {
        return (date == null) ? "N/A" : dateFormat.format(date);
    }

    private String format(Date date) {
        return dateFormat.format(date);
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col == 1 || col == 6)
            return Date.class;
        if (col < 0 || col > 6) {
            throw new IllegalArgumentException("Allowed values are between 0 and 6, but the value is " + col);
        }
        return String.class;
    }

}

package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class DatasetsBrowserTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    public DatasetsBrowserTableModel(EmfDataset[] datasets) {
        this.header = new TableHeader(new String[] { "Name", "Type", "Status", "Creator", "Region", "Start Date",
                "End Date" });

        populate(datasets);
    }

    public int getRowCount() {
        return rows.size();
    }

    public String getColumnName(int index) {
        return header.columnName(index);
    }

    public int getColumnCount() {
        return header.columnsSize();
    }

    public Object getValueAt(int row, int column) {
        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void refresh() {
        // TODO: what to do ?
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public EmfDataset getDataset(int index) {
        Row row = (Row) rows.get(index);
        return (EmfDataset) row.record();
    }

    public void populate(EmfDataset[] datasets) {
        this.rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            EmfDataset dataset = datasets[i];
            Object[] values = { dataset.getName(), dataset.getDatasetType(), dataset.getStatus(), dataset.getCreator(),
                    dataset.getRegion(), dataset.getStartDateTime(), dataset.getStopDateTime() };

            Row row = new Row(dataset, values);
            rows.add(row);
        }
    }

}

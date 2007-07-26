package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategyInputDatasetTableData extends AbstractTableData {

    private List rows;

    public ControlStrategyInputDatasetTableData(EmfDataset[] inputDatasets) {
        rows = createRows(inputDatasets);
    }

    private List createRows(EmfDataset[] inputDatasets) {
        List rows = new ArrayList();
        for (int i = 0; i < inputDatasets.length; i++) {
            Row row = row(inputDatasets[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(EmfDataset inputDataset) {
        Object[] values = { inputDataset.getDatasetType().getName(), inputDataset.getName(), inputDataset.getDefaultVersion()};
        return new ViewableRow(inputDataset, values);
    }

    public String[] columns() {
        return new String[] { "Type", "Dataset", "Version" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(EmfDataset[] inputDataset) {
        for (int i = 0; i < inputDataset.length; i++) {
            Row row = row(inputDataset[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public EmfDataset[] sources() {
        List sources = sourcesList();
        return (EmfDataset[]) sources.toArray(new EmfDataset[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(EmfDataset inputDataset) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            EmfDataset source = (EmfDataset) row.source();
            if (source == inputDataset) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(EmfDataset[] inputDatasets) {
        for (int i = 0; i < inputDatasets.length; i++)
            remove(inputDatasets[i]);

        refresh();
    }

}

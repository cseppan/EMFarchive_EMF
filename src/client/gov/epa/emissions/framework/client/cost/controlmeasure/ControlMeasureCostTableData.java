package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureCostTableData {

    private List rows;

    public ControlMeasureCostTableData(CostRecord[] records) {
        this.rows = createRows(records);
    }

    public void add(CostRecord record) {
        rows.add(row(record));
    }

    public void removeSelected() {
        remove(getSelected());
    }

    private void remove(CostRecord record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            CostRecord source = (CostRecord) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    private void remove(CostRecord[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }

    public String[] columns() {
        return new String[] { "Select", "Record ID", "Pollutant", "Cost Year", "Discount Rate", "a", "b" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return (col == 1) ? false:true;
    }

    private List createRows(CostRecord[] records) {
        List rows = new ArrayList();
        for (int i = 0; i < records.length; i++)
            rows.add(row(records[i]));

        return rows;
    }

    private EditableRow row(CostRecord record) {
        RowSource source = new CostRecordRowSource(record);
        return new EditableRow(source);
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        if (col == 1 || col == 3)
            return Integer.class;

        if (col == 2)
            return String.class;
        
        return Float.class;

    }

    public CostRecord[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            CostRecordRowSource rowSource = (CostRecordRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (CostRecord[]) selected.toArray(new CostRecord[0]);
    }

    public CostRecord[] sources() {
        List sources = sourcesList();
        return (CostRecord[]) sources.toArray(new CostRecord[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            CostRecordRowSource rowSource = (CostRecordRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

    public void sortByOrder() {
        CostRecords records = new CostRecords(sources());
        this.rows = createRows(records.sortByOrder());
    }

}

package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.CostRecord;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureCostTableData extends AbstractEditableTableData {

    private List rows;

    public ControlMeasureCostTableData(CostRecord[] records) {
        this.rows = createRows(records);
    }

    public void add(CostRecord record) {
        rows.add(row(record));
    }

    public String[] columns() {
        return new String[] { "Pollutant", "Cost Year", "Discount Rate", "Slope", "Constant" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
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
        
        if (col == 1)
            return String.class;
        
        if (col == 2)
            return Integer.class;

        return Float.class;
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

    public void remove(CostRecord[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }

    public void sortByOrder() {
        CostRecords records = new CostRecords(sources());
        this.rows = createRows(records.sortByOrder());
    }

}

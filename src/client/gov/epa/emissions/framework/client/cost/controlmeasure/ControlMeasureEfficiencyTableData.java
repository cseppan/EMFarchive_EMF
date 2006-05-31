package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureEfficiencyTableData extends AbstractTableData {

    private List rows;

    public ControlMeasureEfficiencyTableData(EfficiencyRecord[] records) {
        this.rows = createRows(records);
    }

    public void add(EfficiencyRecord record) {
        rows.add(row(record));
    }

    public String[] columns() {
        return new String[] { "Pollutant", "Percent Reduction" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private Row row(EfficiencyRecord record) {
        Object[] values = { record.getPollutant(), new Float(record.getEfficiency()) };

        return new ViewableRow(record, values);
    }

    private List createRows(EfficiencyRecord[] records) {
        List rows = new ArrayList();

        for (int i = 0; i < records.length; i++) {
            EfficiencyRecord record = records[i];
            rows.add(row(record));
        }

        return rows;
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        if (col == 1)
            return String.class;

        return Float.class;
    }

    public EfficiencyRecord[] sources() {
        List sources = sourcesList();
        return (EfficiencyRecord[]) sources.toArray(new EfficiencyRecord[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }
        
        return sources;
    }

    private void remove(EfficiencyRecord record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            EfficiencyRecord source = (EfficiencyRecord) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(EfficiencyRecord[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

}

package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    public ControlMeasureTableData(ControlMeasure[] cms) {
        rows = createRows(cms);
    }

    private List createRows(ControlMeasure[] cms) {
        List rows = new ArrayList();
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlMeasure cm) {
        String[] values = { cm.getAbbreviation(), cm.getName() };
        return new ViewableRow(cm, values);
    }

    public String[] columns() {
        return new String[] { "Abbrev", "Name" };
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

    public void add(ControlMeasure[] cms) {
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlMeasure[] sources() {
        List sources = sourcesList();
        return (ControlMeasure[]) sources.toArray(new ControlMeasure[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlMeasure record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlMeasure source = (ControlMeasure) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlMeasure[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}

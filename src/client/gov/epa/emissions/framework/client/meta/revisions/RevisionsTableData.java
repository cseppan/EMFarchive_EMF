package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.Revision;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.SelectableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RevisionsTableData extends AbstractTableData {

    private List rows;

    private Revision[] values;

    public RevisionsTableData(Revision[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Select", "What", "Why", "References", "Version", "Creator", "Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;
        if (col == 6)
            return Date.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows(Revision[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(Revision revision) {
        return new SelectableRow(new RevisionsRowSource(revision));
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public Revision[] selected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            SelectableRow row = (SelectableRow) iter.next();
            if (row.isSelected())
                selected.add(row.source());
        }

        return (Revision[]) selected.toArray(new Revision[0]);
    }

    public Revision[] getValues() {
        return values;
    }

}

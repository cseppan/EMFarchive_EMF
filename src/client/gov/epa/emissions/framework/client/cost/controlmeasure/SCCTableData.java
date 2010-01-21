package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SCCTableData extends AbstractTableData {

    private List rows;

    public SCCTableData(Scc[] sccs) {
        rows = createRows(sccs);
    }

    private List<Row> createRows(Scc[] sccs) {

        List<Row> rows = new ArrayList();
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            rows.add(row);
        }

        /*
         * Sort by scc
         */
        Collections.sort(rows, new Comparator<Row>() {

            public int compare(Row r1, Row r2) {

                String sccCode1 = (String) r1.getValueAt(0);
                String sccCode2 = (String) r2.getValueAt(0);

                return sccCode1.compareTo(sccCode2);
            }
        });

        return rows;
    }

    private Row row(Scc scc) {
        String[] values = { scc.getCode(), scc.getDescription() };
        return new ViewableRow(scc, values);
    }

    public String[] columns() {
        return new String[] { "SCC", "Description" };
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

    public void add(Scc[] sccs) {
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public Scc[] sources() {
        List sources = sourcesList();
        return (Scc[]) sources.toArray(new Scc[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(Scc record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            Scc source = (Scc) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(Scc[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}

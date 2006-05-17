package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class SCCTableData extends AbstractTableData {

    private List rows;

    public SCCTableData(String[] sccs) {
        rows = createRows(sccs);
    }

    private List createRows(String[] sccs) {
        List rows = new ArrayList();
        for (int i = 0; i < sccs.length; i++) {
            Row row = new ViewableRow(sccs[i], new String[] { sccs[i] });
            rows.add(row);
        }
        return rows;
    }

    public String[] columns() {
        return new String[] { "Name" };
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

}

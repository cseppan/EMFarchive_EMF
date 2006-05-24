package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class SCCTableData extends AbstractTableData {

    private List rows;

    public SCCTableData(Scc[] sccs) {
        rows = createRows(sccs);
    }

    private List createRows(Scc[] sccs) {
        List rows = new ArrayList();
        for (int i = 0; i < sccs.length; i++) {
            Row row = row(sccs[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(Scc scc) {
        String [] values = {scc.code(),scc.description()};
        return  new ViewableRow(scc,values);
    }

    public String[] columns() {
        return new String[] {"SCC","Description" };
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

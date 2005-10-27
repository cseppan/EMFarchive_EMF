package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.EditableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectorCriteriaTableData extends AbstractEmfTableData {
    private List rows;

    public SectorCriteriaTableData(SectorCriteria[] criteria) {
        this.rows = createRows(criteria);
    }

    public String[] columns() {
        return new String[] { "Select", "Type", "Criterion" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(SectorCriteria[] criteria) {
        List rows = new ArrayList();
        for (int i = 0; i < criteria.length; i++)
            rows.add(row(criteria[i]));

        return rows;
    }

    public void remove(SectorCriteria criterion) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            SectorCriteria element = (SectorCriteria) row.record();
            if (element.equals(criterion)) {
                rows.remove(row);
                return;
            }
        }
    }

    public void add(SectorCriteria criterion) {
        rows.add(row(criterion));
    }

    private EditableRow row(SectorCriteria criterion) {
        Object[] values = new Object[] { Boolean.FALSE, criterion.getType(), criterion.getCriteria() };
        return new EditableRow(criterion, values);
    }

    public void setValueAt(Object value, int row, int col) {
        // TODO Auto-generated method stub

    }

    public SectorCriteria[] getSelected() {
        return null;
    }

}

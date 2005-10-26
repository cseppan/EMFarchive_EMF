package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.SectorCriteria;
import gov.epa.emissions.framework.ui.AbstractEmfTableData;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class SectorCriteriaTableData extends AbstractEmfTableData {
    private List rows;

    public SectorCriteriaTableData(SectorCriteria[] criteria) {
        this.rows = createRows(criteria);
    }

    public String[] columns() {
        return new String[] { "Type", "Criterion" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(SectorCriteria[] criteria) {
        List rows = new ArrayList();

        for (int i = 0; i < criteria.length; i++) {
            SectorCriteria element = criteria[i];
            Object[] values = { element.getType(), element.getCriteria() };

            Row row = new Row(element, values);
            rows.add(row);
        }

        return rows;
    }
}

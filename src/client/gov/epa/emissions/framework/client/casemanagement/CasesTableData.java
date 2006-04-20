package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CasesTableData extends AbstractTableData {
    private List rows;

    public CasesTableData(Case[] types) {
        this.rows = createRows(types);
    }

    public String[] columns() {
        return new String[] { "Name", "Category", "Region", "Emissions Year", "Meteorlogical Year", "Last Modified",
                "Run Status" };
    }

    public Class getColumnClass(int col) {
        if (col == 3 || col == 4)
            return Integer.class;

        if (col == 5)
            return Date.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(Case[] types) {
        List rows = new ArrayList();

        for (int i = 0; i < types.length; i++) {
            Case element = types[i];
            Object[] values = { element.getName(), element.getCaseCategory().getName(), element.getRegion().getName(),
                    toInteger(element.getEmissionsYear().getName()),
                    toInteger(element.getMeteorlogicalYear().getName()), format(element.getLastModifiedDate()),
                    element.getRunStatus() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private Integer toInteger(String val) {
        return Integer.valueOf(val);
    }

}

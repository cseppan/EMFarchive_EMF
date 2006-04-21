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
            Object[] values = { element.getName(), caseCategory(element), region(element), emissionsYear(element),
                    meteorlogicalYear(element), format(element.getLastModifiedDate()), element.getRunStatus() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String meteorlogicalYear(Case element) {
        return element.getMeteorlogicalYear() != null ? element.getMeteorlogicalYear().getName() : "";
    }

    private String emissionsYear(Case element) {
        return element.getEmissionsYear() != null ? element.getEmissionsYear().getName() : "";
    }

    private String region(Case element) {
        return element.getRegion() != null ? element.getRegion().getName() : "N/A";
    }

    private String caseCategory(Case element) {
        return element.getCaseCategory() != null ? element.getCaseCategory().getName() : "N/A";
    }

}

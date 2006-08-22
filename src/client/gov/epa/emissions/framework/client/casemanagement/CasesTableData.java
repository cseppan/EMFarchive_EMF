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
        return new String[] { "Name", "Project", "Modeling Regn.", "Creator", "Category", "Run Status", "Abbrev.",
                "AQM", "Base Year", "Met. Year", "Speciation", "Last Modified Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 11)
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
            Row row = new ViewableRow(types[i], rowValues(types[i]));
            rows.add(row);
        }

        return rows;
    }

    private Object[] rowValues(Case element) {
        Object[] values = { element.getName(), project(element), region(element), creator(element),
                caseCategory(element), element.getRunStatus(), abbreviation(element), airQualityModel(element),
                emissionsYear(element), meteorlogicalYear(element), speciation(element),
                format(element.getLastModifiedDate()) };
        return values;
    }

    private String abbreviation(Case element) {
        return element.getAbbreviation() != null ? element.getAbbreviation().getName() : "";
    }

    private String airQualityModel(Case element) {
        return element.getAirQualityModel() != null ? element.getAirQualityModel().getName() : "";
    }

    private String speciation(Case element) {
        return element.getSpeciation() != null ? element.getSpeciation().getName() : "";
    }

    private String meteorlogicalYear(Case element) {
        return element.getMeteorlogicalYear() != null ? element.getMeteorlogicalYear().getName() : "";
    }

    private String emissionsYear(Case element) {
        return element.getEmissionsYear() != null ? element.getEmissionsYear().getName() : "";
    }

    private String region(Case element) {
        return element.getModelingRegion() != null ? element.getControlRegion().getName() : "";
    }

    private String caseCategory(Case element) {
        return element.getCaseCategory() != null ? element.getCaseCategory().getName() : "";
    }

    private String project(Case element) {
        return element.getProject() != null ? element.getProject().getName() : "";
    }

    private String creator(Case element) {
        return element.getLastModifiedBy() != null ? element.getLastModifiedBy().getName() : "";
    }

}

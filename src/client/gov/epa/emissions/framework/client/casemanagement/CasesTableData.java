package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CasesTableData extends AbstractTableData {
    private List rows;

    public CasesTableData(Case[] types) {
        this.rows = createRows(types);
    }

    public String[] columns() {
        return new String[] { "Name", "Project", "Modeling Regn.", "Creator", "Category", "Run Status", "Abbrev.",
                "AQM", "Base Year", "Met. Year", "Grid Name", "Grid Resolution", "Future Year", "Num Met Layers",
                "Start Date", "End Date", "Is Final", "Speciation", "Last Modified Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 14 || col ==15 ||col == 18)
            return Date.class;
        
        if (col == 16)
            return Boolean.class;
        
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }
    
    public void add(Case caseObj) {
        rows.add(row(caseObj));
    }
    
    private Row row(Case caseObj) {
        return new ViewableRow(caseObj, rowValues(caseObj));
    }


    private List createRows(Case[] types) {
        List rows = new ArrayList();

        for (int i = 0; i < types.length; i++) 
            rows.add(row(types[i]));

        return rows;
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }
    
    public Case[] sources() {
        List sources = sourcesList();
        return (Case[]) sources.toArray(new Case[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private Object[] rowValues(Case element) {
        Object[] values = { element.getName(), project(element), region(element), creator(element),
                caseCategory(element), element.getRunStatus(), abbreviation(element), airQualityModel(element),
                emissionsYear(element), meteorlogicalYear(element), Grid(element), gridResolution(element),
                futureYear(element), numMetLayers(element), format(element.getStartDate()), format(element.getEndDate()),
                isFinal(element), speciation(element), format(element.getLastModifiedDate()) };
        return values;
    }

    private Object isFinal(Case element) {
        return new Boolean(element.getIsFinal());
    }

    private Object numMetLayers(Case element) {
        return element.getNumMetLayers()+"" != null ? element.getNumMetLayers()+"" : "";
    }

    private Object futureYear(Case element) {
        return element.getFutureYear()+"" != null ? element.getFutureYear()+"" : "";
    }

    private Object gridResolution(Case element) {
        return element.getGridResolution() != null ? element.getGridResolution().getName() : "";
    }

    private String Grid(Case element) {
        return element.getGrid() != null ? element.getGrid().getName() : "";
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
        return element.getModelingRegion() != null ? element.getModelingRegion().getName() : "";
    }

    private String caseCategory(Case element) {
        return element.getCaseCategory() != null ? element.getCaseCategory().getName() : "";
    }

    private String project(Case element) {
        return element.getProject() != null ? element.getProject().getName() : "";
    }

    private String creator(Case element) {
        return element.getCreator() != null ? element.getCreator().getName() : "";
    }

}

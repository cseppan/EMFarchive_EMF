package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CasesTableData extends AbstractTableData {
    private List rows;

    public CasesTableData(Case[] cases) {
        this.rows = createRows(cases);
    }

    public String[] columns() {
        return new String[] { "Name", "Project", "Model to Run", "Modeling Regn.", "Last Modified By", "Category", "Run Status", "Abbrev.",
                "AQM", "Base Year", "Met. Year", "Future Year", "Grid Name", "Grid Resolution", "Num Met Layers",
                "Start Date", "End Date", "Is Final", "Speciation", "Last Modified Date" };
    }

    public Class getColumnClass(int col) {
        if (col == 17)
            return Boolean.class;
        
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    public void add(Case caseObj) {
        rows.add(row(caseObj));
    }
    
    private Row row(Case caseObj) {
        return new ViewableRow(caseObj, rowValues(caseObj));
    }


    private List createRows(Case[] cases) {
        List rows = new ArrayList();

        for (int i = 0; i < cases.length; i++) 
            rows.add(row(cases[i]));

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
        Object[] values = { element.getName(), project(element), modelToRun(element), region(element), modifiedBy(element),
                caseCategory(element), element.getRunStatus(), abbreviation(element), airQualityModel(element),
                emissionsYear(element), meteorlogicalYear(element), futureYear(element), Grid(element), gridResolution(element),
                numMetLayers(element), format(element.getStartDate()), format(element.getEndDate()),
                isFinal(element), speciation(element), format(element.getLastModifiedDate()) };
        return values;
    }

    private String modelToRun(Case element) {
        return element.getModel() != null ? element.getModel().getName() : "";
    }
    
    private Boolean isFinal(Case element) {
        return new Boolean(element.getIsFinal());
    }

    private String numMetLayers(Case element) {
        return element.getNumMetLayers() != null ? element.getNumMetLayers()+"" : "";
    }

    private String futureYear(Case element) {
        return element.getFutureYear()+"" != null ? element.getFutureYear()+"" : "";
    }

    private String gridResolution(Case element) {
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

    private String modifiedBy(Case element) {
        return element.getLastModifiedBy() != null ? element.getLastModifiedBy().getName() : "";
    }

}

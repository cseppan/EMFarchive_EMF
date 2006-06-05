package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureCost;
import gov.epa.emissions.framework.services.cost.data.ControlMeasureEfficiency;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    public ControlMeasureTableData(ControlMeasure[] measures) {
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Creator", "Abbreviation", "Control Eff.", "Cost per Ton", "Rule Eff.", "Rule Pen.", 
                "Major Poll.", "Description", "Region", "Equipment Life", "Class", 
                "Last Modified Time", "NEI Device Code" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(ControlMeasure[] measures) {
        List rows = new ArrayList();

        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            Object[] values = { measure.getName(), measure.getCreator().getName(), measure.getAbbreviation(),
                    getControlEfficiency(measure), getCostPerTon(measure),
                    new Float(measure.getRuleEffectiveness()), new Float(measure.getRulePenetration()), measure.getMajorPollutant(),
                    measure.getDescription(), getRegionName(measure), new Float(measure.getEquipmentLife()),
                    measure.getCmClass(), getLastModifiedTime(measure),
                    new Integer(measure.getDeviceCode())};

            Row row = new ViewableRow(measure, values);
            rows.add(row);
        }

        return rows;
    }
    
    private Float getCostPerTon(ControlMeasure measure) {
        ControlMeasureCost cost = measure.getCost();
        if(cost != null)
            return new Float(100);
        
        return null;
    }

    private Float getControlEfficiency(ControlMeasure measure) {
        ControlMeasureEfficiency eff = measure.getEfficiency();
        if(eff != null)
            return new Float(0.50);
        
        return null;
    }

    private Object getLastModifiedTime(ControlMeasure measure) {
        DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
        
        return dateFormat.format(measure.getLastModifiedTime());
    }

    private String getRegionName(ControlMeasure measure) {
        Region cmRegion = measure.getRegion();
        
        return cmRegion == null ? null:cmRegion.getName();
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col == 4 || col == 5 || col == 9 )
            return Float.class;
        
        if (col ==  12)
            return Integer.class;
        
        if (col < 0 || col > 12) {
            throw new IllegalArgumentException("Allowed values are between 0 and 6, but the value is " + col);
        }
        return String.class;
    }

}

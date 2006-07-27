package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    private static String pollutant = "Major";

    public ControlMeasureTableData(ControlMeasure[] measures) {
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Creator", "Abbreviation", "Control Eff.", "Cost per Ton", "Rule Eff.",
                "Rule Pen.", "Major Poll.", "Description", "Equipment Life", "Class", "Last Modified Time",
                "NEI Device Code" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(ControlMeasure[] measures) {
        List rows = new ArrayList();

        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            Object[] values = { measure.getName(), measure.getCreator().getName(), measure.getAbbreviation(),
                    getControlEfficiency(measure), getCostPerTon(measure), new Float(0),
                    new Float(0), measure.getMajorPollutant(), measure.getDescription(),
                    new Float(measure.getEquipmentLife()), measure.getCmClass(),
                    getLastModifiedTime(measure), new Integer(measure.getDeviceCode()) };

            Row row = new ViewableRow(measure, values);
            rows.add(row);
        }

        return rows;
    }

    private Float getCostPerTon(ControlMeasure measure) {
//        CostRecord[] records = measure.getCostRecords();
//        String localPollutant = getLocalPollutant(measure);
//
//        if (records.length != 0) {
//            for (int i = 0; i < records.length; i++)
//                if (records[i].getPollutant().equalsIgnoreCase(localPollutant)) {
//                    if (year == -9999)
//                        return new Float(records[i].getCostPerTon());
//                    if (records[i].getCostYear() == year)
//                        return new Float(records[i].getCostPerTon());
//                }
//
//        }

        return new Float(0);
    }

    private Float getControlEfficiency(ControlMeasure measure) {
        EfficiencyRecord[] records = measure.getEfficiencyRecords();
        String localPollutant = getLocalPollutant(measure);

        if (records.length != 0) {
            for (int i = 0; i < records.length; i++) {
                if (records[i].getPollutant().equalsIgnoreCase(localPollutant))
                    return new Float(records[i].getEfficiency());
            }
        }

        return null;
    }

    private String getLocalPollutant(ControlMeasure measure) {
        String localPollutant = pollutant;
        if (pollutant.equalsIgnoreCase("Major"))
            localPollutant = measure.getMajorPollutant().getName().trim();
        
        return localPollutant;
    }

    private Object getLastModifiedTime(ControlMeasure measure) {
        DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

        return dateFormat.format(measure.getLastModifiedTime());
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col == 4 || col == 5 || col == 9)
            return Float.class;

        if (col == 12)
            return Integer.class;

        if (col < 0 || col > 12) {
            throw new IllegalArgumentException("Allowed values are between 0 and 6, but the value is " + col);
        }
        return String.class;
    }
    
    public ControlMeasure[] sources() {
        List sources = sourcesList();
        return (ControlMeasure[]) sources.toArray(new ControlMeasure[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }
        
        return sources;
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public void setPollutantAndYear(String pollutant, String year) {
        this.pollutant = pollutant;
        
        refresh();
    }

}

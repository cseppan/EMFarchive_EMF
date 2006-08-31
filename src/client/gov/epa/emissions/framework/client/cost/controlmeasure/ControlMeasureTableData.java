package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    private ControlMeasure[] allMeasures;

    // private CostYearTable table;

    private Map maxEffMap;

    public ControlMeasureTableData(ControlMeasure[] measures, String pollutant, String year) {
        this.allMeasures = measures;
        maxEffMap = new HashMap();
        filter(pollutant, year);
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Major Poll.", "Control Eff.", "Cost per Ton", "Rule Eff.",
                "Rule Pen.", "Control Technology", "Source Group", "Equipment Life", "NEI Device Code", "Sectors",
                "Class", "Last Modified Time", "Date Reviewed", "Creator", "Data Source", "Description", };
    }

    public List rows() {
        return this.rows;
    }

    public void refresh(String pollutant, String year) {
        filter(pollutant, year);
        this.rows = createRows(allMeasures);
    }

    private List createRows(ControlMeasure[] measures) {
        List rows = new ArrayList();

        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            EfficiencyRecord record = (EfficiencyRecord) maxEffMap.get(measures[i].getAbbreviation());
            Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(record),
                    getControlEfficiency(record), getCostPerTon(record), ruleEffectiveness(record),
                    rulePenetration(record), getControlTechnology(measure), getSourceGroup(measure),
                    "" + measure.getEquipmentLife(), "" + measure.getDeviceCode(), getSectors(measure),
                    measureClass(measure.getCmClass()), getLastModifiedTime(measure), getDateReviewed(measure),
                    measure.getCreator().getName(), measure.getDataSouce(), measure.getDescription(), };

            Row row = new ViewableRow(measure, values);
            rows.add(row);
        }

        return rows;
    }

    private String pollutant(EfficiencyRecord record) {
        if (record == null)
            return "";
        return record.getPollutant().getName();
    }

    private String measureClass(String cmClass) {
        return (cmClass == null) ? "" : cmClass;
    }

    private String getSectors(ControlMeasure measure) {
        Sector[] sectors = measure.getSectors();
        if (sectors.length == 0)
            return null;

        return sectors[0].getName() + "...";
    }

    private Object getDateReviewed(ControlMeasure measure) {
        Date datereviewed = measure.getDateReviewed();
        if (datereviewed == null)
            return null;

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        return dateFormat.format(datereviewed);
    }

    private Object getSourceGroup(ControlMeasure measure) {
        SourceGroup sourcegroup = measure.getSourceGroup();
        if (sourcegroup == null)
            return null;

        return sourcegroup.getName();
    }

    private String getControlTechnology(ControlMeasure measure) {
        ControlTechnology technology = measure.getControlTechnology();
        if (technology == null)
            return null;

        return technology.getName();
    }

    private Object getLastModifiedTime(ControlMeasure measure) {
        DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());

        return dateFormat.format(measure.getLastModifiedTime());
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    private void filter(String pollutant, String year) {
        maxEffMap.clear();
        if (pollutant.equalsIgnoreCase("major"))
            measures(allMeasures);
        else
            measures(allMeasures, pollutant);

    }

    private void measures(ControlMeasure[] measures) {
        for (int i = 0; i < measures.length; i++) {
            EfficiencyRecord record = getMaxEffRecord(measures[i], measures[i].getMajorPollutant().getName());
            maxEffMap.put(measures[i].getAbbreviation(), record);
        }
    }

    private void measures(ControlMeasure[] measures, String pollutant) {
        for (int i = 0; i < measures.length; i++) {
            EfficiencyRecord record = getMaxEffRecord(measures[i], pollutant);
            maxEffMap.put(measures[i].getAbbreviation(), record);
        }
    }

    private EfficiencyRecord getMaxEffRecord(ControlMeasure measure, String pollutant) {
        EfficiencyRecord[] efficiencyRecords = filterPollutant(measure, pollutant);
        if (efficiencyRecords.length == 0)
            return null;

        EfficiencyRecord maxRecord = efficiencyRecords[0];
        for (int i = 1; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getEfficiency() > maxRecord.getEfficiency())
                maxRecord = efficiencyRecords[i];
        }

        return maxRecord;
    }

    private EfficiencyRecord[] filterPollutant(ControlMeasure measure, String pollutant) {
        List list = new ArrayList();
        EfficiencyRecord[] efficiencyRecords = measure.getEfficiencyRecords();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getPollutant().getName().equals(pollutant))
                list.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) list.toArray(new EfficiencyRecord[0]);
    }

    private Float getCostPerTon(EfficiencyRecord record) {
        if (record == null)
            return null;
        // int costYear = record.getCostYear();
        float costPerTon = record.getCostPerTon();
        // change the target year; table.setTargetYear(
        // table.factor(costYear);
        return new Float(costPerTon);
    }

    private Float getControlEfficiency(EfficiencyRecord record) {
        if (record == null)
            return null;

        return new Float(record.getEfficiency());
    }

    private String ruleEffectiveness(EfficiencyRecord record) {
        if (record == null)
            return "";
        return record.getRuleEffectiveness() + "";
    }

    private String rulePenetration(EfficiencyRecord record) {
        if (record == null)
            return "";
        return record.getRulePenetration() + "";
    }

}

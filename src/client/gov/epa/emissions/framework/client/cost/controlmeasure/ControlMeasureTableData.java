package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    private ControlMeasure[] allMeasures;

    private CostYearTable costYearTable;

    private Map maxEffMap;

    private int targetYear;

    private final static Double NAN_VALUE = new Double(Double.NaN);

    public ControlMeasureTableData(ControlMeasure[] measures, CostYearTable costYearTable, String pollutant, String year)
            throws EmfException {
        this.allMeasures = measures;
        this.costYearTable = costYearTable;
        maxEffMap = new HashMap();
        filter(pollutant, year);
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Pollutant", "Max CE", "Cost per Ton", "Rule Eff.", "Rule Pen.",
                "Control Technology", "Source Group", "Equipment Life", "NEI Device Code", "Sectors", "Class",
                "Last Modified Time", "Date Reviewed", "Creator", "Data Source", "Description", };
    }

    public Class getColumnClass(int col) {
        if ((col >= 3 && col <= 6) || col == 9)
            return Double.class;
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public void refresh(String pollutant, String year) throws EmfException {
        filter(pollutant, year);
        this.rows = createRows(allMeasures);
    }

    private List createRows(ControlMeasure[] measures) throws EmfException {
        List rows = new ArrayList();

        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            EfficiencyRecord record = (EfficiencyRecord) maxEffMap.get(measures[i].getAbbreviation());
            Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(record),
                    getControlEfficiency(record), getCostPerTon(record), ruleEffectiveness(record),
                    rulePenetration(record), getControlTechnology(measure), getSourceGroup(measure),
                    new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), getSectors(measure),
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

    private String measureClass(ControlMeasureClass cmClass) {
        return (cmClass == null) ? "" : cmClass.getName();
    }

    private String getSectors(ControlMeasure measure) {
        Sector[] sectors = measure.getSectors();
        String sectorsString = "";
        if (sectors.length == 0)
            return null;

        for (int i = 0; i < sectors.length; i++) {
            if (i == sectors.length - 1) {
                sectorsString += sectors[i].getName();
                break;
            }

            sectorsString += sectors[i].getName() + "|";

        }

        return sectorsString;
    }

    private Object getDateReviewed(ControlMeasure measure) {
        return EmfDateFormat.format_MM_DD_YYYY(measure.getDateReviewed());
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
        return EmfDateFormat.format_YYYY_MM_DD_HH_MM(measure.getLastModifiedTime());
    }

    public boolean isEditable(int col) {
        return false;
    }

    private void filter(String pollutant, String year) throws EmfException {
        maxEffMap.clear();
        this.targetYear = new YearValidation("Cost Year").value(year);
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

    private Double getCostPerTon(EfficiencyRecord record) throws EmfException {
        if (record == null)
            return NAN_VALUE;
        int costYear = record.getCostYear();
        float costPerTon = record.getCostPerTon();
        costYearTable.setTargetYear(targetYear);

        double newCost = costPerTon * costYearTable.factor(costYear);
        return new Double(newCost);
    }

    private Double getControlEfficiency(EfficiencyRecord record) {
        if (record == null)
            return NAN_VALUE;

        return new Double(record.getEfficiency());
    }

    private Double ruleEffectiveness(EfficiencyRecord record) {
        if (record == null)
            return NAN_VALUE;
        return new Double(record.getRuleEffectiveness());
    }

    private Double rulePenetration(EfficiencyRecord record) {
        if (record == null)
            return NAN_VALUE;
        return new Double(record.getRulePenetration());
    }

}

package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.YearValidation;
import gov.epa.emissions.framework.services.cost.data.AggregatedPollutantEfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    private ControlMeasure[] allMeasures;

    private CostYearTable costYearTable;

    private int targetYear;

    private final static int refYear = 2000;

    private final static Double NAN_VALUE = new Double(Double.NaN);
    
    private Pollutant pollutant; 

    public ControlMeasureTableData(ControlMeasure[] measures, CostYearTable costYearTable, Pollutant pollutant, String year)
            throws EmfException {
        this.allMeasures = measures;
        this.costYearTable = costYearTable;
        this.pollutant = pollutant;
//        filter(pollutant, year);
        this.targetYear = (year != null) ? new Integer(year) : 2000;
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Pollutant", 
                "Max CE", "Min CE", "Avg CE", 
                "Max Cost per Ton", "Min Cost per Ton", "Avg Cost per Ton", 
                "Avg Rule Eff.", "Avg Rule Pen.", "Control Technology", 
                "Source Group", "Equipment Life", "NEI Device Code", 
                "Sectors", "Class", "Last Modified Time", 
                "Last Modified By", "Date Reviewed", "Creator", 
                "Data Source", "Description" };
    }

    public Class getColumnClass(int col) {
        if ((col >= 3 && col <= 10) || col == 13)
            return Double.class;
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public void refresh(Pollutant pollutant, String year) throws EmfException {
        filter(pollutant, year);
        this.rows = createRows(allMeasures);
    }

    private List createRows(ControlMeasure[] measures) throws EmfException {
        List rows = new ArrayList();
        int year = targetYear;
        boolean found = false;
        targetYear = year;
        boolean majorPollutant = pollutant.getName().equalsIgnoreCase("major");
        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            AggregatedPollutantEfficiencyRecord[] apers = measure.getAggregatedPollutantEfficiencyRecords();
            found = false;
            for (int j = 0; j < apers.length; j++) {
                AggregatedPollutantEfficiencyRecord aper = apers[j];
                if (majorPollutant && measure.getMajorPollutant().equals(measure.getMajorPollutant())) {
                    Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(measure),
                            new Double(aper.getMaxEfficiency()), new Double(aper.getMinEfficiency()), new Double(aper.getAvgEfficiency()), 
                            getCostPerTon(aper.getMaxCostPerTon()), getCostPerTon(aper.getMinCostPerTon()), getCostPerTon(aper.getAvgCostPerTon()), 
                            new Double(aper.getAvgRuleEffectiveness()), new Double(aper.getAvgRulePenetration()), getControlTechnology(measure), 
                            getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                            getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                            measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                            measure.getDataSouce(), measure.getDescription() };
                    Row row = new ViewableRow(measure, values);
                    rows.add(row);
                    found = true;
                    break;
                } else if (pollutant.equals(aper.getPollutant())) {
                    Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(aper.getPollutant()),
                            new Double(aper.getMaxEfficiency()), new Double(aper.getMinEfficiency()), new Double(aper.getAvgEfficiency()), 
                            getCostPerTon(aper.getMaxCostPerTon()), getCostPerTon(aper.getMinCostPerTon()), getCostPerTon(aper.getAvgCostPerTon()), 
                            new Double(aper.getAvgRuleEffectiveness()), new Double(aper.getAvgRulePenetration()), getControlTechnology(measure), 
                            getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                            getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                            measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                            measure.getDataSouce(), measure.getDescription() };
                    Row row = new ViewableRow(measure, values);
                    rows.add(row);
                    found = true;
                    break;
                }
            }
            if (!found) {
                Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(measure),
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, getControlTechnology(measure), 
                        getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                        getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                        measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                        measure.getDataSouce(), measure.getDescription() };
                Row row = new ViewableRow(measure, values);
                rows.add(row);
            }
        }
        return rows;
    }

    protected List createRows_v1(ControlMeasure[] measures) throws EmfException {
        List rows = new ArrayList();
        int year = targetYear;
        targetYear = year;
        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            AggregatedPollutantEfficiencyRecord[] apers = measure.getAggregatedPollutantEfficiencyRecords();
            if (apers.length > 0) {
                for (int j = 0; j < apers.length; j++) {
                    AggregatedPollutantEfficiencyRecord aper = apers[j];
                    if (pollutant.getName().equalsIgnoreCase("major")) {
                        Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(measure),
                                new Double(aper.getMaxEfficiency()), new Double(aper.getMinEfficiency()), new Double(aper.getAvgEfficiency()), 
                                getCostPerTon(aper.getMaxCostPerTon()), getCostPerTon(aper.getMinCostPerTon()), getCostPerTon(aper.getAvgCostPerTon()), 
                                new Double(aper.getAvgRuleEffectiveness()), new Double(aper.getAvgRulePenetration()), getControlTechnology(measure), 
                                getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                                getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                                measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                                measure.getDataSouce(), measure.getDescription() };
                        Row row = new ViewableRow(measure, values);
                        rows.add(row);
                    } else {
                        if (!pollutant.equals(aper.getPollutant())) aper = null;
                        Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(measure),
                                new Double(aper != null ? aper.getMaxEfficiency() : NAN_VALUE), new Double(aper != null ? aper.getMinEfficiency() : NAN_VALUE), new Double(aper != null ? aper.getAvgEfficiency() : NAN_VALUE), 
                                getCostPerTon(aper != null ? aper.getMaxCostPerTon() : Float.NaN), getCostPerTon(aper != null ? aper.getMinCostPerTon() : Float.NaN), getCostPerTon(aper != null ? aper.getAvgCostPerTon() : Float.NaN), 
                                new Double(aper != null ? aper.getAvgRuleEffectiveness() : NAN_VALUE), new Double(aper != null ? aper.getAvgRulePenetration() : NAN_VALUE), getControlTechnology(measure), 
                                getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                                getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                                measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                                measure.getDataSouce(), measure.getDescription() };
                        Row row = new ViewableRow(measure, values);
                        rows.add(row);
                    }
                }
            } else {
                Object[] values = { measure.getName(), measure.getAbbreviation(), pollutant(measure),
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, NAN_VALUE, 
                        NAN_VALUE, NAN_VALUE, getControlTechnology(measure), 
                        getSourceGroup(measure), new Double(measure.getEquipmentLife()), "" + measure.getDeviceCode(), 
                        getSectors(measure), measureClass(measure.getCmClass()), getLastModifiedTime(measure), 
                        measure.getLastModifiedBy(), getDateReviewed(measure), measure.getCreator().getName(), 
                        measure.getDataSouce(), measure.getDescription() };
                Row row = new ViewableRow(measure, values);
                rows.add(row);
            }
        }

        return rows;
    }


    private String pollutant(ControlMeasure measure) {
        if (measure == null)
            return "";
        if (measure.getMajorPollutant() == null)
            return "";
        return measure.getMajorPollutant().getName();
    }

    private String pollutant(Pollutant pollutant) {
        if (pollutant == null)
            return "";
        return pollutant.getName();
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

    private void filter(Pollutant pollutant, String year) throws EmfException {
        this.targetYear = new YearValidation("Cost Year").value(year);
        this.pollutant = pollutant;
//        if (pollutant.equalsIgnoreCase("major"))
//            measures(allMeasures);
//        else
//            measures(allMeasures, pollutant);

    }

//    private void measures(ControlMeasure[] measures) {
//        for (int i = 0; i < measures.length; i++) {
//            EfficiencyRecord record = getMaxEffRecord(measures[i], measures[i].getMajorPollutant().getName());
//            maxEffMap.put(measures[i].getAbbreviation(), record);
//        }
//    }
//
//    private void measures(ControlMeasure[] measures, String pollutant) {
//        for (int i = 0; i < measures.length; i++) {
//            EfficiencyRecord record = getMaxEffRecord(measures[i], pollutant);
//            maxEffMap.put(measures[i].getAbbreviation(), record);
//        }
//    }

//    private EfficiencyRecord getMaxEffRecord(ControlMeasure measure, String pollutant) {
//        EfficiencyRecord[] efficiencyRecords = filterPollutant(measure, pollutant);
//        if (efficiencyRecords.length == 0)
//            return null;
//
//        EfficiencyRecord maxRecord = efficiencyRecords[0];
//        for (int i = 1; i < efficiencyRecords.length; i++) {
//            if (efficiencyRecords[i].getEfficiency() > maxRecord.getEfficiency())
//                maxRecord = efficiencyRecords[i];
//        }
//
//        return maxRecord;
//    }

//    private EfficiencyRecord[] filterPollutant(ControlMeasure measure, String pollutant) {
//        List list = new ArrayList();
//        EfficiencyRecord[] efficiencyRecords = measure.getEfficiencyRecords();
//        for (int i = 0; i < efficiencyRecords.length; i++) {
//            if (efficiencyRecords[i].getPollutant().getName().equals(pollutant))
//                list.add(efficiencyRecords[i]);
//        }
//        return (EfficiencyRecord[]) list.toArray(new EfficiencyRecord[0]);
//    }

    private Double getCostPerTon(float costPerTon) throws EmfException {
        if (costPerTon == 0)
            return NAN_VALUE;
        costYearTable.setTargetYear(targetYear);
        
        double newCost = costPerTon * costYearTable.factor(refYear);
        return new Double(newCost);
    }

//    private Double getCostPerTon(EfficiencyRecord record) throws EmfException {
//        if (record == null)
//            return NAN_VALUE;
//        int costYear = record.getCostYear();
//        float costPerTon = record.getCostPerTon();
//        costYearTable.setTargetYear(targetYear);
//
//        double newCost = costPerTon * costYearTable.factor(costYear);
//        return new Double(newCost);
//    }
//
//    private Double getControlEfficiency(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//
//        return new Double(record.getEfficiency());
//    }
//
//    private Double ruleEffectiveness(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//        return new Double(record.getRuleEffectiveness());
//    }
//
//    private Double rulePenetration(EfficiencyRecord record) {
//        if (record == null)
//            return NAN_VALUE;
//        return new Double(record.getRulePenetration());
//    }
//
}

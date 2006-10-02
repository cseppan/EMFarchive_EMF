package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ControlStrategiesTableData extends AbstractTableData {

    private List rows;

    public ControlStrategiesTableData(ControlStrategy[] controlStrategies) {
        this.rows = createRows(controlStrategies);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified", "Region", "Project", "Strategy Type","Inv. Dataset",
                "Version", "Inventory Type", "Target Pollutant", "Cost Year", "Inv. Year",
                "Total Cost", "Reduction", "Run Status", "Completion Date", "Creator" };

    }

    public Class getColumnClass(int col) {
        if (col == 1)
            return Date.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ControlStrategy[] controlStrategies) {
        List rows = new ArrayList();
        for (int i = 0; i < controlStrategies.length; i++) {
            ControlStrategy element = controlStrategies[i];
            Object[] values = { element.getName(), format(element.getLastModifiedDate()), region(element),
                    project(element), analysisType(element), dataset(element), version(element), 
                    datasetType(element), element.getTargetPollutant(),
                    costYear(element), "" + element.getInventoryYear(), "" + getTotalCost(element),
                    "" + getReduction(element), element.getRunStatus(), format(element.getCompletionDate()),
                    element.getCreator().getName()};
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private double getReduction(ControlStrategy element) {
        StrategyResult[] results = {};//FIXME:element.getStrategyResults();
        double totalReduction = 0;
        
        if(results.length > 0)
            for (int i = 0; i < results.length; i++)
                totalReduction += results[i].getTotalReduction();
        
        return totalReduction;
    }

    private double getTotalCost(ControlStrategy element) {
        StrategyResult[] results = {};// FIXME:element.getStrategyResults();
        double totalCost = 0;
        
        if(results.length > 0)
            for (int i = 0; i < results.length; i++)
                totalCost += results[i].getTotalCost();
        
        return totalCost;
    }

    private String version(ControlStrategy element) {
        return "" + element.getDatasetVersion();
    }

    private String dataset(ControlStrategy element) {
        Dataset[] datasets = element.getInputDatasets();
        if (datasets.length == 0)
            return "";
        
        String name = datasets[0].getName();
        if (datasets.length > 1)
            name += "...";
        
        return  name;
    }

    private String project(ControlStrategy element) {
        Project project = element.getProject();
        return project != null ? project.getName() : "";
    }

    private String region(ControlStrategy element) {
        return element.getRegion() != null ? element.getRegion().getName() : "";
    }

    private String analysisType(ControlStrategy element) {
        StrategyType type = element.getStrategyType();
        return type != null ? type.getName() : "";
    }

    private String datasetType(ControlStrategy element) {
        DatasetType datasetType = element.getDatasetType();
        return (datasetType != null) ? datasetType.getName() : "";
    }

//    private String discountRate(ControlStrategy element) {
//        return "" + element.getDiscountRate();
//    }

    private String costYear(ControlStrategy element) {
        return "" + element.getCostYear();
    }
    
    public ControlStrategy[] sources() {
        List sources = sourcesList();
        return (ControlStrategy[]) sources.toArray(new ControlStrategy[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }
        
        return sources;
    }

    private void remove(ControlStrategy record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlStrategy source = (ControlStrategy) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlStrategy[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

}

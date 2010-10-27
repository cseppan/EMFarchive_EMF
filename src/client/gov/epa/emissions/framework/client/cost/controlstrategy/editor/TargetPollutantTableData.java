package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.InlineEditableTableData;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.List;

public class TargetPollutantTableData extends AbstractEditableTableData implements InlineEditableTableData {

    private List<EditableRow> rows;
    
    private Pollutant[] pollutants;

    public TargetPollutantTableData(ControlStrategyTargetPollutant[] targets, Pollutant[] allPollutants) {
        pollutants = allPollutants;
        rows = createRows(targets);
    }

    private List<EditableRow> createRows(ControlStrategyTargetPollutant[] targets) {
        List<EditableRow> rows = new ArrayList<EditableRow>();
       
        for (int i = 0; i < targets.length; i++)
            rows.add(row(targets[i], pollutants));

        return rows;
    }
    
    private EditableRow row(ControlStrategyTargetPollutant target, Pollutant[] polls) {
        RowSource<ControlStrategyTargetPollutant> source = new TargetPollutantRowSource(target, polls);
        return new EditableRow(source);
    }
    
    public String[] columns() {
        return new String[] { "Select"
                            , "Pollutant"
                            , "MaxEmisReduction"
                            , "MaxControlEfficiency"
                            , "MinCostPerTon"
                            , "MinAnnCost"
                            //, "ReplacementControlMinEfficiencyDiff" 
                            };
    }

    public Class<?> getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;
        
        if (col == 1)
            return String.class;
        
        return Double.class;
    }

    public List<EditableRow> rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        if (col == 1)
            return false;
        
        return true;
    }
    
    public boolean isEditable(@SuppressWarnings("unused") int row, int col) {
        if (col == 0 || col == 1)
            return false;
        
        return true;
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlStrategyTargetPollutant[] sources() {
        return sourcesList().toArray(new ControlStrategyTargetPollutant[0]);
    }

    private List<ControlStrategyTargetPollutant> sourcesList() {
        List<ControlStrategyTargetPollutant> sources = new ArrayList<ControlStrategyTargetPollutant>();        
        for (EditableRow row : this.rows) {
            sources.add((ControlStrategyTargetPollutant)row.source());
        }

        return sources;
    }

    public boolean contains(ControlStrategyTargetPollutant reference) {
        return this.sourcesList().contains(reference);
    }
    
    public void add(ControlStrategyTargetPollutant target) {
        rows.add(row(target, pollutants));
    }

    private void remove(ControlStrategyTargetPollutant target) {
        for (EditableRow row : this.rows) {
            ControlStrategyTargetPollutant source = (ControlStrategyTargetPollutant)row.source();
            
            if (source.equals(target)) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(List<ControlStrategyTargetPollutant> references) {
        for (ControlStrategyTargetPollutant reference : references) {
            this.remove(reference);
        }
    }

    public void addBlankRow() {
        // NOTE Auto-generated method stub
        
    }

    public void removeSelected() {
        // NOTE Auto-generated method stub
        
    }

}

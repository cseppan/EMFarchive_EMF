package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class StrategyResultsTableData extends AbstractTableData {

    private List rows;

    public StrategyResultsTableData(StrategyResult[] strategyResults) {
        this.rows = createRows(strategyResults);
    }

    private List createRows(StrategyResult[] strategyResults) {
        List rows = new ArrayList();
        for (int i = 0; i < strategyResults.length; i++) {
            StrategyResult result = strategyResults[i];
            EmfDataset outputDataset = (EmfDataset) result.getDetailedResultDataset();
            Object[] values = {""+result.getDatasetId(), outputDataset.getName(), outputDataset.getStatus(),result.getStrategyResultType().getName() };
            Row row = new ViewableRow(result, values);
            rows.add(row);
        }
        //FIXME: remove this
        rows.add(result());
        return rows;
    }
    
    private Row result(){
        StrategyResult sr = new StrategyResult();
        sr.setDatasetId(104);
        StrategyResultType type = new StrategyResultType();
        type.setName("Detailed Strategy Result");
        sr.setStrategyResultType(type);
        EmfDataset outputDataset = new EmfDataset();
        outputDataset.setName("test output104");
        outputDataset.setStatus("Created By Control Strategy");
        sr.setDetailedResultDataset(outputDataset );
        
        return new ViewableRow(sr,new Object[]{""+sr.getDatasetId(),
                sr.getDetailedResultDataset().getName(),
                ((EmfDataset) sr.getDetailedResultDataset()).getStatus(),
                sr.getStrategyResultType().getName()});
    }

    public String[] columns() {
        return new String[] { "Input Dataset Id", "Output Dataset", "Status", "Product" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

}

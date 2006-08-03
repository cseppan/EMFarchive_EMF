package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.services.cost.analysis.maxreduction.InventoryOutputQuery;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class ControlStrategyInventoryOuputQuery {

    private EmfDataset dataset;

    private InventoryOutputQuery outputQuery;

    public ControlStrategyInventoryOuputQuery(EmfDataset dataset, InventoryOutputQuery outputQuery) {
        this.dataset = dataset;
        this.outputQuery = outputQuery;
    }

    // FIXME: what abt multiple tables
    // FIXME: add version query
    public String query(StrategyResult result) {
        String inputTableName = tableName(dataset);
        String detailResultTableName = tableName((EmfDataset) result.getDetailedResultDataset());

        String query = "SELECT " + outputQuery.selectClause("a", "b") + " FROM " + inputTableName + 
        " AS a LEFT JOIN " +detailResultTableName + " AS b "+ 
        " ON " + outputQuery.conditionalClause("a", "b");
        return query;
    }

    private String tableName(EmfDataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = internalSources[0].getTable();
        return tableName;
    }

}

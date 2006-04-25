package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ControlStrategiesTableData extends AbstractTableData {

    private List rows;

    public ControlStrategiesTableData(ControlStrategy[] controlStrategies) {
        this.rows = createRows(controlStrategies);
    }

    public String[] columns() {
        return new String[] { "Name", "Region", "Last Modified" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
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
            Object[] values = { element.getName(), region(element), format(element.getLastModifiedDate()) };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String region(ControlStrategy element) {
        return element.getRegion() != null ? element.getRegion().getName() : "";
    }

}

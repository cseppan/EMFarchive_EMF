package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class DatasetTypesTableData extends AbstractTableData {
    private List rows;

    public DatasetTypesTableData(DatasetType[] types) {
        this.rows = createRows(types);
    }

    public String[] columns() {
        return new String[] { "Name", "Description", "Min Files", "Max Files", "Min Cols", "Max Cols" };
    }

    public Class getColumnClass(int col) {
        if (col == 0 || col == 1)
            return String.class;

        return Integer.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    private List createRows(DatasetType[] types) {
        List rows = new ArrayList();

        for (int i = 0; i < types.length; i++) {
            DatasetType element = types[i];
            Object[] values = { element.getName(), element.getDescription(), new Integer(element.getMinfiles()),
                    new Integer(element.getMaxfiles()), new Integer(element.getMinColumns()),
                    new Integer(element.getMaxColumns()) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

}

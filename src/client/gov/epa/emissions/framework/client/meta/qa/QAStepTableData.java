package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QAStepTableData extends AbstractTableData {

    private List rows;

    private QAStep[] values;

    public QAStepTableData(QAStep[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Version", "Name", "User", "Date", "Program", "Required?", "Order", "Result", "Status" };
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Long.class;
        if (col == 3)
            return Date.class;
        if (col == 5)
            return Boolean.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows(QAStep[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(QAStep step) {
        return new ViewableRow(new QAStepRowSource(step));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public QAStep[] getValues() {
        return values;
    }

}

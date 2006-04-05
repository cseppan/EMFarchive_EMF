package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QAStepsTableData extends AbstractTableData {

    private List rows;

    private QAStep[] values;

    public QAStepsTableData(QAStep[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Version", "Name", "Required", "Order", "Status", "When", "Who", "Comment", "Program",
                "Arguments", "Configuration" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Boolean.class;

        if (col == 5)
            return Date.class;

        if (col == 6)
            return User.class;

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

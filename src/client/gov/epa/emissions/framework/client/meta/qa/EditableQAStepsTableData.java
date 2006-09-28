package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditableQAStepsTableData extends AbstractTableData {

    private List rows;

    private QASteps steps;

    public EditableQAStepsTableData(QAStep[] steps) {
        this.steps = new QASteps(steps);
        this.rows = createRows(this.steps);
    }

    public void refresh() {
        this.rows = createRows(steps);
    }

    public void add(QAStep step) {
        steps.add(step);
        rows.add(row(step));
    }

    public String[] columns() {
        return new String[] { "Version", "Name", "Required", "Order", "Status", "When", "Who", "Comment", "Program",
                "Arguments", "Configuration" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    private List createRows(QASteps steps) {
        List rows = new ArrayList();
        for (int i = 0; i < steps.size(); i++)
            rows.add(row(steps.get(i)));

        return rows;
    }

    private EditableRow row(QAStep step) {
        RowSource source = new QAStepRowSource(step);
        return new EditableRow(source);
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

    public QAStep[] sources() {
        List sources = sourcesList();
        return (QAStep[]) sources.toArray(new QAStep[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            QAStepRowSource rowSource = (QAStepRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

}

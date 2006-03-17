package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.EditableTableData;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditableQAStepTableData extends AbstractEditableTableData implements EditableTableData {

    private List rows;

    public EditableQAStepTableData(QAStep[] steps) {
        this.rows = createRows(steps);
    }

    public void add(QAStep step) {
        rows.add(row(step));
    }

    public String[] columns() {
        return new String[] { "Select", "QA Step ID", "Dataset ID", "Version", "Name", "Program", "Arguments",
                "Required", "Order", "When", "Who", "Result", "Status"};
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    private List createRows(QAStep[] steps) {
        List rows = new ArrayList();
        for (int i = 0; i < steps.length; i++)
            rows.add(row(steps[i]));

        return rows;
    }

    private EditableRow row(QAStep step) {
        RowSource source = new EditableQAStepRowSource(step);
        return new EditableRow(source);
    }

    public Class getColumnClass(int col) {
        if (col == 0 || col == 7)
            return Boolean.class;
        
        if(col == 9)
            return Date.class;
        
        if(col == 10)
            return User.class;

        return String.class;
    }

    public QAStep[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableQAStepRowSource rowSource = (EditableQAStepRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (QAStep[]) selected.toArray(new QAStep[0]);
    }

    public QAStep[] sources() {
        List sources = sourcesList();
        return (QAStep[]) sources.toArray(new QAStep[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableQAStepRowSource rowSource = (EditableQAStepRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

}

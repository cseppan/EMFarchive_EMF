package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InputsTableData extends ChangeableTableData {

    private List rows;

    private CaseInput[] values;

    private List additions;

    public InputsTableData(CaseInput[] values) {
        this.values = values;
        this.rows = createRows(values);
        this.additions = new ArrayList();
    }

    public String[] columns() {
        return new String[] { "Id", "Note Name" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Long.class;
        if (col == 4)
            return Date.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseInput note) {
        additions.add(note);
        rows.add(row(note));
        notifyChanges();
    }

    private List createRows(CaseInput[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(CaseInput note) {
        return new ViewableRow(new InputsRowSource(note));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseInput[] getValues() {
        return values;
    }

    public CaseInput[] additions() {
        return (CaseInput[]) additions.toArray(new CaseInput[0]);
    }

}

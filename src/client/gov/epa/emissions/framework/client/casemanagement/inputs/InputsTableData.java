package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputsTableData extends AbstractTableData {

    private List rows;

    private CaseInput[] values;

    public InputsTableData(CaseInput[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Input", "Sector", "Program", "Envt. Var", "Dataset", "Version", "QA Status", "DS Type",
                "Reqd?", "Show?", "Sub Dir" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseInput input) {
        rows.add(row(input));
    }

    private List createRows(CaseInput[] values) {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(CaseInput input) {
        return new ViewableRow(new InputsRowSource(input));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseInput[] getValues() {
        return values;
    }
    
    public void remove(CaseInput[] inputs) {
        for (int i = 0; i < inputs.length; i++)
            remove(inputs[i]);
    }
    
    private void remove(CaseInput input) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseInput source = (CaseInput) row.source();
            if (source == input) {
                rows.remove(row);
                return;
            }
        }
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }

    public CaseInput[] sources() {
        List sources = sourcesList();
        return (CaseInput[]) sources.toArray(new CaseInput[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

}

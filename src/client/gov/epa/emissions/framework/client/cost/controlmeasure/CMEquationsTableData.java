package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMEquationsTableData extends AbstractEditableTableData {

    private List<EditableRow> rows;
    
    public CMEquationsTableData(ControlMeasureEquation[] equations) {
        rows = createRows(equations);
    }

    private List<EditableRow> createRows(ControlMeasureEquation[] equations) {
        rows = new ArrayList<EditableRow>();
        for(int i=0; i< equations.length; i++){
            rows.add(row(equations[i]));
        }
        return rows;
    }

    private EditableRow row(ControlMeasureEquation equation) {
        RowSource source = new EditableEquationVariableRowSource(equation);
        return new EditableRow(source);
    }

    public String[] columns() {
        return new String[] { "Eqs", "Variable Name", "Value"};
    }

    public Class getColumnClass(int col) {
        if (col==2)
            return Double.class;
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        if (col == 2)
            return true;
        return false;
    }

    public void refresh() {
       //
    }

    public ControlMeasureEquation[] sources() {
        List<ControlMeasureEquation> sources = sourcesList();
        return sources.toArray(new ControlMeasureEquation[0]);
    }

    private List<ControlMeasureEquation> sourcesList() {
        List<ControlMeasureEquation> sources = new ArrayList<ControlMeasureEquation>();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableEquationVariableRowSource rowSource = (EditableEquationVariableRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add((ControlMeasureEquation)rowSource.source());
        }
        return sources;
    }
//
//    private List sourcesList() {
//        List sources = new ArrayList();
//        for (Iterator iter = rows.iterator(); iter.hasNext();) {
//            ViewableRow row = (ViewableRow) iter.next();
//            sources.add(row.source());
//        }
//
//        return sources;
//    }

//    private void remove() {
//        for (Iterator iter = rows.iterator(); iter.hasNext();) {
//            ViewableRow row = (ViewableRow) iter.next();
//            List source = row.source();
//            if (source == record) {
//                rows.remove(row);
//                return;
//            }
//        }
//    }
//
//    public void remove(Scc[] records) {
//        for (int i = 0; i < records.length; i++)
//            remove(records[i]);
//
//        refresh();
//    }

}

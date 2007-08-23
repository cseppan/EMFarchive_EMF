package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquationType;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquationTypeVariable;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMEquationsTableData extends AbstractEditableTableData {

    private List<EditableRow> rows;
    
    public CMEquationsTableData(ControlMeasureEquationType[] equationTypes) {
        rows = createRows(equationTypes);
    }

    private List createRows(ControlMeasureEquationType[] equationTypes) {
        rows = new ArrayList();
        for(int i=0; i<equationTypes.length; i++){
            ControlMeasureEquationTypeVariable[] cMEquaitonTypeVariables = equationTypes[i].getEquationTypeVariables();
            
            for (int j = 0; j < cMEquaitonTypeVariables.length; j++) {
                rows.add(row(equationTypes[i], cMEquaitonTypeVariables[j]));
            }
        }
        return rows;
    }

    private EditableRow row(ControlMeasureEquationType equationType,ControlMeasureEquationTypeVariable equaitonTypeVariable) {
        RowSource source = new EditableEquationVariableRowSource(equaitonTypeVariable);
        return new EditableRow(source);
    }

    public String[] columns() {
        return new String[] { "Eqs","Variable Name", "Value", "Description"};
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

    public ControlMeasureEquationTypeVariable[] sources() {
        List<ControlMeasureEquationTypeVariable> sources = sourcesList();
        return sources.toArray(new ControlMeasureEquationTypeVariable[0]);
    }

    private List<ControlMeasureEquationTypeVariable> sourcesList() {
        List<ControlMeasureEquationTypeVariable> sources = new ArrayList<ControlMeasureEquationTypeVariable>();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableEquationVariableRowSource rowSource = (EditableEquationVariableRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add((ControlMeasureEquationTypeVariable)rowSource.source());
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

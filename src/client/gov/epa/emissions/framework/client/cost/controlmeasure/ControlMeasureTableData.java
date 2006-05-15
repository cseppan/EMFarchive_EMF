package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ControlMeasureTableData extends AbstractTableData {

    private List rows;

    public ControlMeasureTableData(ControlMeasure[] measures) {
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Creator", "Annd. Cost", "Rule Eff.", "Rule Pen.", 
                "Major Poll.", "Description" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(ControlMeasure[] measures) {
        List rows = new ArrayList();

        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            Object[] values = { measure.getName(), measure.getCreator().getName(), new Float(measure.getAnnualizedCost()),
                    new Float(measure.getRuleEffectiveness()), new Float(measure.getRulePenetration()), measure.getMajorPollutant(),
                    measure.getDescription() };

            Row row = new ViewableRow(measure, values);
            rows.add(row);
        }

        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col > 1 && col < 5)
            return Float.class;
        if (col < 0 || col > 6) {
            throw new IllegalArgumentException("Allowed values are between 0 and 6, but the value is " + col);
        }
        return String.class;
    }

}

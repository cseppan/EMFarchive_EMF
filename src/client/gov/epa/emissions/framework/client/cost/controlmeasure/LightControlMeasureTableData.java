package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class LightControlMeasureTableData extends ControlMeasureTableData {

    public LightControlMeasureTableData(ControlMeasure[] measures, CostYearTable costYearTable, Pollutant pollutant, String year) {
        super(measures, pollutant);
        this.rows = createRows(measures);
    }

    public String[] columns() {
        return new String[] { "Name", "Abbreviation", "Pollutant" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public void refresh(Pollutant pollutant, String year) {
       //
    }

    private List createRows(ControlMeasure[] measures) {
        List rows = new ArrayList();
        int year = targetYear;
        targetYear = year;
        for (int i = 0; i < measures.length; i++) {
            ControlMeasure measure = measures[i];
            Object[] values = { measure.getName(), measure.getAbbreviation(), getPollutantName(measure) };
            Row row = new ViewableRow(measure, values);
            rows.add(row);
        }
        return rows;
    }

    private String getPollutantName(ControlMeasure measure) {
        if (measure == null)
            return "";
        if (measure.getMajorPollutant() == null)
            return "";
        return measure.getMajorPollutant().getName();
    }

    public boolean isEditable(int col) {
        return false;
    }
}

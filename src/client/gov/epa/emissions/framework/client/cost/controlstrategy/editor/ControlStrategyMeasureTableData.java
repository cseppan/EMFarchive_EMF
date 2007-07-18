package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlStrategyMeasureTableData extends AbstractTableData {

    private List rows;

    public ControlStrategyMeasureTableData(ControlStrategyMeasure[] cms) {
        rows = createRows(cms);
    }

    private List createRows(ControlStrategyMeasure[] cms) {
        List rows = new ArrayList();
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(ControlStrategyMeasure csm) {
        LightControlMeasure cm = csm.getControlMeasure();
        Object[] values = { cm.getAbbreviation(), 
                        cm.getName(), 
                        csm.getRulePenetration() != null ? csm.getRulePenetration() : Double.NaN, 
                        csm.getRuleEffectiveness() != null ? csm.getRuleEffectiveness() : Double.NaN,
                        csm.getApplyOrder() != null ? csm.getApplyOrder() : Double.NaN};
        return new ViewableRow(csm, values);
    }

    public String[] columns() {
        return new String[] { "Abbrev", "Name", 
                "Rule Penetration", "Rule Effectiveness", 
                "Apply Order" };
    }

    public Class getColumnClass(int col) {
        if (col == 2 || col == 3 || col == 4)
            return Double.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(ControlStrategyMeasure[] cms) {
        for (int i = 0; i < cms.length; i++) {
            Row row = row(cms[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public ControlStrategyMeasure[] sources() {
        List sources = sourcesList();
        return (ControlStrategyMeasure[]) sources.toArray(new ControlStrategyMeasure[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(ControlStrategyMeasure record) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            ControlStrategyMeasure source = (ControlStrategyMeasure) row.source();
            if (source == record) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(ControlStrategyMeasure[] records) {
        for (int i = 0; i < records.length; i++)
            remove(records[i]);

        refresh();
    }

}
